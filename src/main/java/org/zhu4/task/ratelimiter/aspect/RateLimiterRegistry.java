package org.zhu4.task.ratelimiter.aspect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zhu4.task.ratelimiter.RateLimiter;
import org.zhu4.task.ratelimiter.cache.RateLimiterCache;
import org.zhu4.task.ratelimiter.configuration.RateLimiterConfigurationProperties;
import org.zhu4.task.ratelimiter.implementation.FixedWindowRateLimiter;
import org.zhu4.task.ratelimiter.implementation.SlidingWindowRateLimiter;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimiterRegistry {

    private final RateLimiterCache cache;
    private final RateLimiterConfigurationProperties properties;
    private final ConcurrentHashMap<String, RateLimiter> entryMap = new ConcurrentHashMap<>();

    public RateLimiterRegistry(RateLimiterCache cache, RateLimiterConfigurationProperties properties) {
        this.cache = cache;
        this.properties = properties;
    }

    public RateLimiter getRateLimiter(String name) {
        if (entryMap.containsKey(name)) {
            return entryMap.get(name);
        } else {
            final var rateLimiter = createRateLimiter(name);
            entryMap.put(name, createRateLimiter(name));
            return rateLimiter;
        }
    }

    private RateLimiter createRateLimiter(String name) {
        final var defaultConfig = properties.configs().get("default");
        final var config = properties.configs().getOrDefault(name, defaultConfig);
        final var intervalInMillis =
                config.intervalInMillis() != 0 ? config.intervalInMillis() : defaultConfig.intervalInMillis();
        final var maximumRequests =
                config.maximumRequests() != 0 ? config.maximumRequests() : defaultConfig.maximumRequests();
        final var algorithm = config.algorithm() != null ? config.algorithm() : defaultConfig.algorithm();
        if (algorithm == null) {
            return new FixedWindowRateLimiter(name, maximumRequests, intervalInMillis, cache);
        }
        return switch (algorithm) {
            case FIXED_WINDOW -> new FixedWindowRateLimiter(name, maximumRequests, intervalInMillis, cache);
            case SLIDING_WINDOW -> new SlidingWindowRateLimiter(name, maximumRequests, intervalInMillis, cache);
        };
    }
}
