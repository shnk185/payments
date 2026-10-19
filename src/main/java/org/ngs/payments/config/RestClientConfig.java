package org.ngs.payments.config;

import lombok.RequiredArgsConstructor;
import org.ngs.payments.config.properties.ApiConfig;
import org.ngs.payments.enums.ExternalApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    private final ApiConfig apiConfig;

    @Bean
    @Qualifier("createCheckoutSessionRestClient")
    public RestClient createCheckoutSessionRestClient(RestClient.Builder restClientBuilder) {
        ApiConfig.ServiceConfig serviceConfig = apiConfig.getServices().get(ExternalApi.PAYMENT_SERVICE_PROVIDER);
        ApiConfig.ServiceConfig.ClientConfig clientConfig = serviceConfig.getClients().get("CREATE_CHECKOUT_SESSION");
        HttpClientSettings settings = HttpClientSettings.defaults()
                .withConnectTimeout(Duration.ofMillis(clientConfig.getConnectTimeoutMs()))
                .withReadTimeout(Duration.ofMillis(clientConfig.getReadTimeoutMs()));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(settings);
        return restClientBuilder.baseUrl(serviceConfig.getBaseUrl()).requestFactory(requestFactory).build();
    }
}
