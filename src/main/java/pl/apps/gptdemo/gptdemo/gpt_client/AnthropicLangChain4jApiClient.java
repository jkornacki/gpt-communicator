package pl.apps.gptdemo.gptdemo.gpt_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.internal.api.AnthropicApi;
import dev.langchain4j.model.anthropic.internal.client.DefaultAnthropicClient;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import okhttp3.OkHttpClient;
import org.joor.Reflect;
import org.springframework.web.bind.annotation.CrossOrigin;
import pl.apps.gptdemo.gptdemo.GptConfiguration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Objects;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

@CrossOrigin(originPatterns = "*")
class AnthropicLangChain4jApiClient implements GptApiClient {

    private static final String ANTHROPIC_BASE_USR = "https://api.anthropic.com/v1/";

    private final ChatLanguageModel model;
    private final GptConfiguration configuration;

    public AnthropicLangChain4jApiClient(GptConfiguration configuration) {

        this.model = AnthropicChatModel.builder()
                .apiKey(configuration.getApiKey())
                .modelName(configuration.getDefaultAnthropicModel())
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofSeconds(configuration.getAnthropicTimeoutInSec()))
                .maxTokens(configuration.getAnthropicMaxTokens())
                .temperature(configuration.getAnthropicTemperature())
                .build();
        if (configuration.getSkipSsl()) {
            var apiUrl = ANTHROPIC_BASE_USR;
            var insecureOkHttpClient = createInsecureOkHttpClient(
                    configuration.getProxyHost(),
                    configuration.getProxyPort(),
                    configuration.getAnthropicTimeoutInSec()
            );
            var client = DefaultAnthropicClient.builder()
                    .baseUrl(apiUrl)
                    .apiKey(configuration.getApiKey())
                    .version("2023-06-01")
                    .beta("tools-2024-04-04")
                    .timeout(Duration.ofSeconds(configuration.getAnthropicTimeoutInSec()))
                    .logRequests(true)
                    .logResponses(true)
                    .build();
            ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(INDENT_OUTPUT);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(apiUrl)
                    .client(insecureOkHttpClient)
                    .addConverterFactory(JacksonConverterFactory.create(OBJECT_MAPPER))
                    .build();
            AnthropicApi anthropicApi = retrofit.create(AnthropicApi.class);
            Reflect.on(client).set("okHttpClient", insecureOkHttpClient);
            Reflect.on(client).set("anthropicApi", anthropicApi);
            Reflect.on(model).set("client", client);
        }

        this.configuration = configuration;
    }

    public interface AiAssistant {
        String chat(String message);
    }

    @Override
    public String sendSimpleMessage(ChatMessage... messages) {
        var aiMessageResponse = model.generate(messages);
        return aiMessageResponse.content().text();
    }

    @Override
    public String sendMessage(String prompt, String systemPrompt) {
        var singleMessageMemory = prepareEmptyMemory();
        return sendMessage(prompt, singleMessageMemory, systemPrompt);
    }

    @Override
    public String sendMessage(String prompt, ChatMemory chatMemory, String systemPrompt) {

        String sysPrompt;
        if (Objects.isNull(systemPrompt) || systemPrompt.isBlank()) {
            sysPrompt = configuration.getAnthropicSystemPrompt();
        } else {
            sysPrompt = systemPrompt;
        }
        AiAssistant aiAssistant = AiServices.builder(AiAssistant.class)
                .chatLanguageModel(model)
                .chatMemory(chatMemory)
                .systemMessageProvider(o -> sysPrompt)
                .build();

        return aiAssistant.chat(prompt);
    }

    @Override
    public String sendTitleGenerationMessage(String prompt) {
        AiAssistant aiAssistant = AiServices.builder(AiAssistant.class)
                .chatLanguageModel(model)
                .chatMemory(prepareEmptyMemory())
                .systemMessageProvider(o -> configuration.getAnthropicSystemTitlePrompt())
                .build();

        return aiAssistant.chat(prompt);
    }

    private static MessageWindowChatMemory prepareEmptyMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(3) // 1  user, 1 system, 1 ai
                .chatMemoryStore(new InMemoryChatMemoryStore())
                .build();
    }

    private static OkHttpClient createInsecureOkHttpClient(String proxyHost,
                                                           String proxyPort,
                                                           int anthropicTimeoutInSec) {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            var builder = new OkHttpClient.Builder();
            // Konfiguracja proxy
            if (proxyHost != null && !proxyHost.isBlank() && proxyPort != null && !proxyPort.isBlank()) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                builder.proxy(proxy);
            }

            builder.callTimeout(Duration.ofSeconds(anthropicTimeoutInSec));
            builder.readTimeout(Duration.ofSeconds(anthropicTimeoutInSec));

            return builder
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
