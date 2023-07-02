package org.zhu4.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@ConfigurationPropertiesScan("org.zhu4.task.ratelimiter.configuration")
public class RateLimiterTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(RateLimiterTaskApplication.class, args);
	}

}
