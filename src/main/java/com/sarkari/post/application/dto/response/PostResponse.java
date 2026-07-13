package com.sarkari.post.application.dto.response;

import com.sarkari.post.domain.enums.PostStatus;
import com.sarkari.post.domain.enums.PostType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostResponse {

    private String id;
    private String postTitle;
    private String postSlug;
    private String contentHtml;
    private String applicationId;
    private String department;
    private String organization;
    private Integer vacancies;
    private LocalDate startDate;
    private LocalDate endDate;
    private String stateName;
    private String seoTitle;
    private String seoDescription;
    private String seoFocusKeyword;
    private String faqSchemaJson;
    private PostStatus postStatus;
    private LocalDateTime scheduledAt;
    private PostType postType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
