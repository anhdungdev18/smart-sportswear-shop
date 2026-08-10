package com.dunghaiquyen.ecommerce.modules.user.repository;

import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByRoleAndStatus(UserRole role, UserStatus status);

    /**
     * Row-locked read used by AdminUserService before a status/role mutation -
     * without it, two admins concurrently changing DIFFERENT fields on the SAME
     * target user (one locking, one re-assigning role) could lose one of the two
     * changes: both would read the same snapshot, and whichever saves last
     * overwrites the other's field with its own stale copy of it. Locking
     * serializes the read-modify-write so the second writer always starts from
     * the first writer's already-applied change.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);
}
