package com.tinqinacademy.authentication.core.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinqinacademy.authentication.api.enums.RoleEnum;
import com.tinqinacademy.authentication.api.errors.Error;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.models.TokenWrapper;
import com.tinqinacademy.authentication.core.providers.JwtProvider;
import com.tinqinacademy.authentication.persistence.mongorepositories.InvalidatedJwtRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.EMPTY_JWT_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.JWT_EXPIRED_MESSAGE;

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

  public static final String AUTH_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  private final TokenWrapper tokenWrapper;
  private final JwtProvider jwtProvider;
  private final InvalidatedJwtRepository invalidatedJwtRepository;
  private final ObjectMapper objectMapper;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    final String authHeaderValue = request.getHeader(AUTH_HEADER);

    if (authHeaderValue == null || !authHeaderValue.startsWith(BEARER_PREFIX)) {
      buildErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, EMPTY_JWT_MESSAGE);
      return false;
    }

    String token = extractToken(authHeaderValue);
    boolean isTokenInvalidated = invalidatedJwtRepository.existsByToken(token);
    if (isTokenInvalidated) {
      buildErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, JWT_EXPIRED_MESSAGE);
      return false;
    }

    try {
      populateTokenWrapperBean(token);
    } catch (Exception e) {
      buildErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
      return false;
    }

    return true;
  }

  private void buildErrorResponse(HttpServletResponse response, int status, String message) {
    ErrorOutput errorOutput = convertToErrorOutput(status, message);

    response.resetBuffer();
    response.setStatus(status);
    response.setHeader("Content-Type", "application/json");
    try {
      response.getOutputStream().print(objectMapper.writeValueAsString(errorOutput));
      response.flushBuffer();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ErrorOutput convertToErrorOutput(int status, String message) {
    HttpStatusCode statusCode = HttpStatusCode.valueOf(status);
    Error error = Error.builder()
        .message(message)
        .build();
    return ErrorOutput.builder()
        .errors(List.of(error))
        .statusCode(statusCode)
        .build();

  }

  private void populateTokenWrapperBean(String token) {
    String username = jwtProvider.getUsernameFromToken(token);
    List<RoleEnum> roles = jwtProvider.getRolesFromToken(token);
    Instant expirationTime = jwtProvider.getExpirationTimeFromToken(token);

    tokenWrapper.setToken(token);
    tokenWrapper.setUsername(username);
    tokenWrapper.setRoles(roles);
    tokenWrapper.setExpirationTime(expirationTime);
    log.info("Populated token wrapper: {}", tokenWrapper);
  }

  private static String extractToken(String authHeaderValue) {
    return authHeaderValue.substring(BEARER_PREFIX.length());
  }
}
