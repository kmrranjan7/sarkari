package com.sarkari.common.cache;

import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheVersionTracker {

    private final AtomicLong version = new AtomicLong(0);

    public long get() {
        return version.get();
    }

    public long bump(String reason) {
        long next = version.incrementAndGet();
        log.info("Cache version bumped to {} reason={}", next, reason);
        return next;
    }
}
