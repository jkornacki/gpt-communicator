package pl.apps.gptdemo.gptdemo.gpt_client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClaudeApiClientResponse(
        String type,
        String role,
        String model,
        Usage usage,
        List<ResponseContent> content
) {
    public String getFirstContentText() {
        if (content == null || content.isEmpty()) {
            return null;
        }
        return content.getFirst().text();
    }

    public record ResponseContent(
            String type,
            String text
    ) {

    }

    public record Usage(
            @JsonProperty("input_tokens")
            int inputTokens,
            @JsonProperty("output_tokens")
            int outputTokens
    ) {

    }
}
