package org.zhu4.task.ratelimiter;

public interface RateLimiter {

    boolean rateLimit(final String userKey);
}
