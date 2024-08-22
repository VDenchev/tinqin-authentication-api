package com.tinqinacademy.authentication.api.constants;

public class ExceptionMessages {

  public static final String NOT_FOUND_FORMAT = "Entity of type %s with id=%s not found";
  public static final String INVALID_VERIFICATION_CODE_MESSAGE = "Invalid verification code";
  public static final String INVALID_RECOVERY_CODE_MESSAGE = "Invalid recovery code";
  public static final String USERNAME_TAKEN_FORMAT = "User with the username \"%s\" already exists";
  public static final String EMAIL_TAKEN_FORMAT = "User with email \"%s\" already exists";
  public static final String PHONE_NO_TAKEN_FORMAT = "User with phoneNo \"%s\" already exists";
  public static final String UNKNOWN_ROLE_FORMAT = "Role %s does not exist";
  public static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
  public static final String EMAIL_NOT_CONFIRMED_MESSAGE = "Email is not confirmed";
  public static final String RECOVERY_NOT_REQUESTED_MESSAGE = "No recovery request has been made for this email";
  public static final String SELF_PROMOTE_MESSAGE = "Cannot promote yourself";
  public static final String SELF_DEMOTE_MESSAGE = "Cannot demote yourself";
  public static final String LAST_ADMIN_DEMOTE_MESSAGE = "Cannot demote last admin user";
  public static final String JWT_EXPIRED_MESSAGE = "JWT is expired";
  public static final String INVALID_JWT_MESSAGE = "Invalid JWT format";
  public static final String UNSUPPORTED_JWT_MESSAGE = "Unsupported token format";
  public static final String INVALID_SIGNATURE_MESSAGE = "Cannot compute signature";
  public static final String PARSING_JWT_MESSAGE = "Error parsing JWT";
  public static final String EMPTY_JWT_MESSAGE = "JWT cannot be empty or null";
  public static final String NO_PERMISSIONS_MESSAGE = "Only admins can access this resource";
  public static final String TOO_MANY_REQUESTS_MESSAGE = "You have exhausted your API Request Quota";
  public static final String USERNAME_NOT_FOUND_MESSAGE = "Username not found";
}
