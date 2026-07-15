package com.sarkari.post.interfaces.rest;

import com.sarkari.common.response.ApiResponse;
import com.sarkari.post.application.dto.response.ImageUploadResponse;
import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.service.impl.PostImageCleanupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Manage uploaded images")
public class ImageController {

    private final PostImageCleanupService postImageCleanupService;

    @GetMapping
        @Operation(summary = "List uploaded images", description = "Fetch paginated uploaded image file names")
        public ResponseEntity<ApiResponse<PagedResponse<String>>> getAllImages(
                        @RequestParam(defaultValue = "1") @Parameter(description = "Page number (1-based)") int page,
                        @RequestParam(defaultValue = "20") @Parameter(description = "Page size (1-20)") int size,
                        @RequestParam(defaultValue = "all") @Parameter(description = "Filter mode: all, matched, unmatched") String mode
        ) {
                PagedResponse<String> images = postImageCleanupService.listUploadedImagesPaged(page, size, mode);
                return ResponseEntity.ok(ApiResponse.<PagedResponse<String>>builder()
                .success(true)
                .message("Images fetched successfully")
                .data(images)
                .build());
    }

    @DeleteMapping("/{imageName}")
    @Operation(summary = "Delete uploaded image", description = "Delete image from uploads folder by image name")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable
            @Parameter(description = "Image file name like 1783799722544-f4612dba-0791-497a-977e-602ee377eb35.png")
            String imageName
    ) {
        postImageCleanupService.deleteUploadedImage(imageName);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Image deleted successfully")
                .data(null)
                .build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image", description = "Upload image to uploads folder and return image URL")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @RequestParam("file") @Parameter(description = "Image file") MultipartFile file
    ) {
        ImageUploadResponse uploaded = postImageCleanupService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.<ImageUploadResponse>builder()
                .success(true)
                .message("Image uploaded successfully")
                .data(uploaded)
                .build());
    }
}
