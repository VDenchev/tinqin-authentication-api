package com.tinqinacademy.authentication.api.exceptions;

public class SelfModificationNotAllowedException extends RuntimeException {

  public SelfModificationNotAllowedException(String message) {
    super(message);
  }
}
