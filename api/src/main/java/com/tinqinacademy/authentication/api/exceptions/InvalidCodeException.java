package com.tinqinacademy.authentication.api.exceptions;

public class InvalidCodeException extends RuntimeException {

  public InvalidCodeException(String code) {
    super(String.format("Invalid verification code - %s", code));
  }
}
