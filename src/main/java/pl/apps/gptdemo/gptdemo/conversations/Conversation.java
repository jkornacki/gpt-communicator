package pl.apps.gptdemo.gptdemo.conversations;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class Conversation {

    private Long id;
    private String title;
    private List<ConversationItems> items;

    public static Conversation createNew(String prompt, String promptResponse, String suggestedTitle) {
        return Conversation.builder()
                .title(suggestedTitle)
                .items(List.of(
                        ConversationItems.createNew(prompt, null, SendBy.USER),
                        ConversationItems.createNew(prompt, promptResponse, SendBy.GPT)
                ))
                .build();
    }

    public static Conversation empty() {
        return Conversation.builder().build();
    }

    public void addItem(String prompt, String promptResponse) {
        if (items == null) {
            items = new ArrayList<>();
        }

        items.add(ConversationItems.createNew(prompt, null, SendBy.USER));
        items.add(ConversationItems.createNew(prompt, promptResponse, SendBy.GPT));
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
}
