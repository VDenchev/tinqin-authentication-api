package com.tinqinacademy.authentication.api.exceptions;

import java.util.UUID;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.NOT_FOUND_FORMAT;

public class EntityNotFoundException extends RuntimeException {

  private String entityName;
  private UUID id;

  public EntityNotFoundException(String entityName, UUID id) {
    super(String.format(NOT_FOUND_FORMAT, entityName, id));
    this.entityName = entityName;
    this.id = id;
  }
}
