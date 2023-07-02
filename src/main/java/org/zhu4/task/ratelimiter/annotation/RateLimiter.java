package org.zhu4.task.ratelimiter.annotation;


import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface RateLimiter {

    /**
     * Unique Name of the rate limiter.
     *
     * @return the name of the limiter
     */
    String name();
}
