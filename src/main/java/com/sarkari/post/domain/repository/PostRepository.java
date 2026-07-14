package com.sarkari.post.domain.repository;

import com.sarkari.post.domain.entity.Post;
import com.sarkari.post.domain.enums.PostStatus;
import com.sarkari.post.domain.enums.PostType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByPostId(String postId);

    Optional<Post> findByPostSlug(String postSlug);

    Optional<Post> findByPostTitleIgnoreCase(String postTitle);

    boolean existsByPostSlug(String postSlug);

    boolean existsByPostTitleIgnoreCase(String postTitle);

    Page<Post> findByPostType(PostType postType, Pageable pageable);

    Page<Post> findByPostStatus(PostStatus postStatus, Pageable pageable);

    Page<Post> findByPostTypeAndPostStatus(PostType postType, PostStatus postStatus, Pageable pageable);

    List<Post> findByPostSlugIgnoreCaseAndPostStatus(String postSlug, PostStatus postStatus);

    Page<Post> findByPostTitleContainingIgnoreCaseOrDepartmentContainingIgnoreCaseOrOrganizationContainingIgnoreCase(
            String title,
            String department,
            String organization,
            Pageable pageable
    );
}
