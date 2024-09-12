package pl.apps.gptdemo.gptdemo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class GptConfiguration {

    private final Boolean skipSsl;
    private final String apiKey;
    private final String proxyHost;
    private final String proxyPort;

    public GptConfiguration(
            @Value("${app.skipSSL:false}") Boolean skipSsl,
            @Value("${app.anthropic.apikey}") String apiKey,
            @Value("${app.proxyHost:''}") String proxyHost,
            @Value("${app.proxyPort:''}") String proxyPort
    ) {
        this.skipSsl = skipSsl;
        this.apiKey = apiKey;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
}
