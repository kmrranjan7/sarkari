package com.sarkari.post.application.service.impl;

import com.sarkari.common.exception.BusinessException;
import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.dto.response.PostResponse;
import com.sarkari.post.application.dto.response.SitemapUrlResponse;
import com.sarkari.post.application.mapper.PostMapper;
import com.sarkari.post.application.mapper.PublicPostMapper;
import com.sarkari.post.application.service.PublicPostService;
import com.sarkari.post.domain.enums.PostStatus;
import com.sarkari.post.domain.enums.PostType;
import com.sarkari.post.domain.repository.PostRepository;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

        private static final DateTimeFormatter ISO_OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final PublicPostMapper publicPostMapper;

        @Value("${app.frontend.base-url:http://localhost:3000}")
        private String frontendBaseUrl;

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

    @Override
    @Cacheable(value = "publicJobsPages", key = "T(String).format('%s|%s', #slug, #postStatus)")
    public List<PostResponse> getPublicPostsBySlugAndStatus(String slug, String postStatus) {
        return postRepository
                .findByPostSlugIgnoreCaseAndPostStatus(slug, PostStatus.fromValue(postStatus))
                .stream()
                .map(postMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "publicSitemapUrls")
    public List<SitemapUrlResponse> getSitemapUrls() {
        final String siteBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;

        return postRepository.findSitemapPostsByStatus(PostStatus.PUBLISHED)
                .stream()
                .map(post -> SitemapUrlResponse.builder()
                        .loc(siteBaseUrl + "/" + post.getPostSlug())
                        .lastModified(post.getUpdatedAt().atOffset(ZoneOffset.UTC).format(ISO_OFFSET_FORMATTER))
                                                .imageLoc(resolveFirstImageUrl(siteBaseUrl, post.getImageUrls()))
                        .build())
                .toList();
    }

        private static String resolveFirstImageUrl(String siteBaseUrl, String imageUrlsCsv) {
                if (imageUrlsCsv == null || imageUrlsCsv.isBlank()) {
                        return null;
                }

                return Arrays.stream(imageUrlsCsv.split(","))
                                .map(String::trim)
                                .filter(token -> !token.isBlank())
                                .findFirst()
                                .map(token -> siteBaseUrl + "/uploads/" + token)
                                .orElse(null);
        }
}