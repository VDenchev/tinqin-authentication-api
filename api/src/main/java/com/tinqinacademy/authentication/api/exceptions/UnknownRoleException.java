package com.tinqinacademy.authentication.api.exceptions;

public class UnknownRoleException extends RuntimeException {
  public UnknownRoleException(String message) {
    super(message);
  }
}
