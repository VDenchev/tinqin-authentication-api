package com.tinqinacademy.authentication.api.exceptions;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.INVALID_CODE_FORMAT;

public class InvalidCodeException extends RuntimeException {

  public InvalidCodeException(String code) {
    super(String.format(INVALID_CODE_FORMAT, code));
  }
}
