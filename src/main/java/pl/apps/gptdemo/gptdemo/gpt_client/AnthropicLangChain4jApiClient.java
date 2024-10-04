package pl.apps.gptdemo.gptdemo.gpt_client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModelName;
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

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

@CrossOrigin(originPatterns = "*")
class AnthropicLangChain4jApiClient implements GptApiClient {

    private static final String ANTHROPIC_BASE_USR = "https://api.anthropic.com/v1/messages";

    private final ChatLanguageModel model;
    private final GptConfiguration configuration;

    public AnthropicLangChain4jApiClient(GptConfiguration configuration) {
        this(
                configuration.getSkipSsl(),
                configuration.getApiKey(),
                configuration.getProxyHost(),
                configuration.getProxyPort(),
                configuration
        );
    }

    private AnthropicLangChain4jApiClient(Boolean skipSSL,
                                          String apiKey,
                                          String proxyHost,
                                          String proxyPort,
                                          GptConfiguration configuration
    ) {

        this.model = AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(AnthropicChatModelName.CLAUDE_3_5_SONNET_20240620)
                .logRequests(true)
                .logResponses(true)
                .build();
        if (skipSSL) {
            var apiUrl = ANTHROPIC_BASE_USR;
            var insecureOkHttpClient = createInsecureOkHttpClient(proxyHost, proxyPort);
            var client = DefaultAnthropicClient.builder()
                    .baseUrl(apiUrl)
                    .apiKey(apiKey)
                    .version("2023-06-01")
                    .beta("tools-2024-04-04")
                    .timeout(Duration.ofSeconds(20))
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
    public String sendMessage(String prompt) {
        var singleMessageMemory = prepareEmptyMemory();
        return sendMessage(prompt, singleMessageMemory);
    }

    @Override
    public String sendMessage(String prompt, ChatMemory chatMemory) {
        AiAssistant aiAssistant = AiServices.builder(AiAssistant.class)
                .chatLanguageModel(model)
                .chatMemory(chatMemory)
                .systemMessageProvider(o -> configuration.getAnthropicSystemPrompt())
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
                                                           String proxyPort) {
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

            // Konfiguracja proxy
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .proxy(proxy)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
