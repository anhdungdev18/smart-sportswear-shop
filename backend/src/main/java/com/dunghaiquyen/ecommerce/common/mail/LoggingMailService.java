package com.dunghaiquyen.ecommerce.common.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Default/dev/test provider - logs what would have been sent instead of
 * actually sending it, which keeps local dev and the integration test suite
 * fully hermetic (no mail server needed to run tests, no flaky network
 * calls). Active whenever app.mail.provider is "logging" or unset
 * (matchIfMissing - this is the safe default, a fresh checkout must not
 * accidentally try to send real email). See SmtpMailService for the real
 * provider, selected the same way via app.mail.provider=smtp - business code
 * (NotificationService, PasswordResetService) only ever depends on the
 * {@link MailService} interface and never knows which one is active.
 */
@Service
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "logging", matchIfMissing = true)
public class LoggingMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingMailService.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("Mail (logging-only provider, not actually sent) to={} subject={}\n{}", to, subject, body);
    }
}
