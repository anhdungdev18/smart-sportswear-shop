package com.dunghaiquyen.ecommerce.common.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction over actually uploading/deleting an image file to a remote
 * media store. Scope is deliberately product-images-only (no avatar/banner/
 * generic file manager) - business code (ProductImageService) depends only
 * on this interface, never on the Cloudinary SDK directly, so a later
 * different provider (S3, etc.) can be swapped in behind it without
 * touching callers - same shape as MailService/SmtpMailService.
 */
public interface ImageStorageService {

    UploadedImage upload(MultipartFile file);

    /**
     * Best-effort by contract: callers (ProductImageService) must treat a
     * thrown exception here as "remote cleanup failed" and decide for
     * themselves whether that should fail the overall operation - this
     * method itself does not swallow anything, so it stays honest about
     * what actually happened.
     */
    void delete(String publicId);
}
