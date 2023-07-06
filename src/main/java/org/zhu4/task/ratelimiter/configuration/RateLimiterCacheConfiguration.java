package org.zhu4.task.ratelimiter.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.zhu4.task.ratelimiter.cache.RateLimiterCache;
import org.zhu4.task.ratelimiter.cache.RateLimiterCaffeineCache;
import org.zhu4.task.ratelimiter.cache.RateLimiterFromScratchCache;
import org.zhu4.task.ratelimiter.cache.RateLimiterRedissonCache;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class RateLimiterCacheConfiguration {

    @Bean
    @ConditionalOnMissingBean(RateLimiterCache.class)
    public RateLimiterCache defaultCache(RateLimiterConfigurationProperties properties) {
        return new RateLimiterCaffeineCache(
                Caffeine.newBuilder()
                        .expireAfterWrite(properties.cache().expireAfterMillis(), TimeUnit.MILLISECONDS)
        );
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "org.zhu4.rate-limiter.cache", name = "type", havingValue = "caffeine")
    public RateLimiterCache caffeineCache(RateLimiterConfigurationProperties properties) {
        return new RateLimiterCaffeineCache(
                Caffeine.newBuilder()
                        .expireAfterWrite(properties.cache().expireAfterMillis(), TimeUnit.MILLISECONDS)
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "org.zhu4.redis", name = "hostName")
    public RedissonClient redissonClient(RedisConfigurationProperties properties) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + properties.hostName() + ":" + properties.port());
        return Redisson.create(config);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "org.zhu4.rate-limiter.cache", name = "type", havingValue = "redisson")
    public RateLimiterCache redissonCache(
            RateLimiterConfigurationProperties properties,
            RedissonClient redissonClient
    ) {
        return new RateLimiterRedissonCache(redissonClient, properties.cache().expireAfterMillis());
    }

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "org.zhu4.rate-limiter.cache", name = "type", havingValue = "fromScratch")
    public RateLimiterCache fromScratchCache(RateLimiterConfigurationProperties properties) {
        return new RateLimiterFromScratchCache(properties.cache().expireAfterMillis());
    }
}
