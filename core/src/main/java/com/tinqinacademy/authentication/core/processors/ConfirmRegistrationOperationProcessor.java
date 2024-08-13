package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.exceptions.EntityNotFoundException;
import com.tinqinacademy.authentication.api.exceptions.InvalidCodeException;
import com.tinqinacademy.authentication.api.operations.confirmregistration.input.ConfirmRegistrationInput;
import com.tinqinacademy.authentication.api.operations.confirmregistration.operation.ConfirmRegistrationOperation;
import com.tinqinacademy.authentication.api.operations.confirmregistration.output.ConfirmRegistrationOutput;
import com.tinqinacademy.authentication.persistence.entities.User;
import com.tinqinacademy.authentication.persistence.entities.VerificationCode;
import com.tinqinacademy.authentication.persistence.mongorepositories.VerificationCodeRepository;
import com.tinqinacademy.authentication.persistence.repositories.UserRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static io.vavr.API.Match;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.INVALID_VERIFICATION_CODE_MESSAGE;

@Service
@Slf4j
public class ConfirmRegistrationOperationProcessor extends BaseOperationProcessor implements ConfirmRegistrationOperation {

  private final VerificationCodeRepository verificationCodeRepository;
  private final UserRepository userRepository;

  public ConfirmRegistrationOperationProcessor(
      ConversionService conversionService, Validator validator,
      VerificationCodeRepository verificationCodeRepository, UserRepository userRepository
  ) {
    super(conversionService, validator);
    this.verificationCodeRepository = verificationCodeRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  @Override
  public Either<? extends ErrorOutput, ConfirmRegistrationOutput> process(ConfirmRegistrationInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start confirm registration input: {}", validInput);

                  VerificationCode verificationCode = verificationCodeRepository.findFirstByCodeOrderByCreatedAtDesc(validInput.getConfirmationCode())
                      .orElseThrow(() -> new InvalidCodeException(INVALID_VERIFICATION_CODE_MESSAGE));

                  UUID userId = verificationCode.getUserId();
                  verificationCodeRepository.delete(verificationCode);
                  verifyUser(userId);

                  ConfirmRegistrationOutput output = createOutput();
                  log.info("End confirm registration output: {}", output);
                  return output;
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    customStatusCase(t, InvalidCodeException.class, HttpStatus.BAD_REQUEST),
                    customStatusCase(t, EntityNotFoundException.class, HttpStatus.INTERNAL_SERVER_ERROR),
                    defaultCase(t)
                ))
        );
  }

  private void verifyUser(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User", userId));
    user.setIsVerified(Boolean.TRUE);
    userRepository.save(user);
  }

  private ConfirmRegistrationOutput createOutput() {
    return ConfirmRegistrationOutput.builder().build();
  }
}
