package com.tinqinacademy.authentication.api.constants;

public class ExceptionMessages {

  public static final String NOT_FOUND_FORMAT = "Entity of type %s with id=%s not found";
  public static final String INVALID_CODE_FORMAT = "Invalid verification code - %s";
  public static final String USERNAME_TAKEN_FORMAT = "User with the username \"%s\" already exists";
  public static final String EMAIL_TAKEN_FORMAT = "User with email \"%s\" already exists";
  public static final String PHONE_NO_TAKEN_FORMAT = "User with phoneNo \"%s\" already exists";
  public static final String UNKNOWN_ROLE_FORMAT = "Role %s does not exist";
  public static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
  public static final String EMAIL_NOT_CONFIRMED = "Email is not confirmed";
  public static final String RECOVERY_NOT_REQUESTED_MESSAGE = "No recovery request has been made for this email";
}
