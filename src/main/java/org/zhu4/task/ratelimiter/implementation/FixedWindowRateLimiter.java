package org.zhu4.task.ratelimiter.implementation;

import org.zhu4.task.ratelimiter.RateLimiter;
import org.zhu4.task.ratelimiter.cache.RateLimiterCache;

import java.util.Objects;

public class FixedWindowRateLimiter implements RateLimiter {

    private final String name;
    private final long maximumRequests;
    private final long intervalInMillis;
    private final RateLimiterCache cache;

    public FixedWindowRateLimiter(String name, long maximumRequests, long intervalInMillis, RateLimiterCache cache) {
        this.name = name;
        this.maximumRequests = maximumRequests;
        this.intervalInMillis = intervalInMillis;
        this.cache = cache;
    }

    @Override
    public boolean rateLimit(final String userKey) {
        Objects.requireNonNull(userKey, "userKey must not be null");
        final var now = System.currentTimeMillis();
        final var currentWindowKey = name + ":" + userKey + ":" + (now / intervalInMillis);
        final var currentWindowReqCount = cache.incrementAndGet(currentWindowKey);
        return maximumRequests >= currentWindowReqCount;
    }
}
