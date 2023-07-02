package org.zhu4.task.ratelimiter.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zhu4.task.ratelimiter.annotation.RateLimiter;
import org.zhu4.task.ratelimiter.exception.RateLimitExceededException;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class RateLimiterAspect {

    public static String RATE_LIMIT_EXCEEDED_ON_METHOD = "Rate limit exceeded on method: ";

    private final RateLimiterRegistry registry;
    private final MeterRegistry meterRegistry;

    public RateLimiterAspect(RateLimiterRegistry registry, MeterRegistry meterRegistry) {
        this.registry = registry;
        this.meterRegistry = meterRegistry;
    }


    @Pointcut(value = "@annotation(rateLimiter)", argNames = "rateLimiter")
    public void matchAnnotatedMethod(final RateLimiter rateLimiter) {
        // Method used as pointcut
    }

    @Around(value = "matchAnnotatedMethod(rateLimiterAnnotation)", argNames = "proceedingJoinPoint, rateLimiterAnnotation")
    public Object rateLimiterAroundAdvice(
            final ProceedingJoinPoint proceedingJoinPoint, final RateLimiter rateLimiterAnnotation
    ) throws Throwable {
        final var rateLimiter = registry.getRateLimiter(rateLimiterAnnotation.name());
        final var userKey = getUserKey();
        if (userKey == null) {
            // not found user key
            return proceedingJoinPoint.proceed();
        }
        if (!rateLimiter.rateLimit(userKey)) {
            final var onMethod = getMethodName(proceedingJoinPoint);
            meterRegistry.counter("rateLimitExceededCounterAll").increment();
            meterRegistry.counter("rateLimitExceededCounter" + onMethod).increment();
            log.info(RATE_LIMIT_EXCEEDED_ON_METHOD + onMethod);
            throw new RateLimitExceededException(RATE_LIMIT_EXCEEDED_ON_METHOD + onMethod);
        }
        return proceedingJoinPoint.proceed();
    }

    public String getMethodName(final ProceedingJoinPoint proceedingJoinPoint) {
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }

    public String getUserKey() {
        HttpServletRequest request = null;
        try {
            request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (IllegalStateException e) {
            // pass, not in request context
        }
        return request != null ? request.getRemoteAddr() : null;
    }
}
