package com.tinqinacademy.authentication.rest.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinqinacademy.authentication.rest.interceptors.base.BaseInterceptor;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.TOO_MANY_REQUESTS_MESSAGE;

@Component
public class RateLimitInterceptor extends BaseInterceptor implements HandlerInterceptor {

  public static final int ONE_SECOND_IN_NANOS = 1_000_000_000;

  private final Bucket bucket;

  public RateLimitInterceptor(ObjectMapper objectMapper, Bucket bucket) {
    super(objectMapper);
    this.bucket = bucket;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
      return true;
    }

    long waitForRefill = probe.getNanosToWaitForRefill() / ONE_SECOND_IN_NANOS;
    response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
    buildErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS.value(), TOO_MANY_REQUESTS_MESSAGE);
    return false;
  }
}
