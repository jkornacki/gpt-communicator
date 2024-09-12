package pl.apps.gptdemo.gptdemo.api;

import pl.apps.gptdemo.gptdemo.conversations.SendBy;

import java.time.LocalDateTime;

public record ConversationItemDto(
        Long id,
        SendBy sendBy,
        String prompt,
        String content,
        LocalDateTime createdAt
) {

}
