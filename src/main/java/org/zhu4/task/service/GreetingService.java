package org.zhu4.task.service;

import org.springframework.stereotype.Service;
import org.zhu4.task.ratelimiter.annotation.RateLimiter;

@Service
public class GreetingService {

    @RateLimiter(name = "greet")
    public String greet() {
        return "Hello, World!";
    }

    @RateLimiter(name = "hello")
    public String hello() {
        return "Hello, World!";
    }
}
