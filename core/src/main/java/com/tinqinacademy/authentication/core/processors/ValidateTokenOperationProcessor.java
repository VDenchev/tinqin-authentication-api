package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.exceptions.JwtException;
import com.tinqinacademy.authentication.api.operations.validatetoken.input.ValidateTokenInput;
import com.tinqinacademy.authentication.api.operations.validatetoken.operation.ValidateTokenOperation;
import com.tinqinacademy.authentication.api.operations.validatetoken.output.ValidateTokenOutput;
import com.tinqinacademy.authentication.core.providers.JwtProvider;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static io.vavr.API.Match;

@Service
@Slf4j
public class ValidateTokenOperationProcessor extends BaseOperationProcessor implements ValidateTokenOperation {

  private final JwtProvider jwtProvider;
  private final UserDetailsService userDetailsService;

  public ValidateTokenOperationProcessor(
      ConversionService conversionService, Validator validator,
      JwtProvider jwtProvider, UserDetailsService userDetailsService
  ) {
    super(conversionService, validator);
    this.jwtProvider = jwtProvider;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public Either<? extends ErrorOutput, ValidateTokenOutput> process(ValidateTokenInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start validate token input: {}", validInput);

                  String username = jwtProvider.getUsernameFromToken(input.getToken());

                  User userDetails = (User) userDetailsService.loadUserByUsername(username);

                  ValidateTokenOutput output = createOutput(userDetails);
                  log.info("End validate token output: {}", output);
                  return output;
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    customStatusCase(t, JwtException.class, HttpStatus.BAD_REQUEST),
                    customStatusCase(t, UsernameNotFoundException.class, HttpStatus.BAD_REQUEST),
                    defaultCase(t)
                ))
        );
  }

  private ValidateTokenOutput createOutput(User userDetails) {
    return ValidateTokenOutput.builder()
        .user(userDetails)
        .build();
  }
}
