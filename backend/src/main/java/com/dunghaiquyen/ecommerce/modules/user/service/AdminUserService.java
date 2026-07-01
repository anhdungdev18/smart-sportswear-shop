package com.dunghaiquyen.ecommerce.modules.user.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.common.response.PageMeta;
import com.dunghaiquyen.ecommerce.modules.user.dto.AdminUserListQuery;
import com.dunghaiquyen.ecommerce.modules.user.dto.AdminUserResponse;
import com.dunghaiquyen.ecommerce.modules.user.dto.UpdateUserRoleRequest;
import com.dunghaiquyen.ecommerce.modules.user.dto.UpdateUserStatusRequest;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserStatus;
import com.dunghaiquyen.ecommerce.modules.user.mapper.UserMapper;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import com.dunghaiquyen.ecommerce.modules.user.repository.spec.UserSpecifications;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin user management (PHASE1_SPEC.md 6.2: "Xem danh sách user / Xem chi
 * tiết user / Khóa-mở khóa user / Gán role") - deliberately its own service
 * rather than folded into the self-service {@link UserService}, matching that
 * class's own javadoc ("Admin user management... is a separate concern").
 */
@Service
public class AdminUserService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AdminUserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public record ListResult<T>(List<T> items, PageMeta meta) {
    }

    @Transactional(readOnly = true)
    public ListResult<AdminUserResponse> listUsers(AdminUserListQuery query) {
        Specification<User> spec = UserSpecifications.all();
        if (query.role() != null) {
            spec = spec.and(UserSpecifications.hasRole(query.role()));
        }
        if (query.status() != null) {
            spec = spec.and(UserSpecifications.hasStatus(query.status()));
        }
        if (query.keyword() != null && !query.keyword().isBlank()) {
            spec = spec.and(UserSpecifications.keywordMatches(query.keyword().trim()));
        }
        Pageable pageable = PageRequest.of(
                resolvePageIndex(query.page()), resolveLimit(query.limit()), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> page = userRepository.findAll(spec, pageable);
        List<AdminUserResponse> items = page.getContent().stream().map(userMapper::toAdminResponse).toList();
        return new ListResult<>(items, PageMeta.from(page));
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserDetail(UUID userId) {
        return userMapper.toAdminResponse(findUser(userId));
    }

    /**
     * Idempotent: re-sending the status the user already has is a no-op success,
     * not an error - "trạng thái update phải idempotent/hợp lý". The self-lock
     * guard only fires when an actual ACTIVE->LOCKED change on one's own account
     * is being attempted, so it never interferes with the idempotent path.
     *
     * Self-lock guard is an ABSOLUTE rule (always blocked), not "only if you are
     * the last admin": counting other active admins would itself be a fresh
     * TOCTOU race (two admins each seeing "one other admin exists" and both
     * self-locking at once, leaving zero) that would need its own table-level
     * lock to close safely. An unconditional rule trivially implies the weaker
     * "never leave the system without an admin via self-lock" guarantee the
     * spec asks for, with no race to reason about at all.
     */
    @Transactional
    public AdminUserResponse updateStatus(UUID targetUserId, UpdateUserStatusRequest request, UUID actingAdminId) {
        User user = findUserForUpdate(targetUserId);
        if (user.getStatus() == request.status()) {
            return userMapper.toAdminResponse(user);
        }
        if (request.status() == UserStatus.LOCKED && targetUserId.equals(actingAdminId)) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot lock your own account");
        }
        user.setStatus(request.status());
        return userMapper.toAdminResponse(userRepository.save(user));
    }

    /**
     * Same idempotency + absolute-guard reasoning as {@link #updateStatus}: an
     * admin may freely re-confirm their own ADMIN role (no-op) but can never move
     * their OWN account away from ADMIN through this endpoint - moving a
     * DIFFERENT admin away from ADMIN is unrestricted, this only protects against
     * self-demotion.
     */
    @Transactional
    public AdminUserResponse updateRole(UUID targetUserId, UpdateUserRoleRequest request, UUID actingAdminId) {
        User user = findUserForUpdate(targetUserId);
        if (user.getRole() == request.role()) {
            return userMapper.toAdminResponse(user);
        }
        if (targetUserId.equals(actingAdminId) && request.role() != UserRole.ADMIN) {
            throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot change your own role away from ADMIN");
        }
        user.setRole(request.role());
        return userMapper.toAdminResponse(userRepository.save(user));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private User findUserForUpdate(UUID userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
}
