package com.dunghaiquyen.ecommerce.common.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.cloudinary.Cloudinary;
import com.dunghaiquyen.ecommerce.config.AppCloudinaryProperties;
import com.dunghaiquyen.ecommerce.config.CloudinaryConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Verifies app.storage.provider selects exactly one ImageStorageService bean
 * - same pattern/rationale as MailProviderSelectionTest (no DB needed, pure
 * Spring context wiring via ApplicationContextRunner).
 *
 * <p>Limitation (see final report): nothing here uploads to real Cloudinary -
 * CloudinaryImageStorageService.upload()/delete() are thin calls straight
 * into the official Cloudinary SDK's Uploader, which is not this project's
 * code to re-test. What this class verifies is the wiring this project DOES
 * own: correct bean selected per config, fail-fast on missing credentials.
 */
class ImageStorageProviderSelectionTest {

    @Configuration
    @EnableConfigurationProperties(AppCloudinaryProperties.class)
    static class AppCloudinaryPropertiesConfig {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    AppCloudinaryPropertiesConfig.class,
                    CloudinaryConfig.class,
                    NoopImageStorageService.class,
                    CloudinaryImageStorageService.class);

    // ===== default config (no app.storage.provider set) =====

    @Test
    void noProviderConfigured_defaultsToNoop() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(ImageStorageService.class);
            assertThat(context).hasSingleBean(NoopImageStorageService.class);
            assertThat(context).doesNotHaveBean(CloudinaryImageStorageService.class);
            assertThat(context).doesNotHaveBean(Cloudinary.class);
        });
    }

    // ===== explicit none =====

    @Test
    void providerNone_loadsNoop_notCloudinary() {
        contextRunner.withPropertyValues("app.storage.provider=none").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(NoopImageStorageService.class);
            assertThat(context).doesNotHaveBean(CloudinaryImageStorageService.class);
        });
    }

    // ===== explicit cloudinary with valid credentials =====

    @Test
    void providerCloudinary_withValidCredentials_loadsCloudinaryService_notNoop() {
        contextRunner
                .withPropertyValues(
                        "app.storage.provider=cloudinary",
                        "app.cloudinary.cloud-name=demo",
                        "app.cloudinary.api-key=123456789",
                        "app.cloudinary.api-secret=secret-abc")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ImageStorageService.class);
                    assertThat(context).hasSingleBean(CloudinaryImageStorageService.class);
                    assertThat(context).hasSingleBean(Cloudinary.class);
                    assertThat(context).doesNotHaveBean(NoopImageStorageService.class);
                });
    }

    // ===== cloudinary fails fast when credentials are missing =====

    @Test
    void providerCloudinary_missingCredentials_failsFastAtStartup() {
        contextRunner.withPropertyValues("app.storage.provider=cloudinary").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalStateException.class);
        });
    }

    @Test
    void providerCloudinary_blankApiSecret_failsFastAtStartup() {
        contextRunner
                .withPropertyValues(
                        "app.storage.provider=cloudinary",
                        "app.cloudinary.cloud-name=demo",
                        "app.cloudinary.api-key=123456789",
                        "app.cloudinary.api-secret=")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(IllegalStateException.class)
                            .hasRootCauseMessage(
                                    "app.storage.provider=cloudinary requires CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY and "
                                            + "CLOUDINARY_API_SECRET to all be set");
                });
    }

    /** Stands in for ProductImageService, which requires an ImageStorageService bean to exist. */
    @Configuration
    static class ImageStorageServiceConsumer {
        ImageStorageServiceConsumer(ImageStorageService imageStorageService) {
        }
    }

    // ===== unknown provider value: neither bean matches, app fails to start cleanly =====

    @Test
    void unknownProviderValue_noImageStorageServiceBeanMatches_contextFailsToLoad() {
        contextRunner
                .withUserConfiguration(ImageStorageServiceConsumer.class)
                .withPropertyValues("app.storage.provider=typo-value")
                .run(context -> assertThat(context).hasFailed());
    }
}
