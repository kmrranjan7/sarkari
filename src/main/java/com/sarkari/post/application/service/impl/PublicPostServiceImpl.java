package com.sarkari.post.application.service.impl;

import com.sarkari.common.exception.BusinessException;
import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.dto.response.PostResponse;
import com.sarkari.post.application.mapper.PostMapper;
import com.sarkari.post.application.mapper.PublicPostMapper;
import com.sarkari.post.application.service.PublicPostService;
import com.sarkari.post.domain.enums.PostStatus;
import com.sarkari.post.domain.enums.PostType;
import com.sarkari.post.domain.repository.PostRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicPostServiceImpl implements PublicPostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final PublicPostMapper publicPostMapper;

    @Override
    @Cacheable(value = "publicJobsPages", key = "T(String).format('%s|%s|%s|%s|%s|%s|%s', #search, #postType, #postStatus, #page, #size, #sortBy, #sortDir)")
    public PagedResponse<Map<String, Object>> getPublicJobs(String search, String postType, String postStatus, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PostResponse> postPage = postRepository
                .findByPostTypeAndPostStatus(PostType.fromValue(postType), PostStatus.fromValue(postStatus), pageable)
                .map(postMapper::toResponse);

        List<PostResponse> content = postPage.getContent();
        PagedResponse<PostResponse> publishedPage = PagedResponse.<PostResponse>builder()
                .content(content)
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .sort(sortBy + "," + sortDir)
                .first(postPage.isFirst())
                .last(postPage.isLast())
                .build();

        return publicPostMapper.toPublicJobsPage(publishedPage);
    }

    @Override
    @Cacheable(value = "publicJobsPages", key = "T(String).format('%s|%s|%s|%s|%s', #postStatus, #page, #size, #sortBy, #sortDir)")
    public PagedResponse<Map<String, Object>> getPublicPostsByStatus(String postStatus, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PostResponse> postPage = postRepository
                .findByPostStatus(PostStatus.fromValue(postStatus), pageable)
                .map(postMapper::toResponse);

        List<PostResponse> content = postPage.getContent();
        PagedResponse<PostResponse> publishedPage = PagedResponse.<PostResponse>builder()
                .content(content)
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .sort(sortBy + "," + sortDir)
                .first(postPage.isFirst())
                .last(postPage.isLast())
                .build();

        return publicPostMapper.toPublicJobsPage(publishedPage);
    }
}