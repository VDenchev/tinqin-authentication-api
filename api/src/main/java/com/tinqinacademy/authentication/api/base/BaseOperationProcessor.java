package com.tinqinacademy.authentication.api.base;

import com.tinqinacademy.authentication.api.errors.Error;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import io.vavr.API;
import io.vavr.control.Either;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.List;
import java.util.Set;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@RequiredArgsConstructor
public abstract class BaseOperationProcessor {

  protected final ConversionService conversionService;
  protected final Validator validator;

  protected API.Match.Case<? extends Throwable, ErrorOutput> customStatusCase(Throwable t, Class<? extends Throwable> clazz, HttpStatusCode statusCode) {
    ErrorOutput output = ErrorOutput.builder()
        .statusCode(statusCode)
        .errors(List.of(Error.builder()
            .message(t.getMessage())
            .build()
        ))
        .build();
    return createCase(clazz, output);
  }

  protected API.Match.Case<? extends Throwable, ErrorOutput> defaultCase(Throwable t) {
    ErrorOutput output = ErrorOutput.builder()
        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR)
        .errors(List.of(Error.builder()
            .message(t.getMessage())
            .build()
        ))
        .build();
    return createDefaultCase(output);
  }
  protected API.Match.Case<? extends Throwable, ErrorOutput> createCase(Class<? extends Throwable> clazz, ErrorOutput errorOutput) {
    return Case($(instanceOf(clazz)), errorOutput);
  }

  protected API.Match.Case<? extends Throwable, ErrorOutput> createDefaultCase(ErrorOutput errorOutput) {
    return Case($(), errorOutput);
  }

  protected <T extends OperationInput> Either<ErrorOutput, T> validateInput(T input) {
    Set<ConstraintViolation<T>> violations = validator.validate(input);
    if (violations.isEmpty()) {
      return Either.right(input);
    }

    List<Error> errors = violations.stream()
        .map(v -> Error.builder()
            .message(v.getMessage())
            .field(v.getPropertyPath().toString())
            .build()
        ).toList();

    return Either.left(
        ErrorOutput.builder()
            .errors(errors)
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY)
            .build());
  }
}
