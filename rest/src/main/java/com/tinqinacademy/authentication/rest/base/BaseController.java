package com.tinqinacademy.authentication.rest.base;

import com.tinqinacademy.authentication.api.base.OperationOutput;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.operations.login.output.LoginOutput;
import io.vavr.control.Either;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

  protected ResponseEntity<OperationOutput> createResponse(
      Either<? extends ErrorOutput, ? extends OperationOutput> either,
      HttpStatusCode statusCode
  ) {
    return either
        .fold(
            error -> new ResponseEntity<>(error, error.getStatusCode()),
            output -> new ResponseEntity<>(output, statusCode)
        );
  }

  public ResponseEntity<OperationOutput> createResponseWithAuthHeader(Either<? extends ErrorOutput, LoginOutput> either, HttpStatusCode statusCode) {
    return either
        .fold(
            error -> new ResponseEntity<>(error, error.getStatusCode()),
            output -> {
              ResponseEntity<OperationOutput> res = ResponseEntity
                  .status(statusCode)
                  .body(output);

              res.getHeaders()
                  .add("Authorization", "Bearer " + output.getToken());
              return res;
            }
        );
  }
}
