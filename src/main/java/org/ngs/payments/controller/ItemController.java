package org.ngs.payments.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngs.payments.dto.ItemWithInventoryView;
import org.ngs.payments.dto.request.ItemCreateRequest;
import org.ngs.payments.dto.response.ItemCreateResponse;
import org.ngs.payments.dto.response.PageResponse;
import org.ngs.payments.service.ItemService;
import org.ngs.payments.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemCreateResponse> createItems(@RequestBody ItemCreateRequest itemCreateRequest) {
        log.info("received create item request {}", itemCreateRequest);
        long userId = SecurityUtils.currentUserId();
        ItemCreateResponse itemCreateResponse = itemService.createItem(itemCreateRequest, userId);
        log.info("create item response {}", itemCreateResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemCreateResponse);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ItemWithInventoryView>> fetchItems(@RequestParam(required = false, defaultValue = "") String name,
                                                                          @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                          @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        log.info("received fetch items request {}", name);
        PageResponse<ItemWithInventoryView> itemFetchResponse = itemService.fetchItemsByName(name, page, size);
        log.info("fetch items response {}", itemFetchResponse);
        return ResponseEntity.ok(itemFetchResponse);
    }

}
