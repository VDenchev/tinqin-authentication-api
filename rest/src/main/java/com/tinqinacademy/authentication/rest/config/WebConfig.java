package com.tinqinacademy.authentication.rest.config;

import com.tinqinacademy.authentication.rest.interceptors.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.CHANGE_PASSWORD;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.DEMOTE;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.LOGOUT;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.PROMOTE;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final TokenInterceptor tokenInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(tokenInterceptor)
        .addPathPatterns(securedEndpoints());
  }

  private String[] securedEndpoints() {
    return new String[]{DEMOTE, PROMOTE, CHANGE_PASSWORD, LOGOUT};
  }
}
