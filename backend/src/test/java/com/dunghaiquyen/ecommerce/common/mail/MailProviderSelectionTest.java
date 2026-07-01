package com.dunghaiquyen.ecommerce.common.mail;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunghaiquyen.ecommerce.config.AppMailProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Phase "SMTP integration" - verifies app.mail.provider selects exactly one
 * MailService bean, without spinning up the full app (no DB needed: this is
 * pure Spring context wiring, tested the idiomatic Spring Boot way via
 * ApplicationContextRunner rather than AbstractIntegrationTest/MockMvc).
 *
 * <p>Limitation (deliberately not covered here, see final report): nothing
 * in this suite sends a real email to a real SMTP server - SmtpMailService's
 * send() is a thin, ~5-line call straight into Spring's own JavaMailSender,
 * which is itself a well-tested library; what actually needs verifying on
 * this project's side is the wiring (correct bean selected, fails fast on
 * bad config), which this class does cover.
 */
class MailProviderSelectionTest {

    @Configuration
    @EnableConfigurationProperties(AppMailProperties.class)
    static class AppMailPropertiesConfig {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
            .withUserConfiguration(AppMailPropertiesConfig.class, LoggingMailService.class, SmtpMailService.class);

    // ===== default config (no app.mail.provider set) =====

    @Test
    void noProviderConfigured_defaultsToLogging() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(MailService.class);
            assertThat(context).hasSingleBean(LoggingMailService.class);
            assertThat(context).doesNotHaveBean(SmtpMailService.class);
        });
    }

    // ===== explicit logging =====

    @Test
    void providerLogging_loadsLoggingMailService_notSmtp() {
        contextRunner.withPropertyValues("app.mail.provider=logging").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(LoggingMailService.class);
            assertThat(context).doesNotHaveBean(SmtpMailService.class);
        });
    }

    // ===== explicit smtp with valid config =====

    @Test
    void providerSmtp_withValidConfig_loadsSmtpMailService_notLogging() {
        contextRunner
                .withPropertyValues(
                        "app.mail.provider=smtp",
                        "app.mail.from=no-reply@example.com",
                        "spring.mail.host=smtp.example.com",
                        "spring.mail.port=587")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(MailService.class);
                    assertThat(context).hasSingleBean(SmtpMailService.class);
                    assertThat(context).doesNotHaveBean(LoggingMailService.class);
                });
    }

    // ===== smtp fails fast when app.mail.from is missing =====

    @Test
    void providerSmtp_missingFrom_failsFastAtStartup() {
        contextRunner
                .withPropertyValues("app.mail.provider=smtp", "spring.mail.host=smtp.example.com")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class)
                            .hasRootCauseMessage(
                                    "app.mail.provider=smtp requires app.mail.from (MAIL_FROM) to be set to a real sender address");
                });
    }

    // ===== smtp fails fast when spring.mail.host is blank =====

    @Test
    void providerSmtp_blankHost_failsFastAtStartup() {
        contextRunner
                .withPropertyValues(
                        "app.mail.provider=smtp", "app.mail.from=no-reply@example.com", "spring.mail.host=")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class)
                            .hasRootCauseMessage(
                                    "app.mail.provider=smtp requires spring.mail.host (MAIL_HOST) to be set to a real SMTP host");
                });
    }

    /** Stands in for NotificationService/PasswordResetService, both of which require a MailService bean to exist. */
    @Configuration
    static class MailServiceConsumer {
        MailServiceConsumer(MailService mailService) {
        }
    }

    // ===== unknown provider value: neither bean matches, app fails to start cleanly =====

    @Test
    void unknownProviderValue_noMailServiceBeanMatches_contextFailsToLoad() {
        contextRunner
                .withUserConfiguration(MailServiceConsumer.class)
                .withPropertyValues("app.mail.provider=typo-value")
                .run(context -> assertThat(context).hasFailed());
    }
}
