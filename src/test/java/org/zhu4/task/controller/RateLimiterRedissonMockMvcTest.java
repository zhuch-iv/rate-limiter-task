package org.zhu4.task.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.zhu4.task.controller.TestValues.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("redis")
@Testcontainers(disabledWithoutDocker = true)
public class RateLimiterRedissonMockMvcTest extends AbstractMockMvcTest {

    static {
        GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
                .withExposedPorts(6379);
        redis.start();
        System.setProperty("org.zhu4.redis.hostName", redis.getHost());
        System.setProperty("org.zhu4.redis.port", redis.getMappedPort(6379).toString());
    }

    @Value("${org.zhu4.rate-limiter.configs.default.maximumRequests}")
    private long maxRequests;

    @Test
    public void shouldReturnDefaultMessageHello() throws Exception {
        makeRequest(HELLO_PATH, REMOTE_ADDR_FIRST_REDIS).andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello, World!")));
    }

    @Test
    public void returnBadGateway_whenMoreThanMaxRequests_hello() throws Exception {
        for (int i = 0; i < maxRequests; i++) {
            makeRequest(HELLO_PATH, REMOTE_ADDR_SECOND_REDIS).andExpect(status().isOk())
                    .andExpect(content().string(containsString("Hello, World!")));
        }
        makeRequest(HELLO_PATH, REMOTE_ADDR_SECOND_REDIS).andExpect(status().isBadGateway());
    }

    @Test
    public void passesAtMostMaxRequests_concurrentTest() {
        final var futures = new ArrayList<CompletableFuture<Boolean>>();

        for (int i = 0; i < maxRequests * 10; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return makeRequestAndGetStatusCode(HELLO_PATH, REMOTE_ADDR_THIRD_REDIS) == 200;
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
    public void passesWhenRecovered() throws Exception {
        for (int i = 0; i < maxRequests; i++) {
            makeRequest(HELLO_PATH, REMOTE_ADDR_FOURTH_REDIS).andExpect(status().isOk())
                    .andExpect(content().string(containsString("Hello, World!")));
        }

        makeRequest(HELLO_PATH, REMOTE_ADDR_FOURTH_REDIS).andExpect(status().isBadGateway());

        Awaitility.await().atMost(Duration.of(TIMEOUT, ChronoUnit.MILLIS))
                .until(() -> makeRequestAndGetStatusCode(HELLO_PATH, REMOTE_ADDR_FOURTH_REDIS) == 200);
    }
}
