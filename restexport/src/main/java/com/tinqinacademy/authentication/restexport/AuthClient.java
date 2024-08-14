package com.tinqinacademy.authentication.restexport;

import com.tinqinacademy.authentication.api.operations.validatetoken.output.ValidateTokenOutput;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import static com.tinqinacademy.authentication.api.apiroutes.FeignClientApiRoutes.VALIDATE_TOKEN;

@Headers({"Content-Type: application/json"})
public interface AuthClient {

  @Headers("Authorization: {authHeader}")
  @RequestLine(VALIDATE_TOKEN)
  ValidateTokenOutput validateToken(@Param("auth_header") String authHeader);
}
