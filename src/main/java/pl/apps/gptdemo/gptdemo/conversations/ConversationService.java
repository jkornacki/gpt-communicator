package pl.apps.gptdemo.gptdemo.conversations;

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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepositoryPort conversationRepositoryPort;
    private final ConversationMapper conversationMapper;
    private final GptConfiguration configuration;
    private final GptApiClient gptApiClient;

    @Transactional
    public PromptApiResponse sendPromptForNewConversation(String prompt,
                                                          String systemPrompt) {

        LocalDateTime sendPromptDate = LocalDateTime.now();
        String promptResponse = gptApiClient.sendMessage(prompt, systemPrompt);

        var titleResponse = gptApiClient.sendTitleGenerationMessage(promptResponse);

        Conversation conversation = Conversation.createNew(prompt, sendPromptDate, promptResponse, titleResponse, systemPrompt);
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

    @Transactional
    public PromptApiResponse sendPromptForExistingConversation(Long conversationId, String prompt) {

        LocalDateTime sendPrompt = LocalDateTime.now();
        Conversation conversation = Optional.ofNullable(conversationRepositoryPort.getConversationWithItems(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        ChatMemory chatMemory = new ConversationItemChatMemory(configuration.getAnthropicSystemPrompt(),
                conversation, configuration.getDefaultNumberOfMessageHistory());

        var promptApiResponse = gptApiClient.sendMessage(prompt, chatMemory, conversation.getSystemPrompt());

        conversation.addItem(prompt, sendPrompt, promptApiResponse);

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
    public void editConversationTitle(Long conversationId, String newTitle) {

        Conversation conversation = Optional.ofNullable(conversationRepositoryPort.getConversationWithoutItems(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        conversation.editTitle(newTitle);

        conversationRepositoryPort.saveConversation(conversation);
    }

    @Transactional
    public void deleteConversation(Long conversationId) {

        Conversation conversation = Optional.ofNullable(conversationRepositoryPort.getConversationWithoutItems(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        conversation.delete();
        conversationRepositoryPort.saveConversation(conversation);

    }

    @Transactional
    public void editConversationSystemPrompt(Long conversationId, String systemPrompt) {

        Conversation conversation = Optional.ofNullable(conversationRepositoryPort.getConversationWithoutItems(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        if (systemPrompt == null || systemPrompt.isBlank()) {
            throw new IllegalArgumentException("systemPrompt is empty");
        }

        conversation.editSystemPrompt(systemPrompt);
        conversationRepositoryPort.saveConversation(conversation);
    }

    public void removeConversationSystemPrompt(Long conversationId) {
        Conversation conversation = Optional.ofNullable(conversationRepositoryPort.getConversationWithoutItems(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        conversation.removeConversationSystemPrompt();
        conversationRepositoryPort.saveConversation(conversation);
    }
}
