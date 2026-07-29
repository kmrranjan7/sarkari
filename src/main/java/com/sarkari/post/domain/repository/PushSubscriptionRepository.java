package com.sarkari.post.domain.repository;

import com.sarkari.post.domain.entity.PushSubscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    Optional<PushSubscription> findByToken(String token);
}
