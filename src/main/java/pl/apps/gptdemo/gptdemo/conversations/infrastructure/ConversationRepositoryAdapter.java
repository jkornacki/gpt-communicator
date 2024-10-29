package pl.apps.gptdemo.gptdemo.conversations.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.apps.gptdemo.gptdemo.conversations.Conversation;
import pl.apps.gptdemo.gptdemo.conversations.ConversationItems;
import pl.apps.gptdemo.gptdemo.conversations.ConversationRepositoryPort;
import pl.apps.gptdemo.gptdemo.conversations.SystemPrompt;

import java.util.List;

@Service
@RequiredArgsConstructor
class ConversationRepositoryAdapter implements ConversationRepositoryPort {

    private final ConversationJpaRepository conversationJpaRepository;
    private final ConversationItemsJpaRepository conversationItemsJpaRepository;
    private final ConversationRepositoryMapper conversationRepositoryMapper;

    @Override
    public Conversation saveConversation(Conversation conversation) {

        ConversationEntity conversationEntity = conversationRepositoryMapper.mapToConversationEntity(conversation);

        List<ConversationItems> conversationItems = null;
        var saved = conversationJpaRepository.save(conversationEntity);
        if (conversation.getItems() != null && !conversation.getItems().isEmpty()) {
            var items = conversation.getItems()
                    .stream()
                    .map(item -> conversationRepositoryMapper.mapToConversationItemsEntity(item, saved))
                    .toList();

            conversationItems = conversationItemsJpaRepository.saveAll(items)
                    .stream()
                    .map(conversationRepositoryMapper::mapToConversationItems)
                    .toList();
        }
        var savedConversation = conversationRepositoryMapper.mapToConversation(saved);
        savedConversation.setItems(conversationItems);
        return savedConversation;
    }

    @Override
    public List<Conversation> findAll() {
        return conversationJpaRepository.findAllNotDeleted()
                .stream()
                .map(conversationRepositoryMapper::mapToConversationWithoutItems)
                .toList();

    }

    @Override
    public Conversation getConversationWithItems(Long conversationId) {
        return conversationJpaRepository.findById(conversationId)
                .filter(conversationEntity -> conversationEntity.getDeleteDate() == null)
                .map(conversationRepositoryMapper::mapToConversation)
                .orElse(Conversation.empty());
    }

    @Override
    public Conversation getConversationWithoutItems(Long conversationId) {
        return conversationJpaRepository.findById(conversationId)
                .filter(conversationEntity -> conversationEntity.getDeleteDate() == null)
                .map(conversationRepositoryMapper::mapToConversationWithoutItems)
                .orElse(Conversation.empty());
    }

    @Override
    public List<SystemPrompt> findSystemPromptsForAllConversations() {
        return conversationJpaRepository.findSystemPromptsForAllConversations();
    }
}
