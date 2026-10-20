package org.ngs.payments.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payments.webhook")
public class WebhookConfig {
    private String secret;
    private long toleranceSeconds = 300;
}
