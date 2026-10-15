package org.ngs.payments.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.ngs.payments.dto.ItemWithInventoryView;
import org.ngs.payments.dto.request.ItemCreateRequest;
import org.ngs.payments.dto.response.ItemCreateResponse;
import org.ngs.payments.dto.response.PageResponse;
import org.ngs.payments.entity.InventoryEntity;
import org.ngs.payments.entity.ItemEntity;
import org.ngs.payments.repository.InventoryRepository;
import org.ngs.payments.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    @PreAuthorize("hasRole('SELLER')")
    public ItemCreateResponse createItem(ItemCreateRequest itemCreateRequest, long userId) {
        ItemEntity itemEntity = ItemEntity.builder()
                .cost(itemCreateRequest.getCostPerUnit())
                .currency(itemCreateRequest.getCurrency())
                .sellerId(userId)
                .name(itemCreateRequest.getName())
                .build();

        itemRepository.save(itemEntity);
        InventoryEntity inventoryEntity = InventoryEntity.builder()
                .availableQuantity(itemCreateRequest.getQuantity())
                .reservedQuantity(0)
                .itemId(itemEntity.getId())
                .build();

        inventoryRepository.save(inventoryEntity);

        return ItemCreateResponse.builder()
                .costPerUnit(itemEntity.getCost())
                .currency(itemEntity.getCurrency())
                .id(itemEntity.getId())
                .name(itemEntity.getName())
                .quantity(inventoryEntity.getAvailableQuantity())
                .build();
    }

    public PageResponse<ItemWithInventoryView> fetchItemsByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<ItemWithInventoryView> items = inventoryRepository.findItemsWithInventory(name, pageable);

        return PageResponse.<ItemWithInventoryView>builder()
                .content(items.stream().toList())
                .last(items.isLast())
                .page(items.getNumber())
                .size(items.getSize())
                .totalElements(items.getTotalElements())
                .totalPages(items.getTotalPages())
                .build();
    }

}
