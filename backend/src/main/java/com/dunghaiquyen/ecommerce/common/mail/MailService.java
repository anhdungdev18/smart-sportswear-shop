package com.dunghaiquyen.ecommerce.common.mail;

/**
 * Abstraction over actually sending an email. Phase L's only consumer is
 * password reset, but this is intentionally generic (to/subject/body) so a
 * later real SMTP-backed implementation can be swapped in behind this same
 * interface without touching the callers.
 */
public interface MailService {

    void send(String to, String subject, String body);
}
