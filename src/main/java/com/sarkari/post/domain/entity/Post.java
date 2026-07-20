package com.sarkari.post.domain.entity;

import com.sarkari.post.domain.enums.PostStatus;
import com.sarkari.post.domain.enums.PostType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_posts_post_id", columnList = "post_id"),
                @Index(name = "idx_posts_slug", columnList = "post_slug"),
                @Index(name = "idx_posts_type", columnList = "post_type"),
                @Index(name = "idx_posts_status", columnList = "post_status"),
                @Index(name = "idx_posts_created", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false, unique = true, length = 40)
    private String postId;

    @Column(name = "post_title", nullable = false, length = 180)
    private String postTitle;

    @Column(name = "post_slug", nullable = false, unique = true, length = 200)
    private String postSlug;

    @Column(name = "content_html", nullable = false, length = 100000)
    private String contentHtml;

    @Column(name = "application_id", length = 60)
    private String applicationId;

    @Column(length = 120)
    private String department;

    @Column(length = 140)
    private String organization;

    @Column(length = 120)
    private String qualification;

    @Column(name = "image_urls", length = 5000)
    private String imageUrls;

    @Column
    private Integer vacancies;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 80)
    private String stateName;

    @Column(length = 180)
    private String seoTitle;

    @Column(length = 320)
    private String seoDescription;

    @Column(length = 160)
    private String seoFocusKeyword;

    @Column(length = 5000)
    private String faqSchemaJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false, length = 40)
    private PostStatus postStatus;

    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 40)
    private PostType postType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
