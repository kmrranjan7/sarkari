package com.sarkari.post.interfaces.rest;

import com.sarkari.common.response.ApiResponse;
import com.sarkari.post.application.dto.request.CreateContactRequest;
import com.sarkari.post.application.dto.response.ContactResponse;
import com.sarkari.post.application.dto.response.PagedResponse;
import com.sarkari.post.application.service.ContactService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contact", description = "Contact form submission APIs")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @Operation(summary = "Create contact", description = "Save contact submission")
    public ResponseEntity<ApiResponse<ContactResponse>> create(@Valid @RequestBody CreateContactRequest request) {
        log.info("Create contact request received fullName={} email={}", request.getFullName(), request.getEmail());
        ContactResponse created = contactService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ContactResponse>builder()
                .success(true)
                .message("Contact saved successfully")
                .data(created)
                .build());
    }

    @GetMapping
    @Operation(summary = "List contacts", description = "Fetch paginated contact submissions")
    public ResponseEntity<ApiResponse<PagedResponse<ContactResponse>>> getAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") @Parameter(description = "Sort direction: asc or desc") String sortDir
    ) {
        PagedResponse<ContactResponse> contacts = contactService.getAll(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.<PagedResponse<ContactResponse>>builder()
                .success(true)
                .message("Contacts fetched successfully")
                .data(contacts)
                .build());
    }
}
