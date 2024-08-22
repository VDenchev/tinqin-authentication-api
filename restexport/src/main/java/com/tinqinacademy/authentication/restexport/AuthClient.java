package com.tinqinacademy.authentication.restexport;

import com.tinqinacademy.authentication.api.operations.getuserbyphoneno.output.SearchUsersOutput;
import com.tinqinacademy.authentication.api.operations.validatetoken.output.ValidateTokenOutput;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import static com.tinqinacademy.authentication.api.apiroutes.FeignClientApiRoutes.SEARCH_USERS;
import static com.tinqinacademy.authentication.api.apiroutes.FeignClientApiRoutes.VALIDATE_TOKEN;

@Headers({"Content-Type: application/json"})
public interface AuthClient {

  @Headers("Authorization:Bearer {authHeader}")
  @RequestLine(VALIDATE_TOKEN)
  ValidateTokenOutput validateToken(@Param("authHeader") String authHeader);

  @RequestLine(SEARCH_USERS)
  SearchUsersOutput searchUsers(@Param("phoneNo") String phoneNo);
}
