package pl.apps.gptdemo.gptdemo.gpt_client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.apps.gptdemo.gptdemo.GptConfiguration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
class GptApiClientFactory {

    @Bean
    GptApiClient anthropicApiClient(GptConfiguration configuration) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return new AnthropicApiClient(configuration);
    }
}
