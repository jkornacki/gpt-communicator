package pl.apps.gptdemo.gptdemo.gpt_client;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Objects;

public interface GptApiClient {

    default HttpComponentsClientHttpRequestFactory prepareSSLSkipRequestFactory(String proxyHost, String proxyPort) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(csf)
                .build();

        var httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager);
        if (Objects.nonNull(proxyHost) && Objects.nonNull(proxyPort)) {
            httpClientBuilder
                    .setProxy(new HttpHost(proxyHost, proxyPort));
        }
        CloseableHttpClient httpClient = httpClientBuilder.build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    ClaudeApiClientResponse sendRequest(ClaudeApiClientRequest data);
}
