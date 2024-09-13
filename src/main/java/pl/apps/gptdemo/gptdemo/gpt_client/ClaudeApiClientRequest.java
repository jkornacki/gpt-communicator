package pl.apps.gptdemo.gptdemo.gpt_client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record ClaudeApiClientRequest(
        @JsonProperty("model")
        String model,
        @JsonProperty("max_tokens")
        Integer maxTokens,
        @JsonProperty("system")
        String system,
        @JsonProperty("messages")
        List<Message> messages

) {

    public static ClaudeApiClientRequest createNewMessage(String prompt, String systemPrompt) {
        return ClaudeApiClientRequest.builder()
                .model("claude-3-5-sonnet-20240620")
                .maxTokens(1024)
                .system(systemPrompt)
                .messages(List.of(
                        UserMessage.builder()
                                .content(prompt)
                                .build()
                ))
                .build();
    }


    public static ClaudeApiClientRequest createPromptForTitleCreate(String prompt, String firstPromptResponse, String systemPrompt) {
        return ClaudeApiClientRequest.builder()
                .model("claude-3-5-sonnet-20240620")
                .maxTokens(512)
                .system(systemPrompt)
                .messages(List.of(
                        UserMessage.builder()
                                .content(firstPromptResponse)
                                .build()
//                        ,AssistantMessage.builder()
//                                .content(firstPromptResponse)
//                                .build()
                ))
                .build();
    }

    @Builder
    public record UserMessage(
            String content
    ) implements Message {

        @Override
        public String role() {
            return "user";
        }
    }

    @Builder
    public record AssistantMessage(
            String content
    ) implements Message {

        @Override
        public String role() {
            return "assistant";
        }
    }

    public interface Message {
        @JsonProperty("role")
        String role();

        @JsonProperty("content")
        String content();
    }
}
