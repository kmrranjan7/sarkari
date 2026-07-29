package com.sarkari.post.application.service;

import com.sarkari.post.domain.entity.PushSubscription;
import com.sarkari.post.domain.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Transactional
    public PushSubscription register(String token) {
        String normalizedToken = token.trim();
        return pushSubscriptionRepository.findByToken(normalizedToken)
                .orElseGet(() -> {
                    PushSubscription subscription = new PushSubscription();
                    subscription.setToken(normalizedToken);
                    return pushSubscriptionRepository.save(subscription);
                });
    }
}
