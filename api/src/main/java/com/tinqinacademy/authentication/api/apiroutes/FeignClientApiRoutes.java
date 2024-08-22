package com.tinqinacademy.authentication.api.apiroutes;

public class FeignClientApiRoutes {

  public static final String VALIDATE_TOKEN = "POST " + RestApiRoutes.VALIDATE_TOKEN;
  public static final String SEARCH_USERS = "GET " + RestApiRoutes.SEARCH_USERS
      + "?phoneNo={phoneNo}";
}
