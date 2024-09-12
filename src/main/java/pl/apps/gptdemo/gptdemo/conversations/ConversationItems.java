package pl.apps.gptdemo.gptdemo.conversations;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ConversationItems {

    private Long id;
    private SendBy sendBy;
    private String prompt;
    private String content;
    private LocalDateTime createdAt;

    public static ConversationItems createNew(String prompt, String promptResponse, SendBy sendBy) {
        return ConversationItems.builder()
                .sendBy(sendBy)
                .prompt(prompt)
                .content(promptResponse)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
