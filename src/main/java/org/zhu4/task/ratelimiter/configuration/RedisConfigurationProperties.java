package org.zhu4.task.ratelimiter.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "org.zhu4.redis")
public record RedisConfigurationProperties(String hostName, String port) {
}
