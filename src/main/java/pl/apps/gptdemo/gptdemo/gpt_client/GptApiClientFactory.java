package pl.apps.gptdemo.gptdemo.gpt_client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.apps.gptdemo.gptdemo.GptConfiguration;

@Configuration
class GptApiClientFactory {

    @Bean
    GptApiClient anthropicApiClient(GptConfiguration configuration) {
        return new AnthropicLangChain4jApiClient(configuration);
    }
}
