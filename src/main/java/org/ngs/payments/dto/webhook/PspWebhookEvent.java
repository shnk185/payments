package org.ngs.payments.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PspWebhookEvent {
    private String id;
    private String type;
    private long created;
    private PspWebhookSessionData data;
}
