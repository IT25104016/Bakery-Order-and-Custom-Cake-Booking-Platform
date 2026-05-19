package com.bakery.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class ImageUploadService {

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final String PRODUCT_DIR = "src/main/resources/static/images/products/";
    private static final String PROFILE_DIR = "src/main/resources/static/images/profiles/";

    // ── Upload product image ───────────────────────────────────
    public String uploadProductImage(MultipartFile file) throws IOException {
        return upload(file, PRODUCT_DIR, 5);
    }

    // ── Upload profile picture ─────────────────────────────────
    public String uploadProfileImage(MultipartFile file) throws IOException {
        return upload(file, PROFILE_DIR, 2); // max 2MB for profile pics
    }

    // ── Shared upload logic ────────────────────────────────────
    private String upload(MultipartFile file, String dir, int maxMB) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, GIF, or WebP files can be uploaded.");
        }
        if (file.getSize() > (long) maxMB * 1024 * 1024) {
            throw new IllegalArgumentException("Image size lower than " + maxMB + "MB .");
        }

        String original  = file.getOriginalFilename();
        String extension = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : ".jpg";
        String filename = UUID.randomUUID().toString().replace("-", "").substring(0, 12) + extension;

        Path uploadPath = Paths.get(dir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        Files.copy(file.getInputStream(), uploadPath.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }
}