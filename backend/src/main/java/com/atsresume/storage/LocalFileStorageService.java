package com.atsresume.storage;

import com.atsresume.config.StorageProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootDirectory;

    public LocalFileStorageService(StorageProperties properties) {
        this.rootDirectory = Path.of(properties.rootDirectory());
        try {
            Files.createDirectories(rootDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create resume storage directory: " + rootDirectory, e);
        }
    }

    @Override
    public String store(String resumeId, String originalFilename, InputStream content) {
        String extension = extensionOf(originalFilename);
        Path target = rootDirectory.resolve(resumeId + extension);
        try {
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store resume file " + originalFilename, e);
        }
        return target.toString();
    }

    @Override
    public InputStream load(String storagePath) {
        try {
            return Files.newInputStream(Path.of(storagePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resume file at " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(Path.of(storagePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete resume file at " + storagePath, e);
        }
    }

    private String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex) : "";
    }
}
