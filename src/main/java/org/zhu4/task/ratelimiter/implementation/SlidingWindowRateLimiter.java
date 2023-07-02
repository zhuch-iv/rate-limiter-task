package org.zhu4.task.ratelimiter.implementation;


import org.zhu4.task.ratelimiter.RateLimiter;
import org.zhu4.task.ratelimiter.cache.RateLimiterCache;

import java.util.Objects;

public class SlidingWindowRateLimiter implements RateLimiter {

    private final String name;
    private final long maximumRequests;
    private final long intervalInMillis;
    private final RateLimiterCache cache;

    public SlidingWindowRateLimiter(String name, long maximumRequests, long intervalInMillis, RateLimiterCache cache) {
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

        final var currentWindowReqCount = cache.get(currentWindowKey);
        if (currentWindowReqCount >= maximumRequests) {
            return false;
        }

        final var lastWindowKey = name + ":" + userKey + ":" + (now - intervalInMillis) / intervalInMillis;

        final double lastWindowReqCount = cache.get(lastWindowKey);
        final double elapsedTimePercentage = ((double) (now % intervalInMillis)) / ((double) intervalInMillis);

        final var compare = Double.compare(
                lastWindowReqCount * (1 - elapsedTimePercentage) + currentWindowReqCount, maximumRequests
        );
        if (compare < 0) {
            if (!cache.compareAndSet(currentWindowKey, currentWindowReqCount, currentWindowReqCount + 1)) {
                return rateLimit(userKey);
            } else {
                return true;
            }
        }
        return false;
    }
}
