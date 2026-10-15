package org.ngs.payments.dto;

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
public class ItemWithInventoryView {
    private Long id;
    private String name;
    private BigDecimal cost;
    private Currency currency;
    private Long sellerId;
    private Integer availableCount;
}
