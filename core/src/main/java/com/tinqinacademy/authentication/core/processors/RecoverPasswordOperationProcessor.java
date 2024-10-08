package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.operations.recoverpassword.input.RecoverPasswordInput;
import com.tinqinacademy.authentication.api.operations.recoverpassword.operation.RecoverPasswordOperation;
import com.tinqinacademy.authentication.api.operations.recoverpassword.output.RecoverPasswordOutput;
import com.tinqinacademy.authentication.api.services.base.MailService;
import com.tinqinacademy.authentication.persistence.entities.RecoveryCode;
import com.tinqinacademy.authentication.persistence.entities.User;
import com.tinqinacademy.authentication.persistence.mongorepositories.RecoveryCodeRepository;
import com.tinqinacademy.authentication.persistence.repositories.UserRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.vavr.API.Match;

@Service
@Slf4j
public class RecoverPasswordOperationProcessor extends BaseOperationProcessor implements RecoverPasswordOperation {

  public static final int RECOVERY_CODE_SIZE = 32;

  private final RecoveryCodeRepository recoveryCodeRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;

  public RecoverPasswordOperationProcessor(
      ConversionService conversionService, Validator validator,
      RecoveryCodeRepository recoveryCodeRepository, UserRepository userRepository,
      PasswordEncoder passwordEncoder, MailService mailService
  ) {
    super(conversionService, validator);
    this.recoveryCodeRepository = recoveryCodeRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.mailService = mailService;
  }

  @Override
  @Transactional
  public Either<? extends ErrorOutput, RecoverPasswordOutput> process(RecoverPasswordInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start recover password input: {}", validInput);
                  Optional<User> userMaybe = userRepository.findByEmailIgnoreCase(validInput.getEmail());

                  if (userMaybe.isEmpty()) {
                    return createOutput();
                  }

                  User user = userMaybe.get();
                  recoveryCodeRepository.deleteAllByUserId(user.getId());

                  String recoveryCode = generateRandomOtp(RECOVERY_CODE_SIZE);
                  Map<String, Object> model = new HashMap<>();
                  model.put("recoveryCode", recoveryCode);

                  mailService.sendEmail("Recover your password", user.getEmail(), model,
                      "password-recovery-template.ftl");

                  String hashedRecoveryCode = passwordEncoder.encode(recoveryCode);
                  RecoveryCode code = convertToRecoveryCode(user, hashedRecoveryCode);
                  recoveryCodeRepository.save(code);

                  return createOutput();
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    defaultCase(t)
                ))
        );
  }

  private RecoveryCode convertToRecoveryCode(User user, String otp) {
    return RecoveryCode.builder()
        .userId(user.getId())
        .createdAt(Instant.now())
        .hashedOtp(otp)
        .build();
  }

  private RecoverPasswordOutput createOutput() {
    return RecoverPasswordOutput.builder().build();
  }

  private static String generateRandomOtp(int length) {
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*=+?";

    SecureRandom random = new SecureRandom();

    return IntStream.range(0, length)
        .map(i -> characters.charAt(random.nextInt(characters.length())))
        .mapToObj(c -> String.valueOf((char) c))
        .collect(Collectors.joining());
  }
}
