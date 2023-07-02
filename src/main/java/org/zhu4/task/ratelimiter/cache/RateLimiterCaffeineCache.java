package org.zhu4.task.ratelimiter.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.concurrent.atomic.AtomicLong;

public class RateLimiterCaffeineCache implements RateLimiterCache {
    private final LoadingCache<String, AtomicLong> cache;

    public RateLimiterCaffeineCache(final Caffeine<Object, Object> caffeine) {
        cache = Caffeine.newBuilder()
                .build(key -> new AtomicLong(0));
    }

    @Override
    public long get(final String key) {
        return cache.get(key).get();
    }

    @Override
    public boolean compareAndSet(final String key, final long expected, final long update) {
        return cache.get(key).compareAndSet(expected, update);
    }

    @Override
    public long incrementAndGet(final String key) {
        return cache.get(key).incrementAndGet();
    }
}
