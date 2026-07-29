package com.sarkari.post.interfaces.rest;

import com.google.firebase.FirebaseApp;
import com.sarkari.common.response.ApiResponse;
import com.sarkari.post.application.dto.request.BroadcastPushNotificationRequest;
import com.sarkari.post.application.service.PushNotificationService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/push-notifications")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.firebase", name = "enabled", havingValue = "true")
public class InternalPushNotificationController {

    private final PushNotificationService pushNotificationService;

    @Value("${app.push.dispatch-secret}")
    private String dispatchSecret;

    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> broadcast(
            @RequestHeader(value = "X-Push-Dispatch-Secret", defaultValue = "") String requestSecret,
            @Valid @RequestBody BroadcastPushNotificationRequest request
    ) {
        if (dispatchSecret.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResponse.<Map<String, Integer>>builder()
                    .success(false)
                    .message("PUSH_DISPATCH_SECRET is not configured in Spring Boot.")
                    .build());
        }

        if (!MessageDigest.isEqual(
                dispatchSecret.getBytes(StandardCharsets.UTF_8),
                requestSecret.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.<Map<String, Integer>>builder()
                    .success(false)
                    .message("PUSH_DISPATCH_SECRET does not match the Next.js value.")
                    .build());
        }

        PushNotificationService.BroadcastResult result = pushNotificationService.broadcast(
                request.getTitle(), request.getBody(), request.getUrl());
        return ResponseEntity.ok(ApiResponse.<Map<String, Integer>>builder()
                .success(true)
                .message("Push notification sent")
                .data(Map.of("sent", result.sentCount(), "failed", result.failedCount()))
                .build());
    }
}
