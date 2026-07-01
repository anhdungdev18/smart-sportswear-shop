package com.dunghaiquyen.ecommerce.common.storage;

/** Unchecked wrapper around a failed upload/delete against the remote media store (network error, provider outage, etc.). */
public class ImageStorageException extends RuntimeException {

    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
