package org.zhu4.task.ratelimiter.cache;

import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RateLimiterRedissonCache implements RateLimiterCache {

    private final RedissonClient client;
    private final long expireAfterMillis;

    public RateLimiterRedissonCache(RedissonClient client, long expireAfterMillis) {
        this.client = client;
        this.expireAfterMillis = expireAfterMillis;
    }

    @Override
    public long get(final String key) {
        return client.getAtomicLong(key).get();
    }

    @Override
    public boolean compareAndSet(final String key, final long expected, final long update) {
        return client.getAtomicLong(key).compareAndSet(expected, update);
    }

    @Override
    public long incrementAndGet(final String key) {
        final var counter = client.getAtomicLong(key);
        counter.expire(Duration.of(expireAfterMillis, ChronoUnit.MILLIS));
        return counter.incrementAndGet();
    }
}
