package com.dunghaiquyen.ecommerce.modules.collection.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.collection.dto.PublicCollectionDetailResponse;
import com.dunghaiquyen.ecommerce.modules.collection.dto.PublicCollectionResponse;
import com.dunghaiquyen.ecommerce.modules.collection.service.CollectionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public catalog read - see SecurityConfig.PUBLIC_GET_ENDPOINTS. */
@RestController
@RequestMapping("/api/v1/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public ApiResponse<List<PublicCollectionResponse>> list() {
        return ApiResponse.ok(collectionService.listPublic());
    }

    @GetMapping("/{slug}")
    public ApiResponse<PublicCollectionDetailResponse> detail(@PathVariable String slug) {
        return ApiResponse.ok(collectionService.getPublicDetail(slug));
    }
}
