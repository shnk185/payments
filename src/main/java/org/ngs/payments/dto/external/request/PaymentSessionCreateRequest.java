package org.ngs.payments.dto.external.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.payments.enums.Currency;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSessionCreateRequest {
    private BigDecimal amount;
    private Currency currency;
    private String successUrl;
    private String cancelUrl;
    private Map<String, String> metadata;
}
