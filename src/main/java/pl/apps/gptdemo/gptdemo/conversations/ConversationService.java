package pl.apps.gptdemo.gptdemo.conversations;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.apps.gptdemo.gptdemo.GptConfiguration;
import pl.apps.gptdemo.gptdemo.api.ConversationApiResponse;
import pl.apps.gptdemo.gptdemo.api.ConversationItemDto;
import pl.apps.gptdemo.gptdemo.api.ConversationWithItemsApiResponse;
import pl.apps.gptdemo.gptdemo.api.PromptApiResponse;
import pl.apps.gptdemo.gptdemo.gpt_client.GptApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepositoryPort conversationRepositoryPort;
    private final ConversationMapper conversationMapper;
    private final GptConfiguration configuration;
    private final GptApiClient gptApiClient;

    @Transactional
    public PromptApiResponse sendPromptForNewConversation(String prompt) {

        String promptResponse = gptApiClient.sendMessage(prompt);

        var titleResponse = gptApiClient.sendTitleGenerationMessage(promptResponse);

        Conversation conversation = Conversation.createNew(prompt, promptResponse, titleResponse);
        Conversation savedConversation = conversationRepositoryPort.saveConversation(conversation);

        return new PromptApiResponse(
                savedConversation.getId(),
                savedConversation.getTitle(),
                promptResponse,
                Optional.ofNullable(savedConversation.getItems())
                        .map(conversationItems -> conversationItems.stream()
                                .map(conversationMapper::mapToConversationWithItemsApiResponseItem)
                                .toList()
                        ).orElse(null)
        );
    }

    @Transactional(readOnly = true)
    public List<ConversationApiResponse> fetchAllConversations() {

        List<Conversation> findAllConversations = conversationRepositoryPort.findAll();
        return findAllConversations.stream()
                .map(conversationMapper::mapToConversationApiResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationWithItemsApiResponse fetchConversationWithItems(Long conversationId) {

        Conversation conversationWithItems = Optional.ofNullable(conversationRepositoryPort.getConversationWithItems(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));


        List<ConversationItemDto> items;
        if (conversationWithItems.getItems() != null && !conversationWithItems.getItems().isEmpty()) {
            items = conversationWithItems.getItems().stream()
                    .sorted(Comparator.comparing(ConversationItems::getCreatedAt).reversed())
                    .map(conversationMapper::mapToConversationWithItemsApiResponseItem)
                    .toList();
        } else {
            items = Collections.emptyList();
        }
        return conversationMapper.mapToConversationWithItemsApiResponse(conversationWithItems, items);
    }

    @Transactional
    public PromptApiResponse sendPromptForExistingConversation(Long conversationId, String prompt) {

        Conversation conversation = Optional.ofNullable(conversationRepositoryPort.getConversationWithItems(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        ChatMemory chatMemory = prepareChatMemoryWithNumberOfUserMessages(conversation, configuration.getDefaultNumberOfMessageHistory());

        var promptApiResponse = gptApiClient.sendMessage(prompt, chatMemory);

        conversation.addItem(prompt, promptApiResponse);

        Conversation savedConversation = conversationRepositoryPort.saveConversation(conversation);

        return new PromptApiResponse(
                savedConversation.getId(),
                savedConversation.getTitle(),
                promptApiResponse,
                Optional.ofNullable(savedConversation.getItems())
                        .map(conversationItems -> conversationItems.stream()
                                .map(conversationMapper::mapToConversationWithItemsApiResponseItem)
                                .toList()
                        ).orElse(null)
        );

    }

    private ChatMemory prepareChatMemoryWithNumberOfUserMessages(Conversation conversation, int numberOfMessageHistory) {

        List<ConversationItems> items = new ArrayList<>();

        var orderedItems = conversation.getItems().stream()
                .sorted(Comparator.comparing(ConversationItems::getCreatedAt).reversed())
                .toList();

        int currentIndex = 0;
        for (ConversationItems conversationItems : orderedItems) {

            if (currentIndex > numberOfMessageHistory) {
                break;
            }

            items.add(conversationItems);

            if (conversationItems.isUserPrompt()) {
                currentIndex++;
            }

        }

        var chatMessages = items.stream()
                .map(this::convertToChatMessage)
                .collect(Collectors.toCollection(ArrayList::new));


        return new ChatMemory() {
            @Override
            public Object id() {
                return "default";
            }

            @Override
            public void add(ChatMessage chatMessage) {
                chatMessages.add(chatMessage);
            }

            @Override
            public List<ChatMessage> messages() {
                return chatMessages;
            }

            @Override
            public void clear() {
                chatMessages.clear();
            }
        };
    }

    private ChatMessage convertToChatMessage(ConversationItems conversationItems) {
        if (conversationItems.isUserPrompt()) {
            return UserMessage.from(conversationItems.getPrompt());
        }
        return AiMessage.from(conversationItems.getContent());
    }

    @Transactional
    public void editConversationTitle(Long conversationId, String newTitle) {
        Conversation conversation = Optional.ofNullable(conversationRepositoryPort.getConversationWithoutItems(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        conversation.editTitle(newTitle);

        conversationRepositoryPort.saveConversation(conversation);
    }

    public void deleteConversation(Long conversationId) {

        Conversation conversation = Optional.ofNullable(conversationRepositoryPort.getConversationWithoutItems(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        conversation.delete();

        conversationRepositoryPort.saveConversation(conversation);

    }
}
