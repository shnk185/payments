package org.ngs.payments.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartAddItemRequest {
    private Long itemId;
    private Integer quantity;
}
