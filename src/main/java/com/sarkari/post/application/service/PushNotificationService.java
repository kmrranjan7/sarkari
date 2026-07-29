package com.sarkari.post.application.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.WebpushConfig;
import com.sarkari.post.domain.repository.PushSubscriptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.firebase", name = "enabled", havingValue = "true")
@Slf4j
public class PushNotificationService {

    private static final int MAX_TOKENS_PER_REQUEST = 500;

    private final FirebaseApp firebaseApp;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    public BroadcastResult broadcast(String title, String body, String url) {
        List<String> tokens = pushSubscriptionRepository.findAll().stream()
                .map(subscription -> subscription.getToken())
                .filter(token -> token != null && !token.isBlank())
                .toList();

        int sentCount = 0;
        int failedCount = 0;
        String failureReason = "";
        for (int start = 0; start < tokens.size(); start += MAX_TOKENS_PER_REQUEST) {
            List<String> batch = tokens.subList(start, Math.min(start + MAX_TOKENS_PER_REQUEST, tokens.size()));
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batch)
                    .setWebpushConfig(WebpushConfig.builder()
                            .putHeader("Urgency", "high")
                            .putData("url", url)
                            .build())
                    .putData("title", title)
                    .putData("body", body)
                    .putData("url", url)
                    .build();
            try {
                BatchResponse response = FirebaseMessaging.getInstance(firebaseApp).sendEachForMulticast(message);
                sentCount += response.getSuccessCount();
                failedCount += response.getFailureCount();

                if (response.getFailureCount() > 0 && failureReason.isBlank()) {
                    failureReason = response.getResponses().stream()
                            .filter(sendResponse -> !sendResponse.isSuccessful())
                            .map(SendResponse::getException)
                            .filter(exception -> exception != null)
                            .map(Exception::getMessage)
                            .findFirst()
                            .orElse("Firebase rejected one or more subscriber tokens.");
                }
            } catch (FirebaseMessagingException exception) {
                throw new IllegalStateException("Unable to deliver push notification.", exception);
            }
        }

        if (failedCount > 0) {
            log.warn("Push broadcast delivered={} failed={} reason={}", sentCount, failedCount, failureReason);
        }

        return new BroadcastResult(sentCount, failedCount, failureReason);
    }

    public record BroadcastResult(int sentCount, int failedCount, String failureReason) { }
}
