package org.zhu4.task.ratelimiter.cache;

import org.springframework.scheduling.annotation.Scheduled;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiterFromScratchCache implements RateLimiterCache {

    private final ConcurrentSkipListMap<Long, Queue<String>> timestamps = new ConcurrentSkipListMap<>();
    private final ConcurrentMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final long expireAfterMillis;

    public RateLimiterFromScratchCache(long expireAfterMillis) {
        this.expireAfterMillis = expireAfterMillis;
    }

    @Override
    public long get(String key) {
        return getOrDefault(key).get();
    }

    @Override
    public boolean compareAndSet(String key, long expected, long update) {
        return getOrDefault(key).compareAndSet(expected, update);
    }

    @Override
    public long incrementAndGet(String key) {
        return getOrDefault(key).incrementAndGet();
    }

    private AtomicLong getOrDefault(String key) {
        if (counters.containsKey(key)) {
            return counters.get(key);
        }
        final var old = counters.putIfAbsent(key, new AtomicLong(0));
        if (old == null) {
            final var timestamp = System.currentTimeMillis();
            timestamps.getOrDefault(timestamp, new ConcurrentLinkedQueue<>()).add(key);
        }
        return counters.get(key);
    }

    @Scheduled(fixedDelayString = "${org.zhu4.rate-limiter.cache.expireAfterMillis}")
    public void cleanUp() {
        final var expiredKeys = timestamps.subMap(0L, System.currentTimeMillis() - expireAfterMillis);
        expiredKeys.values().stream()
                .flatMap(Collection::stream)
                .forEach(counters::remove);
        expiredKeys.clear();
    }
}
