package org.ngs.payments.repository;

import org.ngs.payments.dto.ItemWithInventoryView;
import org.ngs.payments.entity.InventoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    @Query("""
    SELECT new org.ngs.payments.dto.ItemWithInventoryView(
        i.id, i.name, i.cost, i.currency, i.sellerId, inv.availableQuantity)
    FROM ItemEntity i
    LEFT JOIN InventoryEntity inv ON inv.itemId = i.id
    WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    Page<ItemWithInventoryView> findItemsWithInventory(@Param("name") String name, Pageable pageable);
}
