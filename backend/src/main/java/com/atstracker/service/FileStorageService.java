package com.atstracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

// Storing resumes on local disk. Fine for a portfolio project / small deployment -
// if this ever needed to run across multiple instances, this is the first thing
// I'd swap for S3 (or similar) instead, since local disk won't survive a redeploy
// on most hosting platforms.
@Service
public class FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".doc", ".docx");

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDirPath) {
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create upload directory: " + this.uploadDir, e);
        }
    }

    public String store(MultipartFile file) {
        String originalName = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
        String extension = getExtension(originalName);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Only PDF and Word documents are accepted");
        }

        // random filename on disk so two people uploading "resume.pdf" don't collide,
        // and so nobody can guess another candidate's file path
        String storedName = UUID.randomUUID() + extension;

        try {
            Path target = uploadDir.resolve(storedName);
            Files.copy(file.getInputStream(), target);
            return storedName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + originalName, e);
        }
    }

    public Resource load(String storedFileName) {
        try {
            Path filePath = uploadDir.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new IllegalArgumentException("File not found: " + storedFileName);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid file path: " + storedFileName, e);
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "" : filename.substring(dot);
    }
}
