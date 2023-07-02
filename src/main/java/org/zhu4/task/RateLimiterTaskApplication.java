package org.zhu4.task;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@ConfigurationPropertiesScan("org.zhu4.task.ratelimiter.configuration")
public class RateLimiterTaskApplication {

	@RateLimiter(name = "test")
	public static void main(String[] args) {
		SpringApplication.run(RateLimiterTaskApplication.class, args);
	}

}
