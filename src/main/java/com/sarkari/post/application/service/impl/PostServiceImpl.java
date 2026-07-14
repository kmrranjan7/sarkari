package com.sarkari.post.application.service.impl;

import com.sarkari.common.exception.BusinessException;
import com.sarkari.common.exception.ResourceNotFoundException;
import com.sarkari.post.application.dto.request.CreatePostRequest;
import com.sarkari.post.application.dto.request.UpdatePostRequest;
import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.dto.response.PostResponse;
import com.sarkari.post.application.mapper.PostMapper;
import com.sarkari.post.application.service.PostService;
import com.sarkari.post.domain.entity.Post;
import com.sarkari.post.domain.enums.PostType;
import com.sarkari.post.domain.repository.PostRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository repository;
    private final PostMapper mapper;
    private final PostImageCleanupService postImageCleanupService;

    @Override
    @CacheEvict(value = {"postByPostId", "postPages", "publicJobsPages"}, allEntries = true)
    public PostResponse create(CreatePostRequest request) {
        validateDateRange(request.getStartDate() == null ? null : request.getStartDate().atStartOfDay(),
                request.getEndDate() == null ? null : request.getEndDate().atStartOfDay());

        if (repository.existsByPostSlug(request.getPostSlug().trim())) {
            throw new BusinessException("postSlug already exists");
        }

        if (repository.existsByPostTitleIgnoreCase(request.getPostTitle().trim())) {
            throw new BusinessException("postTitle already exists");
        }

        String postId = buildPostId();
        String applicationId = request.getApplicationId();
        if (applicationId == null || applicationId.isBlank()) {
            applicationId = postId.replace("POST", "APP");
            request.setApplicationId(applicationId);
        }

        Post saved = repository.save(mapper.toEntity(request, postId));
        log.info("Created post postId={} type={} status={}", saved.getPostId(), saved.getPostType(), saved.getPostStatus());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "postByPostId", key = "#postId")
    public PostResponse getByPostId(String postId) {
        Post post = repository.findByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found for id: " + postId));
        return mapper.toResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "postPages", key = "T(String).format('%s|%s|%s|%s|%s|%s', #search, #postType, #page, #size, #sortBy, #sortDir)")
    public PagedResponse<PostResponse> getAll(String search, String postType, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Post> postPage;
        if (postType != null && !postType.isBlank()) {
            postPage = repository.findByPostType(PostType.fromValue(postType), pageable);
        } else if (search != null && !search.isBlank()) {
            String q = search.trim();
            postPage = repository.findByPostTitleContainingIgnoreCaseOrDepartmentContainingIgnoreCaseOrOrganizationContainingIgnoreCase(
                    q, q, q, pageable);
        } else {
            postPage = repository.findAll(pageable);
        }

        List<PostResponse> items = postPage.getContent().stream().map(mapper::toResponse).toList();

        return PagedResponse.<PostResponse>builder()
                .content(items)
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .sort(sortBy + "," + sortDir)
                .first(postPage.isFirst())
                .last(postPage.isLast())
                .build();
    }

    @Override
    @CacheEvict(value = {"postByPostId", "postPages", "publicJobsPages"}, allEntries = true)
    public PostResponse update(String postId, UpdatePostRequest request) {
        Post existing = repository.findByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found for id: " + postId));

        validateDateRange(request.getStartDate() == null ? null : request.getStartDate().atStartOfDay(),
                request.getEndDate() == null ? null : request.getEndDate().atStartOfDay());

        repository.findByPostSlug(request.getPostSlug().trim())
                .filter(found -> !found.getId().equals(existing.getId()))
                .ifPresent(found -> {
                    throw new BusinessException("postSlug already exists");
                });

        repository.findByPostTitleIgnoreCase(request.getPostTitle().trim())
                .filter(found -> !found.getId().equals(existing.getId()))
                .ifPresent(found -> {
                    throw new BusinessException("postTitle already exists");
                });

        mapper.updateEntity(existing, request);
        Post saved = repository.save(existing);
        log.info("Updated post postId={} type={} status={}", saved.getPostId(), saved.getPostType(), saved.getPostStatus());
        return mapper.toResponse(saved);
    }

    @Override
    @CacheEvict(value = {"postByPostId", "postPages", "publicJobsPages"}, allEntries = true)
    public void delete(String postId) {
        Post existing = repository.findByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found for id: " + postId));

        postImageCleanupService.deleteImagesFromContentHtml(existing.getContentHtml());
        repository.delete(existing);
        log.info("Deleted post postId={}", postId);
    }

    private String buildPostId() {
        int year = LocalDateTime.now().getYear();
        int suffix = ThreadLocalRandom.current().nextInt(10000, 100000);
        return "POST-" + year + "-" + suffix;
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException("endDate cannot be before startDate");
        }
    }
}
