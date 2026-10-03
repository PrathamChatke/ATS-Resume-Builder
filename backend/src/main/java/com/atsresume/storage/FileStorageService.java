package com.atsresume.storage;

import java.io.InputStream;

/**
 * Persists uploaded resume files. Kept behind an interface so the backing
 * store (local disk today, S3-compatible object storage later) can change
 * without touching any calling code.
 */
public interface FileStorageService {

    /**
     * Stores the file content and returns an opaque path that {@link #load}
     * can later use to retrieve it.
     */
    String store(String resumeId, String originalFilename, InputStream content);

    InputStream load(String storagePath);

    void delete(String storagePath);
}
