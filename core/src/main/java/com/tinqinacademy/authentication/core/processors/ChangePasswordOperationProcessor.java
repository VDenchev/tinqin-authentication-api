package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.base.BaseOperationProcessor;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.operations.changepassword.input.ChangePasswordInput;
import com.tinqinacademy.authentication.api.operations.changepassword.operation.ChangePasswordOperation;
import com.tinqinacademy.authentication.api.operations.changepassword.output.ChangePasswordOutput;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import org.springframework.core.convert.ConversionService;

import static io.vavr.API.Match;

public class ChangePasswordOperationProcessor extends BaseOperationProcessor implements ChangePasswordOperation {

  public ChangePasswordOperationProcessor(ConversionService conversionService, Validator validator) {
    super(conversionService, validator);
  }

  @Override
  public Either<? extends ErrorOutput, ChangePasswordOutput> process(ChangePasswordInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  return ChangePasswordOutput.builder().build();
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    defaultCase(t)
                ))
        );
  }
}
