package com.dunghaiquyen.ecommerce.common.mail;

import com.dunghaiquyen.ecommerce.config.AppMailProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Real provider, active only when app.mail.provider=smtp. Sends through the
 * JavaMailSender bean Spring Boot's own MailSenderAutoConfiguration already
 * builds from spring.mail.* (host/port/username/password/properties) -
 * nothing here talks to SMTP directly, so there is no custom transport code
 * to get wrong. Plain text only (SimpleMailMessage), matching every template
 * NotificationTemplates currently builds - no HTML templating this phase.
 *
 * <p>Fails fast at startup (constructor runs during context refresh, before
 * any request can be served) if app.mail.from or spring.mail.host is blank -
 * an SMTP provider with no sender address or no host is not a "degraded"
 * mode worth limping along in, it is a configuration mistake that must stop
 * the app rather than silently behave like nothing is wrong.
 */
@Service
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "smtp")
public class SmtpMailService implements MailService {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpMailService(JavaMailSender mailSender, MailProperties mailProperties, AppMailProperties appMailProperties) {
        if (appMailProperties.from() == null || appMailProperties.from().isBlank()) {
            throw new IllegalStateException(
                    "app.mail.provider=smtp requires app.mail.from (MAIL_FROM) to be set to a real sender address");
        }
        if (mailProperties.getHost() == null || mailProperties.getHost().isBlank()) {
            throw new IllegalStateException(
                    "app.mail.provider=smtp requires spring.mail.host (MAIL_HOST) to be set to a real SMTP host");
        }
        this.mailSender = mailSender;
        this.from = appMailProperties.from();
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
