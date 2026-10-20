package org.ngs.payments.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PspWebhookSessionData {
    private String object;
    private String id;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String lastErrorCode;
    private String lastErrorMessage;
}
