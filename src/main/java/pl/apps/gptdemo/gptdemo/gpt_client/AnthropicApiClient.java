package pl.apps.gptdemo.gptdemo.gpt_client;

import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestClient;
import pl.apps.gptdemo.gptdemo.GptConfiguration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@CrossOrigin(originPatterns = "*")
class AnthropicApiClient implements GptApiClient {

    private static final String ANTHROPIC_BASE_USR = "https://api.anthropic.com/v1/messages";

    private final RestClient restClient;
    private final String apiKey;

    public AnthropicApiClient(GptConfiguration configuration) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this(
                configuration.getSkipSsl(),
                configuration.getApiKey(),
                configuration.getProxyHost(),
                configuration.getProxyPort()
        );
    }

    private AnthropicApiClient(Boolean skipSSL, String apiKey, String proxyHost, String proxyPort) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        this.apiKey = apiKey;
        var builder = RestClient.builder();
        if (skipSSL) {
            HttpComponentsClientHttpRequestFactory requestFactory = prepareSSLSkipRequestFactory(proxyHost, proxyPort);
            builder.requestFactory(requestFactory);
        }

        this.restClient = builder.build();

    }

    @Override
    public ClaudeApiClientResponse sendRequest(ClaudeApiClientRequest data) {
        try {

            return restClient.post()
                    .uri(ANTHROPIC_BASE_USR)
                    .headers(headers -> {
                        headers.set("x-api-key", apiKey);
                        headers.set("anthropic-version", "2023-06-01");
                        headers.setContentType(MediaType.APPLICATION_JSON);
                    })
                    .body(data)
                    .retrieve()
                    .body(ClaudeApiClientResponse.class);
        } catch (Exception e) {
            throw e;
        }
    }

}
