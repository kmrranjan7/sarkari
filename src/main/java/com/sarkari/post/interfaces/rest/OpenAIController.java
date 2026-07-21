package com.sarkari.post.interfaces.rest;

import com.sarkari.common.response.ApiResponse;
import com.sarkari.post.application.dto.request.OpenAIRequest;
import com.sarkari.post.application.dto.response.OpenAIResponse;
import com.sarkari.post.application.service.OpenAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Message send and response APIs")
public class OpenAIController {

    private final OpenAIService openAIService;

    @PostMapping("/send")
    @Operation(summary = "Send message", description = "Accepts a message and returns response message details")
    public ResponseEntity<ApiResponse<OpenAIResponse>> sendMessage(@Valid @RequestBody OpenAIRequest request) {
        String message = request.getMessage();
        log.info("Send message request received messageLength={}", message == null ? 0 : message.length());

        OpenAIResponse payload = openAIService.send(message);

        return ResponseEntity.ok(ApiResponse.<OpenAIResponse>builder()
                .success(true)
                .message("Message processed successfully")
                .data(payload)
                .build());
    }
}
