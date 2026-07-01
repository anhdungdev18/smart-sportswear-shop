package com.dunghaiquyen.ecommerce.modules.audit.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.audit.dto.AdminAuditLogListQuery;
import com.dunghaiquyen.ecommerce.modules.audit.dto.AuditLogResponse;
import com.dunghaiquyen.ecommerce.modules.audit.service.AuditLogService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ADMIN only - stricter than other admin-read views (a full action audit trail is more sensitive than e.g. order history). */
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    public AdminAuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AuditLogResponse>> list(@ModelAttribute AdminAuditLogListQuery query) {
        AuditLogService.ListResult result = auditLogService.list(query);
        return ApiResponse.ok(result.items(), result.meta());
    }
}
