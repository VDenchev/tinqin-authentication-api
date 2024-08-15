package com.tinqinacademy.authentication.rest.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinqinacademy.authentication.api.enums.RoleEnum;
import com.tinqinacademy.authentication.api.errors.Error;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.exceptions.JwtException;
import com.tinqinacademy.authentication.core.providers.JwtProvider;
import com.tinqinacademy.authentication.rest.context.TokenContext;
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

@Component
@Slf4j
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

  public static final String AUTH_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  private final TokenContext tokenContext;
  private final JwtProvider jwtProvider;
  private final ObjectMapper objectMapper;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    final String authHeaderValue = request.getHeader(AUTH_HEADER);

    if (authHeaderValue == null || !authHeaderValue.startsWith(BEARER_PREFIX)) {
      buildErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, EMPTY_JWT_MESSAGE);
      return false;
    }

    try {
      String token = extractToken(authHeaderValue);
      jwtProvider.validate(token);
      populateTokenContext(token);
    } catch (JwtException e) {
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

  private void populateTokenContext(String token) {
    String username = jwtProvider.getUsernameFromToken(token);
    List<RoleEnum> roles = jwtProvider.getRolesFromToken(token);
    Instant expirationTime = jwtProvider.getExpirationTimeFromToken(token);

    List<String> rolesAsStrings = roles.stream()
        .map(Enum::name)
        .toList();

    tokenContext.setToken(token);
    tokenContext.setUsername(username);
    tokenContext.setRoles(rolesAsStrings);
    tokenContext.setExpirationTime(expirationTime);
    log.info("Populated token wrapper: {}", tokenContext);
  }

  private String extractToken(String authHeaderValue) {
    return authHeaderValue.substring(BEARER_PREFIX.length());
  }
}
