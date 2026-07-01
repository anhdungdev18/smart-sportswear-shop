package com.dunghaiquyen.ecommerce.modules.auth.repository;

import com.dunghaiquyen.ecommerce.modules.auth.entity.PasswordResetToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Atomic claim, same pattern as RefreshTokenRepository#revokeIfActive: the
     * usedAt/expiresAt check happens again inside this single UPDATE against
     * the row Postgres just locked, so two concurrent reset-password calls
     * with the same token cannot both pass - exactly one UPDATE matches
     * (returns 1), the other matches zero rows (returns 0). Re-checking
     * expiresAt here too (not just usedAt) means correctness never depends on
     * the caller having read-checked expiry moments earlier.
     */
    @Modifying
    @Query("update PasswordResetToken t set t.usedAt = :now where t.id = :id and t.usedAt is null and t.expiresAt > :now")
    int markUsedIfUnused(@Param("id") UUID id, @Param("now") Instant now);
}
