package com.dunghaiquyen.ecommerce.modules.collection.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.collection.dto.CollectionCreateRequest;
import com.dunghaiquyen.ecommerce.modules.collection.dto.CollectionResponse;
import com.dunghaiquyen.ecommerce.modules.collection.dto.CollectionUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.collection.service.CollectionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/collections")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCollectionController {

    private final CollectionService collectionService;

    public AdminCollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public ApiResponse<List<CollectionResponse>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit) {
        CollectionService.AdminListResult result = collectionService.listAdmin(page, limit);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CollectionResponse>> create(@Valid @RequestBody CollectionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Collection created", collectionService.create(request)));
    }

    @PatchMapping("/{id}")
    public ApiResponse<CollectionResponse> update(
            @PathVariable UUID id, @Valid @RequestBody CollectionUpdateRequest request) {
        return ApiResponse.ok("Collection updated", collectionService.update(id, request));
    }
}
