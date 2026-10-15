package org.ngs.payments.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.payments.enums.Currency;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemCreateRequest {
    private String name;
    private Integer quantity;
    private Currency currency;
    private BigDecimal costPerUnit;
}
