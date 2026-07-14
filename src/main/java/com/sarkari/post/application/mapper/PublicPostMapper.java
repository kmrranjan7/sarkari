package com.sarkari.post.application.mapper;

import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.dto.response.PostResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PublicPostMapper {

    public PagedResponse<Map<String, Object>> toPublicJobsPage(PagedResponse<PostResponse> source) {
        List<Map<String, Object>> content = source.getContent().stream()
                .map(this::toPublicJobItem)
                .toList();

        return PagedResponse.<Map<String, Object>>builder()
                .content(content)
                .first(source.isFirst())
                .last(source.isLast())
                .page(source.getPage())
                .size(source.getSize())
                .sort(source.getSort())
                .totalElements(source.getTotalElements())
                .totalPages(source.getTotalPages())
                .build();
    }

    private Map<String, Object> toPublicJobItem(PostResponse post) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("applicationId", post.getApplicationId());
        item.put("createdAt", post.getCreatedAt());
        item.put("department", post.getDepartment());
        item.put("organization", post.getOrganization());
        item.put("qualification", post.getQualification());
        item.put("postSlug", post.getPostSlug());
        item.put("postStatus", post.getPostStatus());
        item.put("postTitle", post.getPostTitle());
        item.put("postType", post.getPostType());
        item.put("startDate", post.getStartDate());
        item.put("endDate", post.getEndDate());
        item.put("stateName", post.getStateName());
        item.put("vacancies", post.getVacancies());
        item.put("updatedAt", post.getUpdatedAt());
        return item;
    }
}