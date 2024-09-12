package pl.apps.gptdemo.gptdemo.api;

public record ConversationApiResponse(
        Long conversationId,
        String conversationTitle
) {
}
