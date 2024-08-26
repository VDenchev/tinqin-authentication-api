package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.exceptions.InvalidCredentialsException;
import com.tinqinacademy.authentication.api.operations.changepassword.input.ChangePasswordInput;
import com.tinqinacademy.authentication.api.operations.changepassword.operation.ChangePasswordOperation;
import com.tinqinacademy.authentication.api.operations.changepassword.output.ChangePasswordOutput;
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
import org.springframework.transaction.annotation.Transactional;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.INVALID_CREDENTIALS_MESSAGE;
import static io.vavr.API.Match;

@Service
@Slf4j
public class ChangePasswordOperationProcessor extends BaseOperationProcessor implements ChangePasswordOperation {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public ChangePasswordOperationProcessor(ConversionService conversionService, Validator validator, UserRepository userRepository, PasswordEncoder passwordEncoder) {
    super(conversionService, validator);
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  @Override
  public Either<? extends ErrorOutput, ChangePasswordOutput> process(ChangePasswordInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start change password input: {}", validInput);

                  User user = userRepository.findByUsernameIgnoreCase(validInput.getTokenInput().getUsername())
                      .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));

                  ensureEmailsMatch(validInput, user);

                  ensurePasswordsMatch(input, user);

                  changePassword(validInput, user);

                  ChangePasswordOutput output = createOutput();
                  log.info("End change password output: {}", output);
                  return output;
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    customStatusCase(t, InvalidCredentialsException.class, HttpStatus.BAD_REQUEST),
                    defaultCase(t)
                ))
        );
  }

  private void ensureEmailsMatch(ChangePasswordInput validInput, User user) {
    if (!user.getEmail().equalsIgnoreCase(validInput.getEmail())) {
      throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
    }
  }

  private ChangePasswordOutput createOutput() {
    return ChangePasswordOutput.builder().build();
  }

  private void ensurePasswordsMatch(ChangePasswordInput input, User user) {
    boolean passwordsMatch = passwordEncoder.matches(input.getOldPassword(), user.getPassword());
    if (!passwordsMatch) {
      throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
    }
  }

  private void changePassword(ChangePasswordInput validInput, User user) {
    String newPasswordHash = passwordEncoder.encode(validInput.getNewPassword());
    user.setPassword(newPasswordHash);
    userRepository.save(user);
  }
}
