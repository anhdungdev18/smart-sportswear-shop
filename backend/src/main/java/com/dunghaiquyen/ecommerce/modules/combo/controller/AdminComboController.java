package com.dunghaiquyen.ecommerce.modules.combo.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.combo.dto.ComboRequest;
import com.dunghaiquyen.ecommerce.modules.combo.dto.ComboResponse;
import com.dunghaiquyen.ecommerce.modules.combo.service.ComboService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Combo management for ADMIN users. */
@RestController
@RequestMapping("/api/v1/admin/combos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminComboController {

    private final ComboService comboService;

    public AdminComboController(ComboService comboService) {
        this.comboService = comboService;
    }

    @GetMapping
    public ApiResponse<List<ComboResponse>> list() {
        return ApiResponse.ok(comboService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ComboResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(comboService.getById(id));
    }

    @PostMapping
    public ApiResponse<ComboResponse> create(@Valid @RequestBody ComboRequest request) {
        return ApiResponse.ok("Combo created", comboService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ComboResponse> update(@PathVariable UUID id, @Valid @RequestBody ComboRequest request) {
        return ApiResponse.ok("Combo updated", comboService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        comboService.delete(id);
        return ApiResponse.ok(null);
    }
}
