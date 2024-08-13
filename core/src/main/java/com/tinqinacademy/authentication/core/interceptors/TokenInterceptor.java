package com.tinqinacademy.authentication.core.interceptors;

import com.tinqinacademy.authentication.api.enums.RoleEnum;
import com.tinqinacademy.authentication.api.models.TokenWrapper;
import com.tinqinacademy.authentication.core.providers.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

  private final TokenWrapper tokenWrapper;
  private final JwtProvider jwtProvider;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    final String authHeaderValue = request.getHeader("Authorization");

    if (authHeaderValue == null || !authHeaderValue.startsWith("Bearer")) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }

    String token = authHeaderValue.substring(7, authHeaderValue.length());

    try {
      String username = jwtProvider.getUsernameFromToken(token);
      List<RoleEnum> roles = jwtProvider.getRolesFromToken(token);

      tokenWrapper.setUsername(username);
      tokenWrapper.setRoles(roles);
      log.info("Token wrapper: {}", tokenWrapper);
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }

    return true;
  }
}
