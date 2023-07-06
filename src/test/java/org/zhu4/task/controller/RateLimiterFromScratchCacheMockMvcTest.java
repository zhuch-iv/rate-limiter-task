package org.zhu4.task.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.zhu4.task.controller.TestValues.*;
import static org.zhu4.task.controller.TestValues.REMOTE_ADDR_FOURTH;

@SpringBootTest
@ActiveProfiles("scratch")
public class RateLimiterFromScratchCacheMockMvcTest extends AbstractMockMvcTest {

    @Value("${org.zhu4.rate-limiter.configs.default.maximumRequests}")
    private long maxRequests;
    @Value("${org.zhu4.rate-limiter.configs.greet.maximumRequests}")
    private long greetMaxRequests;

    @Test
    public void shouldReturnDefaultMessageHello() throws Exception {
        makeRequest(HELLO_PATH, REMOTE_ADDR_FIRST).andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello, World!")));
    }

    @Test
    public void shouldReturnDefaultMessageGreet() throws Exception {
        makeRequest(GREET_PATH, REMOTE_ADDR_FIRST).andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello, World!")));
    }

    @Test
    public void returnBadGateway_whenMoreThanMaxRequests_hello() throws Exception {
        for (int i = 0; i < maxRequests; i++) {
            makeRequest(HELLO_PATH, REMOTE_ADDR_SECOND).andExpect(status().isOk())
                    .andExpect(content().string(containsString("Hello, World!")));
        }
        makeRequest(HELLO_PATH, REMOTE_ADDR_SECOND).andExpect(status().isBadGateway());
    }

    @Test
    public void returnBadGateway_whenMoreThanMaxRequests_greet() throws Exception {
        for (int i = 0; i < greetMaxRequests; i++) {
            makeRequest(GREET_PATH, REMOTE_ADDR_SECOND).andExpect(status().isOk())
                    .andExpect(content().string(containsString("Hello, World!")));
        }
        makeRequest(GREET_PATH, REMOTE_ADDR_SECOND).andExpect(status().isBadGateway());
    }

    @Test
    public void passesAtMostMaxRequests_concurrentTest_hello() {
        final var futures = new ArrayList<CompletableFuture<Boolean>>();

        for (int i = 0; i < maxRequests * 10; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return makeRequestAndGetStatusCode(HELLO_PATH, REMOTE_ADDR_THIRD) == 200;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[(int) maxRequests * 10]))
                .exceptionally(ex -> null)
                .join();

        final var resultSum = futures.stream().mapToInt(future -> {
            try {
                return future.get() ? 1 : 0;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).sum();

        Assertions.assertEquals(maxRequests, resultSum);
    }

    @Test
    public void passesAtMostMaxRequests_concurrentTest_greet() {
        final var futures = new ArrayList<CompletableFuture<Boolean>>();

        for (int i = 0; i < greetMaxRequests * 10; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return makeRequestAndGetStatusCode(GREET_PATH, REMOTE_ADDR_THIRD) == 200;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[(int) greetMaxRequests * 10]))
                .exceptionally(ex -> null)
                .join();

        final var resultSum = futures.stream().mapToInt(future -> {
            try {
                return future.get() ? 1 : 0;
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).sum();

        Assertions.assertEquals(greetMaxRequests, resultSum);
    }

    @Test
    public void passesWhenRecoveredHello() throws Exception {
        for (int i = 0; i < maxRequests; i++) {
            makeRequest(HELLO_PATH, REMOTE_ADDR_FOURTH).andExpect(status().isOk())
                    .andExpect(content().string(containsString("Hello, World!")));
        }

        makeRequest(HELLO_PATH, REMOTE_ADDR_FOURTH).andExpect(status().isBadGateway());

        Awaitility.await().atMost(Duration.of(TIMEOUT, ChronoUnit.MILLIS))
                .until(() -> makeRequestAndGetStatusCode(HELLO_PATH, REMOTE_ADDR_FOURTH) == 200);
    }

    @Test
    public void passesWhenRecoveredGreet() throws Exception {
        for (int i = 0; i < greetMaxRequests; i++) {
            makeRequest(GREET_PATH, REMOTE_ADDR_FOURTH).andExpect(status().isOk())
                    .andExpect(content().string(containsString("Hello, World!")));
        }

        makeRequest(GREET_PATH, REMOTE_ADDR_FOURTH).andExpect(status().isBadGateway());

        Awaitility.await().atMost(Duration.of(GREET_TIMEOUT, ChronoUnit.MILLIS))
                .until(() -> makeRequestAndGetStatusCode(GREET_PATH, REMOTE_ADDR_FOURTH) == 200);
    }
}
