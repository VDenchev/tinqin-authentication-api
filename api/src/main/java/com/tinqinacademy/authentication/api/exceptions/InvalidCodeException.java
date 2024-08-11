package com.tinqinacademy.authentication.api.exceptions;

public class InvalidCodeException extends RuntimeException {

  public InvalidCodeException(String message) {
    super(message);
  }
}
