package com.sarkari.post.application.service.impl;

import com.sarkari.common.exception.BusinessException;
import com.sarkari.common.exception.ResourceNotFoundException;
import com.sarkari.post.application.dto.response.ImageUploadResponse;
import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.domain.entity.Post;
import com.sarkari.post.domain.repository.PostRepository;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class PostImageCleanupService {

    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("(?i)<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern UPLOAD_FILE_NAME_PATTERN = Pattern.compile("^[0-9]{10,}-[a-f0-9-]{36}(?:\\.[A-Za-z0-9]+)?$", Pattern.CASE_INSENSITIVE);
    private static final String UPLOADS_PREFIX = "/uploads/";
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;

    private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = buildContentTypeExtensionMap();

    private final Path uploadsBaseDir;
    private final PostRepository postRepository;

    public PostImageCleanupService(
            @Value("${app.uploads.base-dir:../my-app/public/uploads}") String uploadsBaseDir,
            PostRepository postRepository
    ) {
        this.uploadsBaseDir = Paths.get(uploadsBaseDir).toAbsolutePath().normalize();
        this.postRepository = postRepository;
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

    public List<String> listUploadedImages() {
        if (!Files.exists(uploadsBaseDir) || !Files.isDirectory(uploadsBaseDir)) {
            return List.of();
        }

        try (var stream = Files.list(uploadsBaseDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> UPLOAD_FILE_NAME_PATTERN.matcher(name).matches())
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (Exception ex) {
            throw new BusinessException("Unable to list uploaded images");
        }
    }

    public PagedResponse<String> listUploadedImagesPaged(int page, int size, String mode) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 20);
        String normalizedMode = mode == null ? "all" : mode.trim().toLowerCase(Locale.ROOT);

        List<String> allImages = listUploadedImages();
        List<String> filteredImages = switch (normalizedMode) {
            case "matched" -> {
                Set<String> dbTokens = readDbImageTokenSet();
                yield allImages.stream()
                        .filter(dbTokens::contains)
                        .toList();
            }
            case "unmatched" -> {
                Set<String> dbTokens = readDbImageTokenSet();
                yield allImages.stream()
                        .filter(image -> !dbTokens.contains(image))
                        .toList();
            }
            case "all" -> allImages;
            default -> throw new BusinessException("Invalid mode. Allowed: all, matched, unmatched");
        };

        int totalElements = filteredImages.size();
        int totalPages = totalElements == 0 ? 1 : (int) Math.ceil(totalElements / (double) safeSize);

        int start = (safePage - 1) * safeSize;
        if (start >= totalElements) {
            start = totalElements;
        }

        int end = Math.min(start + safeSize, totalElements);
        List<String> content = filteredImages.subList(start, end);

        return PagedResponse.<String>builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .sort("name,desc")
                .first(safePage <= 1)
                .last(safePage >= totalPages)
                .build();
    }

    private Set<String> readDbImageTokenSet() {
        return postRepository.findAllImageUrlsValues().stream()
                .flatMap(csv -> java.util.Arrays.stream(csv.split(",")))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .collect(Collectors.toSet());
    }

    @Transactional
    public void deleteUploadedImage(String imageName) {
        if (imageName == null || imageName.isBlank()) {
            throw new BusinessException("imageName is required");
        }

        String token = imageName.trim();
        if (!UPLOAD_FILE_NAME_PATTERN.matcher(token).matches()) {
            throw new BusinessException("Invalid image name format");
        }

        removeImageTokenFromPosts(token);

        Path target = uploadsBaseDir.resolve(token).normalize();
        if (!target.startsWith(uploadsBaseDir)) {
            throw new BusinessException("Invalid image path");
        }

        try {
            boolean deleted = Files.deleteIfExists(target);
            if (!deleted) {
                throw new ResourceNotFoundException("Image not found: " + token);
            }
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Unable to delete uploaded image");
        }
    }

    private void removeImageTokenFromPosts(String token) {
        List<Post> candidates = postRepository.findByImageUrlsContaining(token);
        if (candidates.isEmpty()) {
            return;
        }

        for (Post post : candidates) {
            String csv = post.getImageUrls();
            if (csv == null || csv.isBlank()) {
                continue;
            }

            String updated = java.util.Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(part -> !part.isBlank())
                    .filter(part -> !part.equalsIgnoreCase(token))
                    .collect(Collectors.joining(","));

            post.setImageUrls(updated.isBlank() ? null : updated);

            String contentHtml = post.getContentHtml();
            if (contentHtml != null && !contentHtml.isBlank()) {
                post.setContentHtml(removeImageTagByToken(contentHtml, token));
            }
        }

        postRepository.saveAll(candidates);
    }

    private String removeImageTagByToken(String contentHtml, String token) {
        StringBuilder result = new StringBuilder(contentHtml.length());
        Matcher matcher = IMG_SRC_PATTERN.matcher(contentHtml);
        int cursor = 0;

        while (matcher.find()) {
            String src = matcher.group(1);
            Optional<String> relativePath = toUploadsRelativePath(src);
            boolean matchesToken = relativePath
                    .map(path -> {
                        int slash = path.lastIndexOf('/');
                        String fileName = slash >= 0 ? path.substring(slash + 1) : path;
                        return fileName.equalsIgnoreCase(token);
                    })
                    .orElse(false);

            if (matchesToken) {
                int tagStart = matcher.start();
                int tagEnd = contentHtml.indexOf('>', matcher.end());
                if (tagEnd < 0) {
                    continue;
                }

                result.append(contentHtml, cursor, tagStart);
                cursor = tagEnd + 1;
            }
        }

        if (cursor == 0) {
            return contentHtml;
        }

        result.append(contentHtml.substring(cursor));
        return result.toString();
    }

    public ImageUploadResponse uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Image file is required");
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessException("Image size cannot exceed 10MB");
        }

        String contentType = normalizeContentType(file.getContentType());
        String extension = CONTENT_TYPE_TO_EXTENSION.get(contentType);
        if (extension == null) {
            extension = resolveExtensionFromFilename(file.getOriginalFilename());
        }

        if (extension == null) {
            throw new BusinessException("Unsupported image type. Allowed: jpg, jpeg, png, webp, gif");
        }

        String generatedName = System.currentTimeMillis() + "-" + UUID.randomUUID() + "." + extension;

        try {
            Files.createDirectories(uploadsBaseDir);
            Path target = uploadsBaseDir.resolve(generatedName).normalize();
            if (!target.startsWith(uploadsBaseDir)) {
                throw new BusinessException("Invalid upload destination");
            }

            file.transferTo(target);

            return ImageUploadResponse.builder()
                    .fileName(generatedName)
                    .url(UPLOADS_PREFIX + generatedName)
                    .contentType(contentType)
                    .size(file.getSize())
                    .build();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to upload image", ex);
            throw new BusinessException("Unable to upload image");
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

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.trim().toLowerCase();
    }

    private String resolveExtensionFromFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }

        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return null;
        }

        String ext = originalFilename.substring(dot + 1).trim().toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg", "png", "webp", "gif" -> ext;
            default -> null;
        };
    }

    private static Map<String, String> buildContentTypeExtensionMap() {
        Map<String, String> map = new HashMap<>();
        map.put("image/jpeg", "jpg");
        map.put("image/jpg", "jpg");
        map.put("image/png", "png");
        map.put("image/webp", "webp");
        map.put("image/gif", "gif");
        return map;
    }
}