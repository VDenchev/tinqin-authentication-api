package com.tinqinacademy.authentication.api.exceptions;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

  private String entityName;
  private UUID id;

  public EntityNotFoundException(String entityName, UUID id) {
    super(String.format("Entity of type %s with id=%s not found", entityName, id));
    this.entityName = entityName;
    this.id = id;
  }
}
