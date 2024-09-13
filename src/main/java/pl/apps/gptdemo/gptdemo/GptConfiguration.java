package pl.apps.gptdemo.gptdemo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Component
public class GptConfiguration {

    private final Boolean skipSsl;
    private final String apiKey;
    private final String proxyHost;
    private final String proxyPort;

    public GptConfiguration(
            @Value("${app.skipSSL:false}") Boolean skipSsl,
            @Value("${app.anthropic.apikey}") String apiKey,
            @Value("${app.proxyHost:#{null}}") String proxyHost,
            @Value("${app.proxyPort:#{null}}") String proxyPort
    ) {
        log.info("SkipSSL: {}", skipSsl);
        log.info("Proxy: host: {} port: {}", proxyHost, proxyPort);
        this.skipSsl = skipSsl;
        this.apiKey = apiKey;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
}
