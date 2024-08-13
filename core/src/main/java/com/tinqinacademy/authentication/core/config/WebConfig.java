package com.tinqinacademy.authentication.core.config;

import com.tinqinacademy.authentication.api.models.TokenWrapper;
import com.tinqinacademy.authentication.core.interceptors.TokenInterceptor;
import com.tinqinacademy.authentication.core.providers.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.CHANGE_PASSWORD;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.DEMOTE;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.LOGOUT;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.PROMOTE;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final JwtProvider jwtProvider;
  private final TokenInterceptor tokenInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(tokenInterceptor)
        .addPathPatterns(securedEndpoints());
  }

  private String[] securedEndpoints() {
    return new String[]{DEMOTE, PROMOTE, CHANGE_PASSWORD, LOGOUT};
  }

  @Bean
  @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public TokenWrapper tokenWrapper() {
    return TokenWrapper.builder().build();
  }
}
