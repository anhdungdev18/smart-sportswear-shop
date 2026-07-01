package com.dunghaiquyen.ecommerce.common.storage;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default/safe provider - active whenever app.storage.provider is "none" or
 * unset (matchIfMissing - a fresh checkout with no Cloudinary credentials
 * must still start normally and keep the legacy manual-URL image flow
 * working). Only the NEW upload endpoint is actually unusable under this
 * provider, and it fails at the point of use with a clear message, not at
 * app startup - uploading was never possible without real credentials
 * anyway, so there is nothing to silently "fall back" to.
 */
@Service
@ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "none", matchIfMissing = true)
public class NoopImageStorageService implements ImageStorageService {

    @Override
    public UploadedImage upload(MultipartFile file) {
        throw new BusinessRuleException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Image upload is not configured on this server (set APP_STORAGE_PROVIDER=cloudinary with valid credentials)");
    }

    @Override
    public void delete(String publicId) {
        // No real backend exists under this provider, so nothing could ever have
        // been uploaded to one - any publicId on a row is, at most, leftover from
        // the legacy manual-URL flow. Deleting that DB row is still always safe;
        // there is just no remote asset to clean up.
    }
}
