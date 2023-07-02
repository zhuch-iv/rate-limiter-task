package org.zhu4.task.ratelimiter.implementation;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.zhu4.task.ratelimiter.cache.RateLimiterCaffeineCache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.zhu4.task.ratelimiter.implementation.TestValues.*;

public class SlidingWindowRateLimiterTest {

    private SlidingWindowRateLimiter rateLimiter;

    @BeforeEach
    public void initialize() {
        final var caffeine = Caffeine.newBuilder()
                .expireAfterWrite(TestValues.WINDOW_INTERVAL * 3, TimeUnit.MILLISECONDS);
        final var cache = new RateLimiterCaffeineCache(caffeine);
        rateLimiter = new SlidingWindowRateLimiter(RATE_LIMITER_NAME, MAX_REQUESTS, WINDOW_INTERVAL, cache);
    }

    @Test
    public void nullPointerTest() {
        assertThrows(
                NullPointerException.class,
                () -> rateLimiter.rateLimit(null),
                "userKey must not be null"
        );
    }

    @Test
    public void basicTest() {
        assertTrue(rateLimiter.rateLimit(USER_KEY));
    }

    @Test
    public void returnFalse_whenMoreThanMaxRequests() {
        for (int i = 0; i < TestValues.MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.rateLimit(TestValues.USER_KEY));
        }

        assertFalse(rateLimiter.rateLimit(USER_KEY));

        assertTrue(rateLimiter.rateLimit("anotherKey"));
    }

    @Test
    public void returnTrue_whenRecovered() {
        for (int i = 0; i < TestValues.MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.rateLimit(TestValues.USER_KEY));
        }
        assertFalse(rateLimiter.rateLimit(USER_KEY));

        Awaitility.await().atMost(Duration.of(TIMEOUT, ChronoUnit.MILLIS))
                .until(() -> rateLimiter.rateLimit(USER_KEY));
    }

    @Test
    public void passesAtMostMaxRequests_concurrentTest() {
        final var futures = new ArrayList<CompletableFuture<Boolean>>();

        for (int i = 0; i < TestValues.MAX_REQUESTS * 10; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> rateLimiter.rateLimit(TestValues.USER_KEY)));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[(int) MAX_REQUESTS * 10]))
                .exceptionally(ex -> null)
                .join();

        final var resultSum = futures.stream().mapToInt(future -> {
            try {
                return future.get() ? 1 : 0;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).sum();

        assertEquals(MAX_REQUESTS, resultSum);
    }
}
