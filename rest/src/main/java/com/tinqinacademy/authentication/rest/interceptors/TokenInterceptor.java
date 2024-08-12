package com.tinqinacademy.authentication.rest.interceptors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.tinqinacademy.authentication.api.enums.RoleEnum;
import com.tinqinacademy.authentication.api.models.TokenWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Base64;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

  private final TokenWrapper tokenWrapper;
  private final ObjectMapper objectMapper;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    final String authHeaderValue = request.getHeader("Authorization");

    if (authHeaderValue != null && authHeaderValue.startsWith("Bearer")) {
      String token = authHeaderValue.substring(7, authHeaderValue.length());

      String[] parts = token.split("\\.");

      if (parts.length != 3) {
        return true;
      }

      byte[] bytes = Base64.getUrlDecoder().decode(parts[1]);
      JsonNode jsonNode = objectMapper.readTree(bytes);

      tokenWrapper.setToken(token);
      tokenWrapper.setUsername(jsonNode.get("username").textValue());
      JsonNode roles = jsonNode.get("roles");
      ObjectReader reader = objectMapper.readerFor(new TypeReference<List<RoleEnum>>() {
      });

      tokenWrapper.setRoles(reader.readValue(roles));
      log.info("Token wrapper: {}",tokenWrapper);

    }

    return true;
  }
}
