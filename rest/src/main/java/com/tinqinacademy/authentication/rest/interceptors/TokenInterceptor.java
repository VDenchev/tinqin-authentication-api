package com.tinqinacademy.authentication.rest.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinqinacademy.authentication.api.enums.RoleEnum;
import com.tinqinacademy.authentication.api.exceptions.JwtException;
import com.tinqinacademy.authentication.core.providers.JwtProvider;
import com.tinqinacademy.authentication.rest.context.TokenContext;
import com.tinqinacademy.authentication.rest.interceptors.base.BaseInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.List;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.EMPTY_JWT_MESSAGE;

@Component
@Slf4j
public class TokenInterceptor extends BaseInterceptor implements HandlerInterceptor {

  public static final String AUTH_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  private final TokenContext tokenContext;
  private final JwtProvider jwtProvider;

  public TokenInterceptor(
      ObjectMapper objectMapper, TokenContext tokenContext,
      JwtProvider jwtProvider
  ) {
    super(objectMapper);
    this.tokenContext = tokenContext;
    this.jwtProvider = jwtProvider;
  }

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

  private String extractToken(String authHeaderValue) {
    return authHeaderValue.substring(BEARER_PREFIX.length());
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

}
