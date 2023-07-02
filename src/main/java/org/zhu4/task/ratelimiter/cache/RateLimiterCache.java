package org.zhu4.task.ratelimiter.cache;

public interface RateLimiterCache {

    long get(final String key);

    boolean compareAndSet(final String key, final long expected, final long update);

    long incrementAndGet(final String key);

}
