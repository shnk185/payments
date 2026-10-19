package org.ngs.payments.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.payments.enums.ExternalApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "api.config")
public class ApiConfig {
    private Map<ExternalApi, ServiceConfig> services;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServiceConfig {
        private String baseUrl;
        private Map<String, ClientConfig> clients;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ClientConfig {
            private String apiPath;
            private Integer connectTimeoutMs;
            private Integer readTimeoutMs;
        }
    }
}
