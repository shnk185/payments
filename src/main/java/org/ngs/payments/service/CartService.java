package org.ngs.payments.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.ngs.payments.dto.CartItem;
import org.ngs.payments.dto.SellerOrderGroup;
import org.ngs.payments.dto.request.CartAddItemRequest;
import org.ngs.payments.dto.response.CartCheckoutResponse;
import org.ngs.payments.dto.response.CartWithItemsResponse;
import org.ngs.payments.dto.response.CreateCartResponse;
import org.ngs.payments.entity.CartEntity;
import org.ngs.payments.entity.InventoryEntity;
import org.ngs.payments.entity.PaymentEntity;
import org.ngs.payments.entity.PaymentOrderEntity;
import org.ngs.payments.entity.PaymentOrderItemEntity;
import org.ngs.payments.enums.Currency;
import org.ngs.payments.enums.PaymentStatus;
import org.ngs.payments.exception.ForbiddenOperationException;
import org.ngs.payments.exception.InvalidRequestException;
import org.ngs.payments.exception.ResourceNotFoundException;
import org.ngs.payments.repository.CartItemRepository;
import org.ngs.payments.repository.CartRepository;
import org.ngs.payments.repository.InventoryRepository;
import org.ngs.payments.repository.PaymentOrderItemRepository;
import org.ngs.payments.repository.PaymentOrderRepository;
import org.ngs.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private static final Duration PAYMENT_EXPIRY = Duration.ofMinutes(10);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentOrderItemRepository paymentOrderItemRepository;

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
    public CartCheckoutResponse checkoutCart(String cartId, long userId, String idempotencyKey) {
        PaymentEntity existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existingPayment != null) {
            return CartCheckoutResponse.builder().uniqueId(cartId).paymentUUID(existingPayment.getUniqueId())
                    .cartCost(existingPayment.getAmount()).build();
        }

        CartEntity cartEntity = cartRepository.findByUniqueIdAndDeletedFalse(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid cart id"));
        if (!cartEntity.getUserId().equals(userId)) {
            throw new ForbiddenOperationException("Can only checkout your own cart");
        }

        List<CartItem> cartItems = cartItemRepository.findCartItemsWithSelectedCountView(cartEntity.getId());
        if (cartItems.isEmpty()) {
            throw new InvalidRequestException("Cart is empty");
        }

        List<SellerOrderGroup> sellerOrders = groupOrdersBySellers(cartItems);
        BigDecimal amount = sellerOrders.stream().map(SellerOrderGroup::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Currency currency = cartItems.get(0).getCurrency();

        PaymentEntity payment = PaymentEntity.builder().cartId(cartEntity.getId()).buyerId(userId)
                .uniqueId(UUID.randomUUID().toString()).amount(amount).currency(currency).status(PaymentStatus.NOT_STARTED)
                .idempotencyKey(idempotencyKey).expiresAt(Instant.now().plus(PAYMENT_EXPIRY)).build();
        paymentRepository.save(payment);
        for (SellerOrderGroup sellerOrder : sellerOrders) {
            PaymentOrderEntity paymentOrder = PaymentOrderEntity.builder().paymentId(payment.getId())
                    .sellerId(sellerOrder.getSellerId()).subtotal(sellerOrder.getSubtotal()).currency(currency)
                    .status(PaymentStatus.NOT_STARTED).build();
            paymentOrderRepository.save(paymentOrder);
            for (CartItem cartItem : sellerOrder.getItems()) {
                PaymentOrderItemEntity paymentOrderItem = PaymentOrderItemEntity.builder()
                        .paymentOrderId(paymentOrder.getId()).itemId(cartItem.getId()).name(cartItem.getName())
                        .costPerUnit(cartItem.getCost()).currency(cartItem.getCurrency())
                        .quantity(cartItem.getSelectedCount()).lineTotal(cartItem.getTotalCost()).build();
                paymentOrderItemRepository.save(paymentOrderItem);
            }
        }

        for (CartItem cartItem: cartItems) {
            int updated = inventoryRepository.reserveItemQuantity(cartItem.getId(), cartItem.getSelectedCount());
            if (updated == 0) {
                throw new ResourceNotFoundException("insufficient inventory for item with id " + cartItem.getId());
            }
        }
        return CartCheckoutResponse.builder().uniqueId(cartId).paymentUUID(payment.getUniqueId()).cartCost(amount)
                .build();
    }

    private static List<SellerOrderGroup> groupOrdersBySellers(List<CartItem> cartItems) {
        Map<Long, List<CartItem>> linesBySeller = cartItems.stream()
                .collect(Collectors.groupingBy(CartItem::getSellerId, LinkedHashMap::new, Collectors.toList()));

        List<SellerOrderGroup> sellerOrders = new ArrayList<>();

        for (Map.Entry<Long, List<CartItem>> entry : linesBySeller.entrySet()) {
            BigDecimal subtotal = BigDecimal.ZERO;
            for (CartItem cartItem : entry.getValue()) {
                BigDecimal lineTotal = cartItem.getCost().multiply(BigDecimal.valueOf(cartItem.getSelectedCount()));
                cartItem.setTotalCost(lineTotal);
                subtotal = subtotal.add(lineTotal);
            }
            sellerOrders.add(new SellerOrderGroup(entry.getKey(), entry.getValue(), subtotal));
        }
        return sellerOrders;
    }
}
