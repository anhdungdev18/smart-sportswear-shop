package com.dunghaiquyen.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

/** Maps to error code ACCOUNT_LOCKED (API_SPEC_PHASE1.md section 14). */
public class AccountLockedException extends ApiException {

    public AccountLockedException() {
        super(HttpStatus.FORBIDDEN, "Account is locked");
    }
}
