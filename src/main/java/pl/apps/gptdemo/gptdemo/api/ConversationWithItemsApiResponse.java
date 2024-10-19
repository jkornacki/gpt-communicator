package pl.apps.gptdemo.gptdemo.api;

import java.util.List;

public record ConversationWithItemsApiResponse(
        Long conversationId,
        String conversationTitle,
        String systemPrompt,
        List<ConversationItemDto> items
) {

}
