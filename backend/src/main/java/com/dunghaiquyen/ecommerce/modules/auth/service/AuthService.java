package com.dunghaiquyen.ecommerce.modules.auth.service;

import com.dunghaiquyen.ecommerce.common.exception.AccountLockedException;
import com.dunghaiquyen.ecommerce.common.exception.EmailAlreadyExistsException;
import com.dunghaiquyen.ecommerce.common.exception.InvalidTokenException;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.common.security.JwtTokenProvider;
import com.dunghaiquyen.ecommerce.common.security.TokenHasher;
import com.dunghaiquyen.ecommerce.config.JwtProperties;
import com.dunghaiquyen.ecommerce.modules.auth.dto.AuthResponse;
import com.dunghaiquyen.ecommerce.modules.auth.dto.AuthTokensResponse;
import com.dunghaiquyen.ecommerce.modules.auth.dto.LoginRequest;
import com.dunghaiquyen.ecommerce.modules.auth.dto.RegisterRequest;
import com.dunghaiquyen.ecommerce.modules.auth.entity.RefreshToken;
import com.dunghaiquyen.ecommerce.modules.auth.repository.RefreshTokenRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserStatus;
import com.dunghaiquyen.ecommerce.modules.user.mapper.UserMapper;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh tokens are rotated on every use and the DB row is the source of
 * truth for revocation - the JWT signature alone is not enough to consider a
 * refresh token valid. Reuse of an already-revoked token is treated as a
 * possible theft signal and revokes every other active session for that user.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final TokenHasher tokenHasher;
    private final UserMapper userMapper;
    private final RefreshTokenRevocationService refreshTokenRevocationService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties,
            TokenHasher tokenHasher,
            UserMapper userMapper,
            RefreshTokenRevocationService refreshTokenRevocationService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.tokenHasher = tokenHasher;
        this.userMapper = userMapper;
        this.refreshTokenRevocationService = refreshTokenRevocationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        // Always CUSTOMER: RegisterRequest has no role field, so there is nothing for a client to override.
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // race: two concurrent registrations with the same email past the existsByEmail check
            throw new EmailAlreadyExistsException(email);
        }

        return new AuthResponse(userMapper.toResponse(user), issueTokens(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        // Delegates to CustomUserDetailsService + CustomUserDetails: throws LockedException
        // for UserStatus.LOCKED (checked before the password) and BadCredentialsException
        // for a wrong password or unknown email - both mapped in GlobalExceptionHandler.
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();

        return new AuthResponse(userMapper.toResponse(user), issueTokens(user));
    }

    @Transactional
    public AuthTokensResponse refresh(String rawRefreshToken) {
        UUID tokenUserId = jwtTokenProvider.parseRefreshToken(rawRefreshToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHasher.hash(rawRefreshToken))
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!tokenUserId.equals(stored.getUser().getId())) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        User user = stored.getUser();

        // One-time-use, race-safe: this single UPDATE is the only place that decides
        // whether a refresh token is "still active". Two concurrent requests for the
        // same token both reach this statement, but only one can match revoked_at IS
        // NULL - Postgres re-checks the predicate when it acquires the row lock, so
        // the loser sees 0 affected rows instead of both passing a stale in-memory check.
        int claimed = refreshTokenRepository.revokeIfActive(stored.getId(), Instant.now());
        if (claimed == 0) {
            // Not active anymore: either genuine reuse of an old, already-rotated
            // token, or this request lost the race to a concurrent rotation of the
            // very same token. Runs in its own committed transaction - see
            // RefreshTokenRevocationService javadoc for why this must not share this
            // method's transaction. Known tradeoff documented on
            // RefreshTokenRepository#revokeAllActiveForUser.
            refreshTokenRevocationService.revokeAllActiveForUser(user.getId());
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AccountLockedException();
        }

        return issueTokens(user);
    }

    @Transactional
    public void logout(String rawRefreshToken, UUID currentUserId) {
        jwtTokenProvider.parseRefreshToken(rawRefreshToken).ifPresent(tokenUserId -> {
            refreshTokenRepository.findByTokenHash(tokenHasher.hash(rawRefreshToken)).ifPresent(token -> {
                boolean ownedByCaller = token.getUser().getId().equals(currentUserId);
                if (ownedByCaller && token.getRevokedAt() == null) {
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                }
            });
        });
        // Unknown/foreign/already-revoked token: no-op, logout is idempotent and never
        // leaks whether the token existed.
    }

    private AuthTokensResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(tokenHasher.hash(refreshToken));
        entity.setExpiresAt(Instant.now().plus(jwtProperties.refreshTokenTtlDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(entity);

        return new AuthTokensResponse(accessToken, refreshToken);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
