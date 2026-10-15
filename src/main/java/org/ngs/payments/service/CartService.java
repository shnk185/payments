package org.ngs.payments.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.ngs.payments.dto.CartItem;
import org.ngs.payments.dto.request.CartAddItemRequest;
import org.ngs.payments.dto.response.CartCheckoutResponse;
import org.ngs.payments.dto.response.CartWithItemsResponse;
import org.ngs.payments.dto.response.CreateCartResponse;
import org.ngs.payments.entity.CartEntity;
import org.ngs.payments.entity.InventoryEntity;
import org.ngs.payments.exception.ForbiddenOperationException;
import org.ngs.payments.exception.ResourceNotFoundException;
import org.ngs.payments.repository.CartItemRepository;
import org.ngs.payments.repository.CartRepository;
import org.ngs.payments.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;

    public CreateCartResponse createCart(long userId) {
        CartEntity cartEntity = CartEntity.builder().uniqueId(UUID.randomUUID().toString()).userId(userId).build();
        cartRepository.save(cartEntity);
        return CreateCartResponse.builder().id(cartEntity.getId()).uniqueId(cartEntity.getUniqueId()).build();
    }

    @Transactional
    public void addItemToCart(CartAddItemRequest cartAddItem, long userId, String cartId) {
        CartEntity cartEntity = cartRepository.findByUniqueIdAndDeletedFalse(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid cart id"));

        if (!cartEntity.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("Can only change your own cart");
        }

        InventoryEntity inventoryEntity = inventoryRepository.findByItemId(cartAddItem.getItemId());
        if (inventoryEntity.getAvailableQuantity() < cartAddItem.getQuantity()) {
            throw new ResourceNotFoundException("insufficient inventory");
        }

        cartItemRepository.upsertCartItem(cartEntity.getId(), cartAddItem.getItemId(), cartAddItem.getQuantity());
    }

    public CartWithItemsResponse fetchCartWithItems(long userId, String cartId) {
        CartEntity cartEntity = cartRepository.findByUniqueIdAndDeletedFalse(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid cart id"));

        if (!cartEntity.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("Can only fetch your own cart");
        }

        List<CartItem> cartItems = cartItemRepository.findCartItemsWithSelectedCountView(cartEntity.getId());
        cartItems.forEach(cartItem -> cartItem.setTotalCost(cartItem.getCost()
                .multiply(BigDecimal.valueOf(cartItem.getSelectedCount()))));

        return CartWithItemsResponse.builder().cartUniqueId(cartId).cartItems(cartItems).build();
    }


    @Transactional
    public CartCheckoutResponse checkoutCart(String cartId, long userId) {
        CartEntity cartEntity = cartRepository.findByUniqueIdAndDeletedFalse(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid cart id"));

        if (!cartEntity.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("Can only checkout your own cart");
        }
        List<CartItem> cartItems = cartItemRepository.findCartItemsWithSelectedCountView(cartEntity.getId());
        for (CartItem cartItem: cartItems) {
            int updated = inventoryRepository.reserveItemQuantity(cartItem.getId(), cartItem.getSelectedCount());
            if (updated == 0) {
                throw new ResourceNotFoundException("insufficient inventory for item with id " + cartItem.getId());
            }
        }
        return new CartCheckoutResponse();
    }
}
