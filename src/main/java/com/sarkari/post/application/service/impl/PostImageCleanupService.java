package com.sarkari.post.application.service.impl;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PostImageCleanupService {

    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("(?i)<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern UPLOAD_FILE_NAME_PATTERN = Pattern.compile("^[0-9]{10,}-[a-f0-9-]{36}(?:\\.[A-Za-z0-9]+)?$", Pattern.CASE_INSENSITIVE);
    private static final String UPLOADS_PREFIX = "/uploads/";

    private final Path uploadsBaseDir;

    public PostImageCleanupService(@Value("${app.uploads.base-dir:../my-app/public/uploads}") String uploadsBaseDir) {
        this.uploadsBaseDir = Paths.get(uploadsBaseDir).toAbsolutePath().normalize();
    }

    public void deleteImagesFromContentHtml(String contentHtml) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return;
        }

        if (!Files.exists(uploadsBaseDir)) {
            return;
        }

        Set<Path> imagePaths = resolveImagePaths(contentHtml);
        for (Path imagePath : imagePaths) {
            try {
                Files.deleteIfExists(imagePath);
            } catch (Exception ex) {
                log.warn("Failed to delete uploaded image file={}", imagePath, ex);
            }
        }
    }

    private Set<Path> resolveImagePaths(String contentHtml) {
        Set<Path> imagePaths = new LinkedHashSet<>();
        Matcher matcher = IMG_SRC_PATTERN.matcher(contentHtml);

        while (matcher.find()) {
            String src = matcher.group(1);
            Optional<String> relativePath = toUploadsRelativePath(src);
            if (relativePath.isEmpty()) {
                continue;
            }

            Path candidate = uploadsBaseDir.resolve(relativePath.get()).normalize();
            if (candidate.startsWith(uploadsBaseDir)) {
                imagePaths.add(candidate);
            }
        }

        return imagePaths;
    }

    private Optional<String> toUploadsRelativePath(String src) {
        if (src == null || src.isBlank()) {
            return Optional.empty();
        }

        String path = src.trim();
        try {
            URI uri = URI.create(path);
            if (uri.getPath() != null && !uri.getPath().isBlank()) {
                path = uri.getPath();
            }
        } catch (Exception ignored) {
            // Keep raw src when URI parsing fails.
        }

        int uploadsIndex = path.indexOf(UPLOADS_PREFIX);
        if (uploadsIndex < 0) {
            String compact = path.trim();
            int slashIndex = compact.lastIndexOf('/');
            if (slashIndex >= 0) {
                compact = compact.substring(slashIndex + 1);
            }
            compact = compact.trim();
            if (UPLOAD_FILE_NAME_PATTERN.matcher(compact).matches()) {
                return Optional.of(compact);
            }
            return Optional.empty();
        }

        String relativePath = path.substring(uploadsIndex + UPLOADS_PREFIX.length());
        int queryIndex = relativePath.indexOf('?');
        if (queryIndex >= 0) {
            relativePath = relativePath.substring(0, queryIndex);
        }

        int fragmentIndex = relativePath.indexOf('#');
        if (fragmentIndex >= 0) {
            relativePath = relativePath.substring(0, fragmentIndex);
        }

        if (relativePath.isBlank()) {
            return Optional.empty();
        }

        String decoded = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);
        return Optional.of(decoded.replace('\\', '/'));
    }
}