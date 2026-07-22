package com.sarkari.post.interfaces.rest;

import com.sarkari.common.response.ApiResponse;
import com.sarkari.post.application.dto.request.CreatePostRequest;
import com.sarkari.post.application.dto.request.UpdatePostRequest;
import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.dto.response.PostResponse;
import com.sarkari.post.application.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Posts", description = "CRUD APIs for dashboard posts (Job/Admit/Exam/Result)")
public class PostController {

    private final PostService service;

    @PostMapping
    @Operation(summary = "Create post", description = "Creates a dashboard post record")
    public ResponseEntity<ApiResponse<PostResponse>> create(@Valid @RequestBody CreatePostRequest request) {
        log.info("Create post request received postTitle={} postType={}", request.getPostTitle(), request.getPostType());
        PostResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<PostResponse>builder()
                .success(true)
                .message("Post created successfully")
                .data(created)
                .build());
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get post by id", description = "Fetch a post using business id (POST-YYYY-XXXXX)")
    public ResponseEntity<ApiResponse<PostResponse>> getById(@PathVariable @Parameter(description = "Business post id") String postId) {
        log.info("Get post by id request received postId={}", postId);
        PostResponse result = service.getByPostId(postId);
        return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                .success(true)
                .message("Post fetched successfully")
                .data(result)
                .build());
    }

    @GetMapping
    @Operation(summary = "List posts", description = "Fetch paginated posts with optional search and type filter")
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String postType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
            log.info("List posts request received search={} postType={} page={} size={} sortBy={} sortDir={}",
            search, postType, page, size, sortBy, sortDir);
        PagedResponse<PostResponse> result = service.getAll(search, postType, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.<PagedResponse<PostResponse>>builder()
                .success(true)
                .message("Posts fetched successfully")
                .data(result)
                .build());
    }

    @PutMapping("/{postId}")
        @Operation(summary = "Update post", description = "Updates an existing post by business id")
    public ResponseEntity<ApiResponse<PostResponse>> update(
            @PathVariable @Parameter(description = "Business post id") String postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        log.info("Update post request received postId={} postTitle={} postType={}", postId, request.getPostTitle(), request.getPostType());
        PostResponse updated = service.update(postId, request);
        return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                .success(true)
                .message("Post updated successfully")
                .data(updated)
                .build());
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete post", description = "Deletes post by business id")
    public ResponseEntity<Void> delete(@PathVariable @Parameter(description = "Business post id") String postId) {
        log.info("Delete post request received postId={}", postId);
        service.delete(postId);
        return ResponseEntity.noContent().build();
    }
}
