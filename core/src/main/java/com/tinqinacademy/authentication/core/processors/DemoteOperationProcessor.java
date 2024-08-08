package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.base.BaseOperationProcessor;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.operations.demote.input.DemoteInput;
import com.tinqinacademy.authentication.api.operations.demote.operation.DemoteOperation;
import com.tinqinacademy.authentication.api.operations.demote.output.DemoteOutput;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import org.springframework.core.convert.ConversionService;

import static io.vavr.API.Match;

public class DemoteOperationProcessor extends BaseOperationProcessor implements DemoteOperation {

  public DemoteOperationProcessor(ConversionService conversionService, Validator validator) {
    super(conversionService, validator);
  }

  @Override
  public Either<? extends ErrorOutput, DemoteOutput> process(DemoteInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  return DemoteOutput.builder().build();
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    defaultCase(t)
                ))
        );
  }
}
