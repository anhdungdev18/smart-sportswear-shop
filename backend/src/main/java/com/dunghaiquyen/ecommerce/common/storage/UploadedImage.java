package com.dunghaiquyen.ecommerce.common.storage;

/** width/height are nullable - not every provider/response shape guarantees them. */
public record UploadedImage(String publicId, String secureUrl, Integer width, Integer height) {
}
