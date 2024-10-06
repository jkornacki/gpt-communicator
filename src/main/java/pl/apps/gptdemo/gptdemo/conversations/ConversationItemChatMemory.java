package pl.apps.gptdemo.gptdemo.conversations;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConversationItemChatMemory implements ChatMemory {

    private List<ChatMessage> messages = Collections.synchronizedList(new ArrayList<>());

    public ConversationItemChatMemory(String systemMessage, Conversation conversation, int numberOfMessageHistory) {
        List<ConversationItems> items = new ArrayList<>();

        var orderedItems = conversation.getItems().stream()
                .sorted(Comparator.comparing(ConversationItems::getCreatedAt))
                .toList();

        int currentIndex = 0;
        for (ConversationItems conversationItems : orderedItems) {

            items.add(conversationItems);
            if (conversationItems.isUserPrompt()) {
                currentIndex++;
            } else {
                if (currentIndex >= numberOfMessageHistory) {
                    break;
                }
            }

        }

        messages.clear();
        messages.add(new SystemMessage(systemMessage));
        messages.addAll(items.stream()
                .map(this::convertToChatMessage)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private ChatMessage convertToChatMessage(ConversationItems conversationItems) {
        if (conversationItems.isUserPrompt()) {
            if (conversationItems.getPrompt() == null || conversationItems.getPrompt().isBlank()) {
                return null;
            }
            return UserMessage.from(conversationItems.getPrompt());
        }
        if (conversationItems.getContent() == null || conversationItems.getContent().isBlank()) {
            return null;
        }
        return AiMessage.from(conversationItems.getContent());
    }

    @Override
    public Object id() {
        return "default";
    }

    @Override
    public void add(ChatMessage message) {
        if (!(message instanceof SystemMessage)) {
            messages.add(message);
        }
    }

    @Override
    public List<ChatMessage> messages() {
        return messages;
    }

    @Override
    public void clear() {
        messages.clear();
    }
}
