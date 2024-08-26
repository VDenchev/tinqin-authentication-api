package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.exceptions.EntityAlreadyExistsException;
import com.tinqinacademy.authentication.api.exceptions.UnknownRoleException;
import com.tinqinacademy.authentication.api.operations.register.input.RegisterInput;
import com.tinqinacademy.authentication.api.operations.register.operation.RegisterOperation;
import com.tinqinacademy.authentication.api.operations.register.output.RegisterOutput;
import com.tinqinacademy.authentication.core.services.EmailConfirmationService;
import com.tinqinacademy.authentication.persistence.entities.Role;
import com.tinqinacademy.authentication.persistence.entities.User;
import com.tinqinacademy.authentication.persistence.enums.RoleEnum;
import com.tinqinacademy.authentication.persistence.repositories.RoleRepository;
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

import java.util.ArrayList;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.EMAIL_TAKEN_FORMAT;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.PHONE_NO_TAKEN_FORMAT;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.UNKNOWN_ROLE_FORMAT;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.USERNAME_TAKEN_FORMAT;
import static io.vavr.API.Match;

@Service
@Slf4j
public class RegisterOperationProcessor extends BaseOperationProcessor implements RegisterOperation {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailConfirmationService emailConfirmationService;

  public RegisterOperationProcessor(
      ConversionService conversionService, Validator validator, UserRepository userRepository,
      RoleRepository roleRepository, PasswordEncoder passwordEncoder,
      EmailConfirmationService emailConfirmationService
  ) {
    super(conversionService, validator);
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailConfirmationService = emailConfirmationService;
  }

  @Transactional
  @Override
  public Either<? extends ErrorOutput, RegisterOutput> process(RegisterInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start register input: {}", validInput);

                  checkUsernameTaken(validInput);
                  checkEmailTaken(validInput);
                  checkPhoneNoTaken(validInput);

                  String securePasswordHash = passwordEncoder.encode(validInput.getPassword());
                  log.info("Generated secure password hash: {}", securePasswordHash);

                  User userToSave = convertToUser(validInput, securePasswordHash);

                  User savedUser = userRepository.save(userToSave);

                  emailConfirmationService.sendConfirmationEmail(savedUser);

                  RegisterOutput output = createOutput(savedUser);
                  log.info("End register output: {}", output);
                  return output;
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    customStatusCase(t, EntityAlreadyExistsException.class, HttpStatus.CONFLICT),
                    defaultCase(t)
                ))
        );
  }

  private void checkUsernameTaken(RegisterInput input) {
    String username = input.getUsername();

    boolean idDuplicateUsername = userRepository.existsByUsernameIgnoreCase(username);
    if (idDuplicateUsername) {
      throw new EntityAlreadyExistsException(String.format(USERNAME_TAKEN_FORMAT, username));
    }
  }

  private void checkEmailTaken(RegisterInput input) {
    String email = input.getEmail();

    boolean idDuplicateEmail = userRepository.existsByEmailIgnoreCase(email);
    if (idDuplicateEmail) {
      throw new EntityAlreadyExistsException(String.format(EMAIL_TAKEN_FORMAT, email));
    }
  }

  private void checkPhoneNoTaken(RegisterInput input) {
    String phoneNo = input.getPhoneNo();

    boolean idDuplicatePhoneNo = userRepository.existsByPhoneNumber(phoneNo);
    if (idDuplicatePhoneNo) {
      throw new EntityAlreadyExistsException(String.format(PHONE_NO_TAKEN_FORMAT, phoneNo));
    }
  }

  private User convertToUser(RegisterInput input, String securePasswordHash) {
    User user = conversionService.convert(input, User.class);
    user.setPassword(securePasswordHash);
    Role userRole = roleRepository.findByType(RoleEnum.USER)
        .orElseThrow(() -> new UnknownRoleException(String.format(UNKNOWN_ROLE_FORMAT, RoleEnum.USER)));
    user.setRoles(new ArrayList<>());
    user.getRoles().add(userRole);
    user.setIsVerified(Boolean.FALSE);

    promoteIfNoAdmins(user);
    return user;
  }

  private void promoteIfNoAdmins(User user) {
    Long adminsCount = userRepository.countUsersWithAdminRole();
    if (adminsCount == 0) {
      Role adminRole = roleRepository.findByType(RoleEnum.ADMIN)
          .orElseThrow(() -> new UnknownRoleException(String.format(UNKNOWN_ROLE_FORMAT, RoleEnum.ADMIN)));
      user.getRoles().add(adminRole);
    }
  }

  private RegisterOutput createOutput(User savedUser) {
    return RegisterOutput.builder()
        .id(savedUser.getId().toString())
        .build();
  }
}
