package org.zhu4.task.ratelimiter.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "org.zhu4.rate-limiter")
public record RateLimiterConfigurationProperties(
        RateLimiterCacheConfig cache, Map<String, RateLimiterConfig> configs
) {
    public RateLimiterConfigurationProperties {
        if (configs == null) {
            throw new IllegalArgumentException("org.zhu4.rate-limiter.configs cannot be null");
        }
        if (configs.get("default") == null) {
            throw new IllegalArgumentException("org.zhu4.rate-limiter.configs.default cannot be null");
        }
        final var defaultConfig = configs.get("default");
        if (defaultConfig.intervalInMillis <= 0) {
            throw new IllegalArgumentException(
                    "org.zhu4.rate-limiter.configs.default.intervalInMillis cannot be less then zero"
            );
        }
        if (defaultConfig.maximumRequests <= 0) {
            throw new IllegalArgumentException(
                    "org.zhu4.rate-limiter.configs.default.maximumRequests cannot be less then zero"
            );
        }
    }

    public record RateLimiterCacheConfig(RateLimiterCacheType type, long expireAfterMillis) { }

    public record RateLimiterConfig(
            long maximumRequests, long intervalInMillis, RateLimiterAlgorithm algorithm
    ) { }

    public enum RateLimiterAlgorithm {
        FIXED_WINDOW("fixed-window"), SLIDING_WINDOW("sliding-window");

        private final String name;

        RateLimiterAlgorithm(String name) {
            this.name = name;
        }
    }

    public enum RateLimiterCacheType {

        CAFFEINE("caffeine"), REDISSON("redisson");

        private final String name;

        RateLimiterCacheType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
