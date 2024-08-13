package com.tinqinacademy.authentication.api.exceptions;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.NO_PERMISSIONS_MESSAGE;

public class NoPermissionsException extends RuntimeException{

  public NoPermissionsException() {
    super(NO_PERMISSIONS_MESSAGE);
  }
}
