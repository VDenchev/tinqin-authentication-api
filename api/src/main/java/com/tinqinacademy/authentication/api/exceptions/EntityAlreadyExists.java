package com.tinqinacademy.authentication.api.exceptions;

public class EntityAlreadyExists extends RuntimeException {
  public EntityAlreadyExists(String message) {
    super(message);
  }
}
