package pl.apps.gptdemo.gptdemo.api;

import java.util.List;

public record PromptApiResponse(
        Long conversationId,
        String conversationTitle,
        String firstPromptResponseMarkdown,
        List<ConversationItemDto> items
) {
}
