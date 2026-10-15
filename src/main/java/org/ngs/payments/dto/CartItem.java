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
public class CartItem {
    private Long id;
    private String name;
    private BigDecimal cost;
    private Currency currency;
    private Long sellerId;
    private Integer selectedCount;
    private BigDecimal totalCost;

    public CartItem(Long id, String name, BigDecimal cost, Currency currency, Long sellerId, Integer selectedCount) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.currency = currency;
        this.sellerId = sellerId;
        this.selectedCount = selectedCount;
    }
}
