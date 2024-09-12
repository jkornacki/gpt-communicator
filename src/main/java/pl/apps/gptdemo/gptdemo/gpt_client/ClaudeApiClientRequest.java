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

    public static ClaudeApiClientRequest createNewMessage(String prompt) {
        return ClaudeApiClientRequest.builder()
                .model("claude-3-5-sonnet-20240620")
                .maxTokens(1024)
                .system("Jako asystent programisty przygotuj odpowiedzi na wskazane pytanie. Wszystkie odpowiedzi przygotuj w formacie Mark Down. Pamiętaj aby do wszystkich przykładów kodu dodaj ```<LANGUAGE> np ```SQL\nKOD```")
                .messages(List.of(
                        UserMessage.builder()
                                .content(prompt)
                                .build()
                ))
                .build();
    }


    public static ClaudeApiClientRequest createPromptForTitleCreate(String prompt, String firstPromptResponse) {
        return ClaudeApiClientRequest.builder()
                .model("claude-3-5-sonnet-20240620")
                .maxTokens(512)
                .system("Jako asystent przygotuj tytuł pasujący do przekazanego tekstu. " +
                        "Język w którym zostanie zwrócony tytuł dostosuj do języka w którym jest on przekazany. " +
                        "Tytuł może zawierać maksymalnie 75 znaków. " +
                        "Pamiętaj aby zmieścić się w tym limicie. Idealny tytuł to taki który składa się z 2-4 słów")
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
