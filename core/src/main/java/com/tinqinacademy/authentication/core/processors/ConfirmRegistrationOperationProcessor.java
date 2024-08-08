package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.base.BaseOperationProcessor;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.operations.confirmregistration.input.ConfirmRegistrationInput;
import com.tinqinacademy.authentication.api.operations.confirmregistration.operation.ConfirmRegistrationOperation;
import com.tinqinacademy.authentication.api.operations.confirmregistration.output.ConfirmRegistrationOutput;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import static io.vavr.API.Match;

@Service
public class ConfirmRegistrationOperationProcessor extends BaseOperationProcessor implements ConfirmRegistrationOperation {

  public ConfirmRegistrationOperationProcessor(ConversionService conversionService, Validator validator) {
    super(conversionService, validator);
  }

  @Override
  public Either<? extends ErrorOutput, ConfirmRegistrationOutput> process(ConfirmRegistrationInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  return ConfirmRegistrationOutput.builder().build();
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    defaultCase(t)
                ))
        );
  }
}
