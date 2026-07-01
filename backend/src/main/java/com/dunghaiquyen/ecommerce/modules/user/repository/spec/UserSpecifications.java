package com.dunghaiquyen.ecommerce.modules.user.repository.spec;

import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserStatus;
import org.springframework.data.jpa.domain.Specification;

/** Filters for GET /api/v1/admin/users. */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    /** Explicit "match everything" base - every filter below is optional, so the list needs a true anchor to .and() onto. */
    public static Specification<User> all() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** Matches fullName, email, or phone (phone is nullable - LIKE against NULL simply never matches). */
    public static Specification<User> keywordMatches(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(root.get("phone"), "%" + keyword + "%"));
        };
    }
}
