package org.zhu4.task.ratelimiter.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.zhu4.task.ratelimiter.cache.RateLimiterFromScratchCache;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class FixedWindowRateLimiterFromScracheCacheTest {

    private FixedWindowRateLimiter rateLimiter;

    @BeforeEach
    public void initialize() {
        final var cache = new RateLimiterFromScratchCache(TestValues.WINDOW_INTERVAL * 3);
        rateLimiter = new FixedWindowRateLimiter(TestValues.RATE_LIMITER_NAME, TestValues.MAX_REQUESTS, TestValues.WINDOW_INTERVAL, cache);
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
        assertTrue(rateLimiter.rateLimit("someKey"));
    }

    @Test
    public void returnFalse_whenMoreThanMaxRequests() {
        for (int i = 0; i < TestValues.MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.rateLimit(TestValues.USER_KEY));
        }

        assertFalse(rateLimiter.rateLimit(TestValues.USER_KEY));

        assertTrue(rateLimiter.rateLimit("anotherKey"));
    }

    @Test
    public void returnTrue_whenRecovered() {
        for (int i = 0; i < TestValues.MAX_REQUESTS; i++) {
            assertTrue(rateLimiter.rateLimit(TestValues.USER_KEY));
        }
        assertFalse(rateLimiter.rateLimit(TestValues.USER_KEY));

        Awaitility.await().atMost(Duration.of(TestValues.TIMEOUT, ChronoUnit.MILLIS))
                .until(() -> rateLimiter.rateLimit(TestValues.USER_KEY));
    }

    @Test
    public void passesAtMostMaxRequests_concurrentTest() {
        final var futures = new ArrayList<CompletableFuture<Boolean>>();

        for (int i = 0; i < TestValues.MAX_REQUESTS * 10; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> rateLimiter.rateLimit(TestValues.USER_KEY)));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[(int) TestValues.MAX_REQUESTS * 10]))
                .exceptionally(ex -> null)
                .join();

        final var resultSum = futures.stream().mapToInt(future -> {
            try {
                return future.get() ? 1 : 0;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).sum();

        Assertions.assertEquals(TestValues.MAX_REQUESTS, resultSum);
    }
}
