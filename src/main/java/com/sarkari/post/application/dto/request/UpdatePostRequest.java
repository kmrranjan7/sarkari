package com.sarkari.post.application.dto.request;

import com.sarkari.post.domain.enums.PostStatus;
import com.sarkari.post.domain.enums.PostType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePostRequest {

    @NotBlank(message = "postTitle is required")
    @Size(min = 3, max = 180, message = "postTitle must be between 3 and 180 chars")
    private String postTitle;

    @NotBlank(message = "postSlug is required")
    @Size(min = 3, max = 200, message = "postSlug must be between 3 and 200 chars")
    private String postSlug;

    @NotBlank(message = "contentHtml is required")
    @Size(min = 10, max = 100000, message = "contentHtml must be between 10 and 100000 chars")
    private String contentHtml;

    @Size(max = 60)
    private String applicationId;

    @Size(max = 120)
    private String department;

    @Size(max = 140)
    private String organization;

    @Size(max = 120)
    private String qualification;

    @PositiveOrZero(message = "vacancies must be 0 or greater")
    private Integer vacancies;

    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 80)
    private String stateName;

    @Size(max = 180)
    private String seoTitle;

    @Size(max = 320)
    private String seoDescription;

    @Size(max = 160)
    private String seoFocusKeyword;

    @Size(max = 5000)
    private String faqSchemaJson;

    @NotNull
    private PostStatus postStatus;

    @Future(message = "scheduledAt must be in future")
    private LocalDateTime scheduledAt;

    @NotNull
    private PostType postType;

    private Boolean isFeatured;

    @Min(value = 0, message = "priorityScore must be between 0 and 100")
    @Max(value = 100, message = "priorityScore must be between 0 and 100")
    private Integer priorityScore;
}
