package com.sarkari.post.interfaces.rest;

import com.sarkari.common.response.ApiResponse;
import com.sarkari.post.application.dto.request.CreatePushSubscriptionRequest;
import com.sarkari.post.application.service.PushSubscriptionService;
import com.sarkari.post.domain.entity.PushSubscription;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push-subscriptions")
@RequiredArgsConstructor
public class PushSubscriptionController {

    private final PushSubscriptionService pushSubscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody CreatePushSubscriptionRequest request) {
        PushSubscription subscription = pushSubscriptionService.register(request.getToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>builder()
                        .success(true)
                        .message("Push notifications enabled")
                        .build());
    }
}
