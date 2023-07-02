package org.zhu4.task.controller;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zhu4.task.ratelimiter.exception.RateLimitExceededException;

@ControllerAdvice
public class RateLimitExceededExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException() {
        return ResponseEntity.status(HttpStatusCode.valueOf(502)) // TODO: 429
                .body(new ErrorResponse("Bad Gateway", "502", "You have exceeded your quota"));
    }
}
