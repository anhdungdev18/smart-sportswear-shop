package com.dunghaiquyen.ecommerce.modules.cart.web;

import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Cart endpoints are public (no JWT required), but JwtAuthenticationFilter
 * still populates the SecurityContext if a valid Bearer token is present
 * regardless of the endpoint being public - so a logged-in caller hitting
 * /api/v1/cart is distinguishable from a guest right here, no SecurityConfig
 * changes needed.
 *
 * Guest identity is a plain random UUID in an httpOnly cookie - not a signed
 * token, since the only thing it protects is "which cart shows in this
 * browser", not authentication. If the cookie is missing, one is minted and
 * set on the response so the FE never has to read, generate, or send it
 * manually; the browser carries it automatically on subsequent requests.
 */
@Component
public class CartIdentityResolver {

    public static final String SESSION_COOKIE_NAME = "session_id";

    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 30;

    public CartOwner resolve(HttpServletRequest request, HttpServletResponse response, CustomUserDetails principal) {
        if (principal != null) {
            return new CartOwner(principal.getUserId(), null);
        }

        String sessionId = readSessionCookie(request);
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
            writeSessionCookie(response, sessionId);
        }
        return new CartOwner(null, sessionId);
    }

    private String readSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void writeSessionCookie(HttpServletResponse response, String sessionId) {
        // secure(false): Phase 1 local dev runs over plain HTTP. Flip this once TLS
        // terminates in front of the app - browsers silently drop "Secure" cookies
        // on non-HTTPS, which would break guest cart entirely if set too early.
        ResponseCookie cookie = ResponseCookie.from(SESSION_COOKIE_NAME, sessionId)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(COOKIE_MAX_AGE_SECONDS)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
