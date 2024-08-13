package com.tinqinacademy.authentication.api.exceptions;

import lombok.Getter;

@Getter
public class JwtException extends RuntimeException {

  public JwtException(String message) {
    super(message);
  }
}
