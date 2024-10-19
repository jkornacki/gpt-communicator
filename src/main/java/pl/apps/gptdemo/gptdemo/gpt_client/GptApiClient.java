package pl.apps.gptdemo.gptdemo.gpt_client;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;

public interface GptApiClient {

    String sendSimpleMessage(ChatMessage... messages);

    String sendMessage(String prompt, ChatMemory chatMemory, String systemPrompt);

    String sendMessage(String prompt, String systemPrompt);

    String sendTitleGenerationMessage(String prompt);
}
