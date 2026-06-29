package com.dunghaiquyen.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Use for violations of an explicit business rule (insufficient stock, invalid
 * order status transition, duplicate callback, ...), as opposed to plain
 * input validation which Bean Validation already covers.
 */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public BusinessRuleException(HttpStatus status, String message) {
        super(status, message);
    }
}
