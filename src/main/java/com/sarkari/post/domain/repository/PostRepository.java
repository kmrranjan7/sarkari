package com.sarkari.post.domain.repository;

import com.sarkari.post.domain.entity.Post;
import com.sarkari.post.domain.enums.PostStatus;
import com.sarkari.post.domain.enums.PostType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    interface SitemapPostProjection {
        String getPostSlug();
        LocalDateTime getUpdatedAt();
        String getImageUrls();
    }

    Optional<Post> findByPostId(String postId);

    Optional<Post> findByPostSlug(String postSlug);

    Optional<Post> findByPostTitleIgnoreCase(String postTitle);

    boolean existsByPostSlug(String postSlug);

    boolean existsByPostTitleIgnoreCase(String postTitle);

    Page<Post> findByPostType(PostType postType, Pageable pageable);

    Page<Post> findByPostStatus(PostStatus postStatus, Pageable pageable);

    Page<Post> findByPostTypeAndPostStatus(PostType postType, PostStatus postStatus, Pageable pageable);

        @Query("""
            select p from Post p
            where p.postType = :postType
              and p.postStatus = :postStatus
              and (
                lower(p.postTitle) like lower(concat('%', :search, '%'))
             or lower(p.department) like lower(concat('%', :search, '%'))
             or lower(p.organization) like lower(concat('%', :search, '%'))
                         or lower(p.qualification) like lower(concat('%', :search, '%'))
                         or lower(p.stateName) like lower(concat('%', :search, '%'))
                         or str(p.vacancies) like concat('%', :search, '%')
              )
            """)
        Page<Post> findByPostTypeAndPostStatusAndSearch(
            @Param("postType") PostType postType,
            @Param("postStatus") PostStatus postStatus,
            @Param("search") String search,
            Pageable pageable
        );

    List<Post> findByPostSlugIgnoreCaseAndPostStatus(String postSlug, PostStatus postStatus);

    List<Post> findByImageUrlsContaining(String imageToken);

    @Query("select p.postSlug as postSlug, p.updatedAt as updatedAt, p.imageUrls as imageUrls from Post p where p.postStatus = :postStatus order by p.updatedAt desc")
    List<SitemapPostProjection> findSitemapPostsByStatus(PostStatus postStatus);

    @Query("select p.imageUrls from Post p where p.imageUrls is not null and p.imageUrls <> ''")
    List<String> findAllImageUrlsValues();

        @Query("""
            select p from Post p
            where (
                lower(p.postTitle) like lower(concat('%', :search, '%'))
             or lower(p.department) like lower(concat('%', :search, '%'))
             or lower(p.organization) like lower(concat('%', :search, '%'))
             or lower(p.qualification) like lower(concat('%', :search, '%'))
             or lower(p.stateName) like lower(concat('%', :search, '%'))
             or str(p.vacancies) like concat('%', :search, '%')
            )
            """)
        Page<Post> findBySearchEverywhere(
            @Param("search") String search,
            Pageable pageable
        );

        Page<Post> findByPostTitleContainingIgnoreCaseOrDepartmentContainingIgnoreCaseOrOrganizationContainingIgnoreCase(
            String title,
            String department,
            String organization,
            Pageable pageable
    );
}
