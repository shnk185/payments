package org.ngs.payments.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartAddItemRequest {
    @NotNull(message = "Item id is required")
    private Long itemId;
    @Min(1)
    private Integer quantity;
}
