package com.sarkari.post.application.mapper;

import com.sarkari.post.application.dto.request.CreatePostRequest;
import com.sarkari.post.application.dto.request.UpdatePostRequest;
import com.sarkari.post.application.dto.response.PostResponse;
import com.sarkari.post.domain.entity.Post;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("(?i)<img[^>]*\\bsrc\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern UPLOAD_FILE_NAME_PATTERN = Pattern.compile(
            "^[0-9]{10,}-[a-f0-9-]{36}(?:\\.[A-Za-z0-9]+)?$",
            Pattern.CASE_INSENSITIVE
    );

    public Post toEntity(CreatePostRequest req, String postId) {
        String contentHtml = trim(req.getContentHtml());
        return Post.builder()
                .postId(postId)
                .postTitle(trim(req.getPostTitle()))
                .postSlug(trim(req.getPostSlug()))
                .contentHtml(contentHtml)
                .imageUrls(extractImageUrlsCsv(contentHtml))
                .applicationId(trim(req.getApplicationId()))
                .department(trim(req.getDepartment()))
                .organization(trim(req.getOrganization()))
                .qualification(trim(req.getQualification()))
                .vacancies(req.getVacancies())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .stateName(trim(req.getStateName()))
                .seoTitle(trim(req.getSeoTitle()))
                .seoDescription(trim(req.getSeoDescription()))
                .seoFocusKeyword(trim(req.getSeoFocusKeyword()))
                .faqSchemaJson(trim(req.getFaqSchemaJson()))
                .postStatus(req.getPostStatus())
                .scheduledAt(req.getScheduledAt())
                .postType(req.getPostType())
                .build();
    }

    public void updateEntity(Post post, UpdatePostRequest req) {
        String contentHtml = trim(req.getContentHtml());
        post.setPostTitle(trim(req.getPostTitle()));
        post.setPostSlug(trim(req.getPostSlug()));
        post.setContentHtml(contentHtml);
        post.setImageUrls(extractImageUrlsCsv(contentHtml));
        post.setApplicationId(trim(req.getApplicationId()));
        post.setDepartment(trim(req.getDepartment()));
        post.setOrganization(trim(req.getOrganization()));
        post.setQualification(trim(req.getQualification()));
        post.setVacancies(req.getVacancies());
        post.setStartDate(req.getStartDate());
        post.setEndDate(req.getEndDate());
        post.setStateName(trim(req.getStateName()));
        post.setSeoTitle(trim(req.getSeoTitle()));
        post.setSeoDescription(trim(req.getSeoDescription()));
        post.setSeoFocusKeyword(trim(req.getSeoFocusKeyword()));
        post.setFaqSchemaJson(trim(req.getFaqSchemaJson()));
        post.setPostStatus(req.getPostStatus());
        post.setScheduledAt(req.getScheduledAt());
        post.setPostType(req.getPostType());
    }

    public PostResponse toResponse(Post post) {
        return PostResponse.builder()
                .id(post.getPostId())
                .postTitle(post.getPostTitle())
                .postSlug(post.getPostSlug())
                .contentHtml(post.getContentHtml())
                .imageUrls(post.getImageUrls())
                .applicationId(post.getApplicationId())
                .department(post.getDepartment())
                .organization(post.getOrganization())
                .qualification(post.getQualification())
                .vacancies(post.getVacancies())
                .startDate(post.getStartDate())
                .endDate(post.getEndDate())
                .stateName(post.getStateName())
                .seoTitle(post.getSeoTitle())
                .seoDescription(post.getSeoDescription())
                .seoFocusKeyword(post.getSeoFocusKeyword())
                .faqSchemaJson(post.getFaqSchemaJson())
                .postStatus(post.getPostStatus())
                .scheduledAt(post.getScheduledAt())
                .postType(post.getPostType())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private String trim(String input) {
        return input == null ? null : input.trim();
    }

    private String extractImageUrlsCsv(String contentHtml) {
        if (contentHtml == null || contentHtml.isBlank()) {
            return null;
        }

        Matcher matcher = IMG_SRC_PATTERN.matcher(contentHtml);
        Set<String> urls = new LinkedHashSet<>();

        while (matcher.find()) {
            String token = toImageToken(matcher.group(1));
            if (token != null) {
                urls.add(token);
            }
        }

        if (urls.isEmpty()) {
            return null;
        }

        return String.join(",", urls);
    }

    private String toImageToken(String src) {
        String value = trim(src);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            URI uri = URI.create(value);
            if (uri.getPath() != null && !uri.getPath().isBlank()) {
                value = uri.getPath();
            }
        } catch (Exception ignored) {
            // Keep raw value when URI parsing fails.
        }

        int slashIndex = value.lastIndexOf('/');
        String fileName = slashIndex >= 0 ? value.substring(slashIndex + 1) : value;
        fileName = trim(fileName);

        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        if (!UPLOAD_FILE_NAME_PATTERN.matcher(fileName).matches()) {
            return null;
        }

        return fileName;
    }

}
