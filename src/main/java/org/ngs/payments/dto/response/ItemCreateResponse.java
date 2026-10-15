package org.ngs.payments.dto.response;


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
public class ItemCreateResponse {
    private Long id;
    private String name;
    private Currency currency;
    private Integer quantity;
    private BigDecimal costPerUnit;
}
