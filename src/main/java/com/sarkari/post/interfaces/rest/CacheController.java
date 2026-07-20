package com.sarkari.post.interfaces.rest;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.sarkari.common.cache.CacheVersionTracker;
import com.sarkari.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/cache")
@RequiredArgsConstructor
@Tag(name = "Cache", description = "Cache debug and health APIs")
public class CacheController {

    private static final String CACHE_ADMIN_HEADER = "X-CACHE-ADMIN-KEY";

    private final CacheManager cacheManager;
    private final CacheVersionTracker cacheVersionTracker;

    @Value("${app.cache.admin-key:}")
    private String cacheAdminKey;

    @GetMapping("/status")
    @Operation(summary = "Cache status", description = "Returns cache names, version, estimated sizes and hit/miss stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCacheStatus() {
        List<String> cacheNames = cacheManager.getCacheNames().stream().sorted().toList();
        Map<String, Object> perCache = new LinkedHashMap<>();

        for (String cacheName : cacheNames) {
            org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
            Map<String, Object> entry = new LinkedHashMap<>();

            if (!(springCache instanceof CaffeineCache caffeineCache)) {
                entry.put("type", springCache == null ? "unknown" : springCache.getClass().getSimpleName());
                entry.put("estimatedSize", null);
                entry.put("stats", null);
                perCache.put(cacheName, entry);
                continue;
            }

            Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
            CacheStats stats = nativeCache.stats();

            Map<String, Object> statsMap = new LinkedHashMap<>();
            statsMap.put("requestCount", stats.requestCount());
            statsMap.put("hitCount", stats.hitCount());
            statsMap.put("missCount", stats.missCount());
            statsMap.put("hitRate", stats.hitRate());
            statsMap.put("missRate", stats.missRate());
            statsMap.put("evictionCount", stats.evictionCount());

            entry.put("type", "caffeine");
            entry.put("estimatedSize", nativeCache.estimatedSize());
            entry.put("stats", statsMap);
            perCache.put(cacheName, entry);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("cacheVersion", cacheVersionTracker.get());
        payload.put("cacheNames", cacheNames);
        payload.put("caches", perCache);

        log.info("Cache status requested cacheCount={} version={}", cacheNames.size(), cacheVersionTracker.get());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Cache status fetched successfully")
                .data(payload)
                .build());
    }

    @PostMapping("/clear-all")
    @Operation(summary = "Clear all caches", description = "Clears all configured caches. Requires X-CACHE-ADMIN-KEY header")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearAllCaches(
            @RequestHeader(value = CACHE_ADMIN_HEADER, required = false) String adminKey
    ) {
        if (!isAuthorized(adminKey)) {
            log.warn("Cache clear-all denied due to invalid admin key");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("Forbidden: invalid cache admin key")
                            .data(null)
                            .build());
        }

        int clearedCaches = 0;
        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                continue;
            }

            cache.clear();
            clearedCaches++;
        }

        long nextVersion = cacheVersionTracker.bump("cache:clear-all");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("clearedCaches", clearedCaches);
        payload.put("cacheVersion", nextVersion);

        log.info("Cache clear-all executed clearedCaches={} newVersion={}", clearedCaches, nextVersion);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("All caches cleared successfully")
                .data(payload)
                .build());
    }

    private boolean isAuthorized(String adminKey) {
        return cacheAdminKey != null
                && !cacheAdminKey.isBlank()
                && cacheAdminKey.equals(adminKey);
    }
}
