package org.ngs.payments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.payments.dto.request.CartAddItemRequest;
import org.ngs.payments.dto.response.CartCheckoutResponse;
import org.ngs.payments.dto.response.CartWithItemsResponse;
import org.ngs.payments.dto.response.CreateCartResponse;
import org.ngs.payments.service.CartService;
import org.ngs.payments.util.IdempotencyUtil;
import org.ngs.payments.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CreateCartResponse> createCart() {
        long userId = SecurityUtils.currentUserId();
        log.info("received create cart request {}", userId);
        CreateCartResponse createCartResponse = cartService.createCart(userId);
        log.info("cart create response {}", createCartResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(createCartResponse);
    }

    @PutMapping("/{cartId}/items")
    public ResponseEntity<Void> addItemToCart(@RequestBody CartAddItemRequest cartAddItem, @PathVariable String cartId) {
        long userId = SecurityUtils.currentUserId();
        log.info("received add item to cart request {} {} {}", cartAddItem, userId, cartId);
        cartService.addItemToCart(cartAddItem, userId, cartId);
        log.info("added item to cart {} {}", cartAddItem, cartId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cartId}/items")
    public ResponseEntity<CartWithItemsResponse> fetchCartWithItems(@PathVariable String cartId) {
        long userId = SecurityUtils.currentUserId();
        log.info("received fetch cart items request {} {}", cartId, userId);
        CartWithItemsResponse cartWithItemsResponse = cartService.fetchCartWithItems(userId, cartId);
        log.info("cart fetch response {}", cartWithItemsResponse);
        return ResponseEntity.ok(cartWithItemsResponse);
    }

    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<CartCheckoutResponse> checkoutCart(@PathVariable String cartId,
                                                            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        long userId = SecurityUtils.currentUserId();
        log.info("received cart checkout request {} {} {}", cartId, userId, idempotencyKey);
        CartCheckoutResponse cartCheckoutResponse = IdempotencyUtil.executeIdempotentMethod(
                () -> cartService.checkoutCart(cartId, userId, idempotencyKey),
                () -> cartService.checkoutCartIdempotentFallback(idempotencyKey, cartId));
        log.info("cart checkout response {}", cartCheckoutResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartCheckoutResponse);
    }
}
