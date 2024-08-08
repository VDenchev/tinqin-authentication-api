package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.base.BaseOperationProcessor;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.operations.recoverpassword.input.RecoverPasswordInput;
import com.tinqinacademy.authentication.api.operations.recoverpassword.operation.RecoverPasswordOperation;
import com.tinqinacademy.authentication.api.operations.recoverpassword.output.RecoverPasswordOutput;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import org.springframework.core.convert.ConversionService;

import static io.vavr.API.Match;

public class RecoverPasswordOperationProcessor extends BaseOperationProcessor implements RecoverPasswordOperation {

  public RecoverPasswordOperationProcessor(ConversionService conversionService, Validator validator) {
    super(conversionService, validator);
  }

  @Override
  public Either<? extends ErrorOutput, RecoverPasswordOutput> process(RecoverPasswordInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  return RecoverPasswordOutput.builder().build();
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    defaultCase(t)
                ))
        );
  }
}
