package com.sarkari.post.interfaces.rest;

import com.sarkari.common.response.ApiResponse;
import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.dto.response.PostResponse;
import com.sarkari.post.application.service.PublicPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "System", description = "Basic system endpoints")
public class PublicApiController {

        private static final String JOBS_CACHE_CONTROL = "public, max-age=30, s-maxage=120, stale-while-revalidate=300";
        private static final String SLUG_CACHE_CONTROL = "public, max-age=60, s-maxage=300, stale-while-revalidate=600";

    private final PublicPostService publicPostService;


    @GetMapping("/jobs")
    @Operation(summary = "Jobs", description = "Fetch paginated jobs in public API response format")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> getPublicPosts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String postType,
            @RequestParam(required = false) String postStatus,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") @Parameter(description = "Sort direction: asc or desc") String sortDir
    ) {
            log.info("Public jobs request received search={} postType={} postStatus={} page={} size={} sortBy={} sortDir={}",
                search, postType, postStatus, page, size, sortBy, sortDir);
        PagedResponse<Map<String, Object>> payload = publicPostService.getPublicJobs(search, postType, postStatus, page, size, sortBy, sortDir);

                        return ResponseEntity.ok()
                                .header(HttpHeaders.CACHE_CONTROL, JOBS_CACHE_CONTROL)
                                .body(ApiResponse.<PagedResponse<Map<String, Object>>>builder()
                .success(true)
                .message(postType + " fetched successfully")
                .data(payload)
                                .build());
    }

    @GetMapping("/latest-update")
    @Operation(summary = "latest-update", description = "Fetch paginated latest-update by status in public API response format")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> getPublicPostsByStatus(
            @RequestParam(required = false) String postStatus,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") @Parameter(description = "Sort direction: asc or desc") String sortDir
    ) {
            log.info("Latest update request received postStatus={} page={} size={} sortBy={} sortDir={}",
                postStatus, page, size, sortBy, sortDir);
        PagedResponse<Map<String, Object>> payload = publicPostService.getPublicPostsByStatus(postStatus, page, size, sortBy, sortDir);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, JOBS_CACHE_CONTROL)
                .body(ApiResponse.<PagedResponse<Map<String, Object>>>builder()
                .success(true)
                .message("Posts fetched successfully")
                .data(payload)
                .build());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Posts by slug and status", description = "Fetch all posts by slug and postStatus in public API response format")
        public ResponseEntity<ApiResponse<List<PostResponse>>> getPublicPostsBySlugAndStatus(
            @PathVariable("slug") String slug
    ) {
            log.info("Posts by slug request received slug={} status={}", slug, "Published");
                List<PostResponse> payload = publicPostService.getPublicPostsBySlugAndStatus(slug, "Published");
            
                return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, SLUG_CACHE_CONTROL)
                .body(ApiResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Posts fetched successfully")
                .data(payload)
                .build());
    }

}