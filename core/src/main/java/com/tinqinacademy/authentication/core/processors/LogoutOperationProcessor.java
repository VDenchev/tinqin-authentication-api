package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.models.TokenWrapper;
import com.tinqinacademy.authentication.api.operations.logout.input.LogoutInput;
import com.tinqinacademy.authentication.api.operations.logout.operation.LogoutOperation;
import com.tinqinacademy.authentication.api.operations.logout.output.LogoutOutput;
import com.tinqinacademy.authentication.persistence.entities.InvalidatedJwt;
import com.tinqinacademy.authentication.persistence.mongorepositories.InvalidatedJwtRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import static io.vavr.API.Match;

@Service
@Slf4j
public class LogoutOperationProcessor extends BaseOperationProcessor implements LogoutOperation {

  private final InvalidatedJwtRepository invalidatedJwtRepository;
  private final TokenWrapper tokenWrapper;

  public LogoutOperationProcessor(ConversionService conversionService, Validator validator, InvalidatedJwtRepository invalidatedJwtRepository, TokenWrapper tokenWrapper) {
    super(conversionService, validator);
    this.invalidatedJwtRepository = invalidatedJwtRepository;
    this.tokenWrapper = tokenWrapper;
  }

  @Override
  public Either<? extends ErrorOutput, LogoutOutput> process(LogoutInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start logout input: {}", validInput);

                  InvalidatedJwt invalidatedJwt = createInvalidatedJwt();
                  invalidatedJwtRepository.save(invalidatedJwt);

                  LogoutOutput output = createOutput();
                  log.info("End logout output: {}", output);
                  return output;
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    defaultCase(t)
                ))
        );
  }

  private InvalidatedJwt createInvalidatedJwt() {
    return InvalidatedJwt.builder()
        .token(tokenWrapper.getToken())
        .expiryTime(tokenWrapper.getExpirationTime())
        .build();
  }

  private LogoutOutput createOutput() {
    return LogoutOutput.builder().build();
  }
}
