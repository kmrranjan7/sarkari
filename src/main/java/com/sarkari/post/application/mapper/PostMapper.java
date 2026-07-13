package com.sarkari.post.application.mapper;

import com.sarkari.post.application.dto.request.CreatePostRequest;
import com.sarkari.post.application.dto.request.UpdatePostRequest;
import com.sarkari.post.application.dto.response.PostResponse;
import com.sarkari.post.domain.entity.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public Post toEntity(CreatePostRequest req, String postId) {
        return Post.builder()
                .postId(postId)
                .postTitle(trim(req.getPostTitle()))
                .postSlug(trim(req.getPostSlug()))
                .contentHtml(trim(req.getContentHtml()))
                .applicationId(trim(req.getApplicationId()))
                .department(trim(req.getDepartment()))
                .organization(trim(req.getOrganization()))
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
        post.setPostTitle(trim(req.getPostTitle()));
        post.setPostSlug(trim(req.getPostSlug()));
        post.setContentHtml(trim(req.getContentHtml()));
        post.setApplicationId(trim(req.getApplicationId()));
        post.setDepartment(trim(req.getDepartment()));
        post.setOrganization(trim(req.getOrganization()));
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
                .applicationId(post.getApplicationId())
                .department(post.getDepartment())
                .organization(post.getOrganization())
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
}
