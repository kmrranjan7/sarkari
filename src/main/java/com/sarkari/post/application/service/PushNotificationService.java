package com.sarkari.post.application.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.WebpushConfig;
import com.sarkari.post.domain.entity.PushSubscription;
import com.sarkari.post.domain.repository.PushSubscriptionRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findAll().stream()
                .filter(subscription -> subscription.getToken() != null && !subscription.getToken().isBlank())
                .toList();

        int sentCount = 0;
        int failedCount = 0;
        String failureReason = "";
        Set<Long> staleSubscriptionIds = new HashSet<>();
        for (int start = 0; start < subscriptions.size(); start += MAX_TOKENS_PER_REQUEST) {
            List<PushSubscription> batch = subscriptions.subList(start, Math.min(start + MAX_TOKENS_PER_REQUEST, subscriptions.size()));
            List<String> tokens = batch.stream().map(PushSubscription::getToken).toList();
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
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

                for (int index = 0; index < response.getResponses().size(); index++) {
                    SendResponse sendResponse = response.getResponses().get(index);
                    if (!sendResponse.isSuccessful() && shouldRemoveToken(sendResponse.getException())) {
                        staleSubscriptionIds.add(batch.get(index).getId());
                    }
                }

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

        if (!staleSubscriptionIds.isEmpty()) {
            pushSubscriptionRepository.deleteAllById(staleSubscriptionIds);
            log.info("Removed {} expired Firebase push subscription(s).", staleSubscriptionIds.size());
        }

        if (failedCount > 0) {
            log.warn("Push broadcast delivered={} failed={} reason={}", sentCount, failedCount, failureReason);
        }

        return new BroadcastResult(sentCount, failedCount, failureReason);
    }

    private boolean shouldRemoveToken(FirebaseMessagingException exception) {
        if (exception == null) {
            return false;
        }

        MessagingErrorCode errorCode = exception.getMessagingErrorCode();
        return errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT;
    }

    public record BroadcastResult(int sentCount, int failedCount, String failureReason) { }
}
