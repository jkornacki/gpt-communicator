package pl.apps.gptdemo.gptdemo.conversations;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Builder
public class Conversation {

    private Long id;
    private String title;
    private LocalDateTime createDate;
    private LocalDateTime deleteDate;
    private String systemPrompt;
    private List<ConversationItems> items;

    public static Conversation createNew(String prompt, LocalDateTime sendPromptDate, String promptResponse, String suggestedTitle, String systemPrompt) {
        return Conversation.builder()
                .title(suggestedTitle)
                .createDate(LocalDateTime.now())
                .deleteDate(null)
                .items(List.of(
                        ConversationItems.createNew(prompt, null, SendBy.USER, sendPromptDate),
                        ConversationItems.createNew(prompt, promptResponse, SendBy.GPT, LocalDateTime.now())
                ))
                .systemPrompt(Objects.nonNull(systemPrompt) && !systemPrompt.isBlank() ? systemPrompt : null)
                .build();
    }

    public static Conversation empty() {
        return Conversation.builder().build();
    }

    public void addItem(String prompt, LocalDateTime sendPromptDate, String promptResponse) {
        if (items == null) {
            items = new ArrayList<>();
        }

        items.add(ConversationItems.createNew(prompt, null, SendBy.USER, sendPromptDate));
        items.add(ConversationItems.createNew(prompt, promptResponse, SendBy.GPT, LocalDateTime.now()));
    }

    public void setItems(List<ConversationItems> conversationItems) {
        if (conversationItems == null) {
            return;
        }
        if (items == null) {
            items = new ArrayList<>();
        }

        items.addAll(conversationItems);
    }

    public void editTitle(String newTitle) {
        this.title = newTitle;
    }

    public void delete() {
        this.deleteDate = LocalDateTime.now();
    }

    public void editSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public void removeConversationSystemPrompt() {
        this.systemPrompt = null;
    }
}
