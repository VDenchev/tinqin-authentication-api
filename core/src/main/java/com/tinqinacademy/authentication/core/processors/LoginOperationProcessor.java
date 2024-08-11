package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.base.BaseOperationProcessor;
import com.tinqinacademy.authentication.api.enums.RoleEnum;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.exceptions.InvalidCredentialsException;
import com.tinqinacademy.authentication.api.exceptions.UserNotVerifiedException;
import com.tinqinacademy.authentication.api.operations.login.input.LoginInput;
import com.tinqinacademy.authentication.api.operations.login.operation.LoginOperation;
import com.tinqinacademy.authentication.api.operations.login.output.LoginOutput;
import com.tinqinacademy.authentication.core.providers.JwtProvider;
import com.tinqinacademy.authentication.persistence.entities.User;
import com.tinqinacademy.authentication.persistence.repositories.UserRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.EMAIL_NOT_CONFIRMED_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.INVALID_CREDENTIALS_MESSAGE;
import static io.vavr.API.Match;

@Service
@Slf4j
public class LoginOperationProcessor extends BaseOperationProcessor implements LoginOperation {

  private final UserRepository userRepository;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;

  public LoginOperationProcessor(
      ConversionService conversionService, Validator validator,
      UserRepository userRepository, JwtProvider jwtProvider,
      PasswordEncoder passwordEncoder
  ) {
    super(conversionService, validator);
    this.userRepository = userRepository;
    this.jwtProvider = jwtProvider;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Either<? extends ErrorOutput, LoginOutput> process(LoginInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start login input: {}", validInput);

                  User user = userRepository.findByUsernameIgnoreCase(validInput.getUsername())
                      .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));

                  if (!user.getIsVerified()) {
                    throw new UserNotVerifiedException(EMAIL_NOT_CONFIRMED_MESSAGE);
                  }

                  checkPasswordsMatch(validInput, user);

                  String token = generateJwt(user);

                  LoginOutput output = createOutput(token);
                  log.info("End login output: {}", output);
                  return output;
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    customStatusCase(t, InvalidCredentialsException.class, HttpStatus.BAD_REQUEST),
                    customStatusCase(t, UserNotVerifiedException.class, HttpStatus.BAD_REQUEST),
                    defaultCase(t)
                ))
        );
  }

  private void checkPasswordsMatch(LoginInput input, User user) {
    boolean passwordMatches = passwordEncoder.matches(input.getPassword(), user.getPassword());
    if (!passwordMatches) {
      throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
    }
  }

  private String generateJwt(User user) {
    List<RoleEnum> roles = user.getRoles().stream()
        .map(r -> RoleEnum.valueOf(r.getType().name()))
        .toList();

    return jwtProvider.createToken(user.getUsername(), roles);
  }

  private static LoginOutput createOutput(String token) {
    return LoginOutput.builder()
        .token(token)
        .build();
  }
}
