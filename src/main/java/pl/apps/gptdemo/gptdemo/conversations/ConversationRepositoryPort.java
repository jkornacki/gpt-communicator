package pl.apps.gptdemo.gptdemo.conversations;

import java.util.List;

public interface ConversationRepositoryPort {

    Conversation saveConversation(Conversation conversation);

    List<Conversation> findAll();

    Conversation getConversationWithItems(Long conversationId);

    Conversation getConversationWithoutItems(Long conversationId);

}
