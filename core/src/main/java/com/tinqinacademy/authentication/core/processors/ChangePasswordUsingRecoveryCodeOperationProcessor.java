package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.exceptions.InvalidCodeException;
import com.tinqinacademy.authentication.api.exceptions.RecoveryNotRequestedException;
import com.tinqinacademy.authentication.api.operations.changepasswordusingrecoverycode.input.ChangePasswordUsingRecoveryCodeInput;
import com.tinqinacademy.authentication.api.operations.changepasswordusingrecoverycode.operation.ChangePasswordUsingRecoveryCodeOperation;
import com.tinqinacademy.authentication.api.operations.changepasswordusingrecoverycode.output.ChangePasswordUsingRecoveryCodeOutput;
import com.tinqinacademy.authentication.persistence.entities.RecoveryCode;
import com.tinqinacademy.authentication.persistence.entities.User;
import com.tinqinacademy.authentication.persistence.mongorepositories.RecoveryCodeRepository;
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

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.INVALID_RECOVERY_CODE_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.RECOVERY_NOT_REQUESTED_MESSAGE;
import static io.vavr.API.Match;

@Service
@Slf4j
public class ChangePasswordUsingRecoveryCodeOperationProcessor extends BaseOperationProcessor
    implements ChangePasswordUsingRecoveryCodeOperation {

  private final UserRepository userRepository;
  private final RecoveryCodeRepository recoveryCodeRepository;
  private final PasswordEncoder passwordEncoder;

  public ChangePasswordUsingRecoveryCodeOperationProcessor(
      ConversionService conversionService, Validator validator,
      UserRepository userRepository, RecoveryCodeRepository recoveryCodeRepository, PasswordEncoder passwordEncoder
  ) {
    super(conversionService, validator);
    this.userRepository = userRepository;
    this.recoveryCodeRepository = recoveryCodeRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public Either<? extends ErrorOutput, ChangePasswordUsingRecoveryCodeOutput> process(ChangePasswordUsingRecoveryCodeInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start change password using recovery code input: {}", validInput);
                  User user = userRepository.findByEmailIgnoreCase(validInput.getEmail())
                      .orElseThrow(() -> new RecoveryNotRequestedException(RECOVERY_NOT_REQUESTED_MESSAGE));

                  RecoveryCode recoveryCode = recoveryCodeRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId())
                      .orElseThrow(() -> new RecoveryNotRequestedException(RECOVERY_NOT_REQUESTED_MESSAGE));

                  recoveryCodesMatch(validInput, recoveryCode);

                  recoveryCodeRepository.deleteAllByUserId(user.getId());
                  changePassword(validInput, user);

                  return ChangePasswordUsingRecoveryCodeOutput.builder().build();
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    customStatusCase(t, InvalidCodeException.class, HttpStatus.BAD_REQUEST),
                    customStatusCase(t, RecoveryNotRequestedException.class, HttpStatus.BAD_REQUEST),
                    defaultCase(t)
                ))
        );
  }

  private void recoveryCodesMatch(ChangePasswordUsingRecoveryCodeInput validInput, RecoveryCode recoveryCode) {
    boolean recoveryCodesMatch = passwordEncoder.matches(validInput.getRecoveryCode(), recoveryCode.getHashedOtp());
    if (!recoveryCodesMatch) {
      throw new InvalidCodeException(INVALID_RECOVERY_CODE_MESSAGE);
    }
  }

  private void changePassword(ChangePasswordUsingRecoveryCodeInput validInput, User user) {
    String newPasswordHash = passwordEncoder.encode(validInput.getNewPassword());
    user.setPassword(newPasswordHash);
    userRepository.save(user);
  }
}
