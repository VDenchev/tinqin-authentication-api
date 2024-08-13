package com.tinqinacademy.authentication.api.apiroutes;

public class RestApiRoutes {

  public static final String ROOT = "/api/v1";
  public static final String AUTH = ROOT + "/auth";

  public static final String LOGIN = AUTH + "/login";
  public static final String REGISTER = AUTH + "/register";
  public static final String RECOVER_PASSWORD = AUTH + "/recover-password";
  public static final String CHANGE_PASSWORD_USING_RECOVERY = AUTH + "/recover-password/change";
  public static final String CONFIRM_REGISTRATION = AUTH + "/confirm-registration";
  public static final String CHANGE_PASSWORD = AUTH + "/change-password";
  public static final String PROMOTE = AUTH + "/promote";
  public static final String DEMOTE = AUTH + "/demote";
  public static final String VALIDATE_TOKEN = AUTH + "/validate-token";
}
