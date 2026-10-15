package org.ngs.payments.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ngs.payments.dto.CartItem;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartWithItemsResponse {
    private String cartUniqueId;
    private List<CartItem> cartItems;
}
