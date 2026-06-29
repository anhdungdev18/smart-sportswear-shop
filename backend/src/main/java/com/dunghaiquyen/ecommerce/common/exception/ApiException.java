package com.dunghaiquyen.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for business/domain exceptions that should map to a clean HTTP
 * status + message instead of falling through to the generic 500 handler.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
