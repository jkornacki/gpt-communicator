package pl.apps.gptdemo.gptdemo.conversations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.apps.gptdemo.gptdemo.GptConfiguration;
import pl.apps.gptdemo.gptdemo.api.ConversationApiResponse;
import pl.apps.gptdemo.gptdemo.api.ConversationItemDto;
import pl.apps.gptdemo.gptdemo.api.ConversationWithItemsApiResponse;
import pl.apps.gptdemo.gptdemo.api.PromptApiResponse;
import pl.apps.gptdemo.gptdemo.gpt_client.ClaudeApiClientRequest;
import pl.apps.gptdemo.gptdemo.gpt_client.ClaudeApiClientResponse;
import pl.apps.gptdemo.gptdemo.gpt_client.GptApiClient;

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
    public PromptApiResponse sendPromptForNewConversation(String prompt) {

        ClaudeApiClientResponse promptApiResponse = gptApiClient.sendRequest(
                ClaudeApiClientRequest.createNewMessage(prompt, configuration.getAnthropicSystemPrompt())
        );

        var promptResponse = promptApiResponse.getFirstContentText();
        var titleResponse = gptApiClient.sendRequest(
                ClaudeApiClientRequest.createPromptForTitleCreate(prompt, promptResponse, configuration.getAnthropicSystemTitlePrompt())
        );

        Conversation conversation = Conversation.createNew(prompt, promptResponse, titleResponse.getFirstContentText());

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

        Conversation conversation = Optional.ofNullable(conversationRepositoryPort.getConversationWithoutItems(conversationId))
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        ClaudeApiClientResponse promptApiResponse = gptApiClient.sendRequest(
                ClaudeApiClientRequest.createNewMessage(prompt, configuration.getAnthropicSystemPrompt())
        );

        var promptResponse = promptApiResponse.getFirstContentText();
        conversation.addItem(prompt, promptResponse);

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
}
