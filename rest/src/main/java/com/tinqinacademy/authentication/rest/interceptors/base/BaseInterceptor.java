package com.tinqinacademy.authentication.rest.interceptors.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinqinacademy.authentication.api.errors.Error;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public abstract class BaseInterceptor {

  private final ObjectMapper objectMapper;

  protected void buildErrorResponse(HttpServletResponse response, int status, String message) {
    ErrorOutput errorOutput = convertToErrorOutput(status, message);

    response.resetBuffer();
    response.setStatus(status);
    response.setHeader("Content-Type", "application/json");
    try {
      response.getOutputStream().print(objectMapper.writeValueAsString(errorOutput));
      response.flushBuffer();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ErrorOutput convertToErrorOutput(int status, String message) {
    HttpStatusCode statusCode = HttpStatusCode.valueOf(status);
    Error error = Error.builder()
        .message(message)
        .build();

    return ErrorOutput.builder()
        .errors(List.of(error))
        .statusCode(statusCode)
        .build();
  }
}
