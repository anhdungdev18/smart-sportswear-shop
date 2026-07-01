package com.dunghaiquyen.ecommerce.modules.audit.service;

import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.modules.audit.dto.AdminAuditLogListQuery;
import com.dunghaiquyen.ecommerce.modules.audit.dto.AuditLogResponse;
import com.dunghaiquyen.ecommerce.modules.audit.entity.AuditLog;
import com.dunghaiquyen.ecommerce.modules.audit.repository.AuditLogRepository;
import com.dunghaiquyen.ecommerce.modules.audit.repository.spec.AuditLogSpecifications;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * GET /api/v1/admin/audit-logs (read side) is wired up and works for
 * anything written through {@link #record}. The WRITE side is deliberately
 * NOT retrofitted across every pre-existing admin action this phase (order
 * status changes, product/coupon/promotion CRUD, etc.) - doing so would mean
 * touching many already-shipped, already-tested services just to add a
 * logging call, which is exactly the "refactor lan man ngoài phạm vi" this
 * whole effort was told to avoid. The first real callers are the brand-new
 * Return/Refund admin actions (ReturnService) written in this same phase,
 * where adding the call costs nothing extra in review/blast-radius terms
 * since that code is new anyway. Retrofitting older modules is a deliberate,
 * separate future phase - see the final report's risks section.
 *
 * <p>Never throws: a failed audit write must not block the admin action it
 * is recording (same "infrastructure concern must not break the business
 * action" stance NotificationService already takes for failed emails).
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public record ListResult(List<AuditLogResponse> items, PageMeta meta) {
    }

    @Transactional
    public void record(User actor, String action, String entityType, String entityId, Object before, Object after) {
        try {
            AuditLog entry = new AuditLog();
            entry.setActorUser(actor);
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setBeforeJson(toMap(before));
            entry.setAfterJson(toMap(after));
            entry.setIpAddress(currentRequestIp());
            entry.setUserAgent(currentRequestUserAgent());
            auditLogRepository.save(entry);
        } catch (RuntimeException ex) {
            log.warn("Audit log write failed action={} entityType={} entityId={}: {}", action, entityType, entityId, ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ListResult list(AdminAuditLogListQuery query) {
        Specification<AuditLog> spec = Specification.where(null);
        if (query.actorUserId() != null) {
            spec = spec.and(AuditLogSpecifications.byActor(query.actorUserId()));
        }
        if (query.entityType() != null) {
            spec = spec.and(AuditLogSpecifications.byEntityType(query.entityType()));
        }
        if (query.entityId() != null) {
            spec = spec.and(AuditLogSpecifications.byEntityId(query.entityId()));
        }
        if (query.action() != null) {
            spec = spec.and(AuditLogSpecifications.byAction(query.action()));
        }
        Pageable pageable = PageRequest.of(
                resolvePageIndex(query.page()), resolveLimit(query.limit()), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);
        List<AuditLogResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return new ListResult(items, PageMeta.from(page));
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> toMap(Object value) {
        if (value == null) {
            return null;
        }
        return objectMapper.convertValue(value, java.util.Map.class);
    }

    private String currentRequestIp() {
        var attrs = currentRequestAttributes();
        return attrs != null ? attrs.getRequest().getRemoteAddr() : null;
    }

    private String currentRequestUserAgent() {
        var attrs = currentRequestAttributes();
        return attrs != null ? attrs.getRequest().getHeader("User-Agent") : null;
    }

    private ServletRequestAttributes currentRequestAttributes() {
        var attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes servletAttrs ? servletAttrs : null;
    }

    private int resolvePageIndex(Integer page) {
        return (page != null && page > 0) ? page - 1 : 0;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private AuditLogResponse toResponse(AuditLog entry) {
        return new AuditLogResponse(
                entry.getId(),
                entry.getActorUser() != null ? entry.getActorUser().getId() : null,
                entry.getActorUser() != null ? entry.getActorUser().getFullName() : null,
                entry.getAction(),
                entry.getEntityType(),
                entry.getEntityId(),
                entry.getBeforeJson(),
                entry.getAfterJson(),
                entry.getIpAddress(),
                entry.getUserAgent(),
                entry.getCreatedAt());
    }
}
