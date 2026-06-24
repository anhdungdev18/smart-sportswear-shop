package com.dunghaiquyen.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

/** Refresh token missing, malformed, expired, or already revoked. */
public class InvalidTokenException extends ApiException {

    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
