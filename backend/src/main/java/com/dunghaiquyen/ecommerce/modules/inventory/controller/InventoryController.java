package com.dunghaiquyen.ecommerce.modules.inventory.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.inventory.dto.AdjustStockRequest;
import com.dunghaiquyen.ecommerce.modules.inventory.dto.ExportStockRequest;
import com.dunghaiquyen.ecommerce.modules.inventory.dto.ImportStockRequest;
import com.dunghaiquyen.ecommerce.modules.inventory.dto.InventoryItemResponse;
import com.dunghaiquyen.ecommerce.modules.inventory.dto.InventoryListQuery;
import com.dunghaiquyen.ecommerce.modules.inventory.dto.InventoryTransactionListQuery;
import com.dunghaiquyen.ecommerce.modules.inventory.dto.InventoryTransactionResponse;
import com.dunghaiquyen.ecommerce.modules.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API_SPEC_PHASE1.md section 11 - admin/warehouse inventory management. */
@RestController
@RequestMapping("/api/v1/admin/inventory")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ApiResponse<List<InventoryItemResponse>> list(@ModelAttribute InventoryListQuery query) {
        InventoryService.ListResult<InventoryItemResponse> result = inventoryService.listInventory(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> importStock(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody ImportStockRequest request) {
        InventoryItemResponse response = inventoryService.importStock(request, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Stock imported", response));
    }

    @PostMapping("/export")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> exportStock(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody ExportStockRequest request) {
        InventoryItemResponse response = inventoryService.exportStock(request, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Stock exported", response));
    }

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> adjust(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody AdjustStockRequest request) {
        InventoryItemResponse response = inventoryService.adjustStock(request, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Stock adjusted", response));
    }

    @GetMapping("/transactions")
    public ApiResponse<List<InventoryTransactionResponse>> transactions(
            @ModelAttribute InventoryTransactionListQuery query) {
        InventoryService.ListResult<InventoryTransactionResponse> result = inventoryService.listTransactions(query);
        return ApiResponse.ok(result.items(), result.meta());
    }
}
