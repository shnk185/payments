package org.ngs.payments.repository;

import org.ngs.payments.dto.CartItem;
import org.ngs.payments.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findByCartId(Long id);


    @Query("SELECT new org.ngs.payments.dto.CartItem(i.id, i.name, i.cost, i.currency, i.sellerId, ci.selectedCount) " +
            "from CartItemEntity ci " +
            "INNER JOIN ItemEntity i " +
            "on ci.itemId = i.id " +
            "where ci.cartId = :cartId")
    List<CartItem> findCartItemsWithSelectedCountView(Long cartId);


    @Modifying
    @Query(value = """
        INSERT INTO cart_items (cart_id, item_id, selected_count, created_at, updated_at, deleted)
        VALUES (:cartId, :itemId, :quantity, now(), now(), false)
        ON DUPLICATE KEY UPDATE selected_count = :quantity, updated_at = now()
    """, nativeQuery = true)
    void upsertCartItem(@Param("cartId") Long cartId,
                        @Param("itemId") Long itemId,
                        @Param("quantity") int quantity);
}
