package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.base.BaseOperationProcessor;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.operations.promote.input.PromoteInput;
import com.tinqinacademy.authentication.api.operations.promote.operation.PromoteOperation;
import com.tinqinacademy.authentication.api.operations.promote.output.PromoteOutput;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import static io.vavr.API.Match;

@Service
public class PromoteOperationProcessor extends BaseOperationProcessor implements PromoteOperation {

  public PromoteOperationProcessor(ConversionService conversionService, Validator validator) {
    super(conversionService, validator);
  }

  @Override
  public Either<? extends ErrorOutput, PromoteOutput> process(PromoteInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  return PromoteOutput.builder().build();
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    defaultCase(t)
                ))
        );
  }
}
