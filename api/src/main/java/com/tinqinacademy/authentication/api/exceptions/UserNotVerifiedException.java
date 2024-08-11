package com.tinqinacademy.authentication.api.exceptions;

public class UserNotVerifiedException extends RuntimeException {

  public UserNotVerifiedException(String message) {
    super(message);
  }
}
