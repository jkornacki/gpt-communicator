package pl.apps.gptdemo.gptdemo.gpt_client;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;

public interface GptApiClient {

    String sendSimpleMessage(ChatMessage... messages);

    String sendMessage(String prompt);

    String sendMessage(String prompt, ChatMemory chatMemory);

    String sendTitleGenerationMessage(String prompt);
}
