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
@Tag(name = "System", description = "Basic system endpoints")
public class PublicApiController {

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
        PagedResponse<Map<String, Object>> payload = publicPostService.getPublicJobs(search, postType, postStatus, page, size, sortBy, sortDir);

            return ResponseEntity.ok(ApiResponse.<PagedResponse<Map<String, Object>>>builder()
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
        PagedResponse<Map<String, Object>> payload = publicPostService.getPublicPostsByStatus(postStatus, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.<PagedResponse<Map<String, Object>>>builder()
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
                List<PostResponse> payload = publicPostService.getPublicPostsBySlugAndStatus(slug, "Published");

                return ResponseEntity.ok(ApiResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Posts fetched successfully")
                .data(payload)
                .build());
    }

}