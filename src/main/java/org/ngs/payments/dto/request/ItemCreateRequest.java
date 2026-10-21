package org.ngs.payments.dto.request;

import jakarta.validation.constraints.*;
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
    @Size(max = 255)
    @NotBlank(message = "Item name is required")
    private String name;
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    @NotNull(message = "Currency is required")
    private Currency currency;
    @Positive
    @NotNull(message = "Cost per unit is required")
    private BigDecimal costPerUnit;
}
