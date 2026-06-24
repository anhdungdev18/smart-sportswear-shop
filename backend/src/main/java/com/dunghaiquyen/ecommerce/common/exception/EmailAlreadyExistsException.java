package com.dunghaiquyen.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

/** Maps to error code EMAIL_ALREADY_EXISTS (API_SPEC_PHASE1.md section 14). */
public class EmailAlreadyExistsException extends ApiException {

    public EmailAlreadyExistsException(String email) {
        super(HttpStatus.CONFLICT, "Email already exists: " + email);
    }
}
