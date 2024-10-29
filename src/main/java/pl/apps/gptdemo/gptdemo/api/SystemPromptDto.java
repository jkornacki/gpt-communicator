package pl.apps.gptdemo.gptdemo.api;

import lombok.Builder;

@Builder
public record SystemPromptDto(
        Long conversationId,
        String prompt,
        boolean isCurrentConversationsPrompt,
        boolean isSystemPrompt
) {
}
