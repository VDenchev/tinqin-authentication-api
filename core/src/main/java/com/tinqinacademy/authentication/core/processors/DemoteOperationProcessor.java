package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.exceptions.EntityNotFoundException;
import com.tinqinacademy.authentication.api.exceptions.LastAdminDemotionException;
import com.tinqinacademy.authentication.api.exceptions.NoPermissionsException;
import com.tinqinacademy.authentication.api.exceptions.SelfModificationNotAllowedException;
import com.tinqinacademy.authentication.api.exceptions.UnknownRoleException;
import com.tinqinacademy.authentication.api.models.TokenInput;
import com.tinqinacademy.authentication.api.operations.demote.input.DemoteInput;
import com.tinqinacademy.authentication.api.operations.demote.operation.DemoteOperation;
import com.tinqinacademy.authentication.api.operations.demote.output.DemoteOutput;
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
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.LAST_ADMIN_DEMOTE_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.SELF_DEMOTE_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.UNKNOWN_ROLE_FORMAT;
import static io.vavr.API.Match;

@Service
@Slf4j
public class DemoteOperationProcessor extends BaseOperationProcessor implements DemoteOperation {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  public DemoteOperationProcessor(
      ConversionService conversionService, Validator validator,
      UserRepository userRepository, RoleRepository roleRepository
  ) {
    super(conversionService, validator);
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  public Either<? extends ErrorOutput, DemoteOutput> process(DemoteInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start demote input: {}", validInput);

                  checkPrincipalPermissions(validInput.getTokenInput());

                  UUID userId = UUID.fromString(input.getUserId());
                  User user = userRepository.findById(userId)
                      .orElseThrow(() -> new EntityNotFoundException("User", userId));

                  checkForSelfDemotion(user, validInput.getTokenInput());

                  checkForLastAdminLeft(user);

                  demoteUser(user);

                  DemoteOutput output = createOutput();
                  log.info("End demote output: {}", output);
                  return output;
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    customStatusCase(t, EntityNotFoundException.class, HttpStatus.BAD_REQUEST),
                    customStatusCase(t, LastAdminDemotionException.class, HttpStatus.CONFLICT),
                    customStatusCase(t, SelfModificationNotAllowedException.class, HttpStatus.CONFLICT),
                    customStatusCase(t, IllegalArgumentException.class, HttpStatus.UNPROCESSABLE_ENTITY),
                    customStatusCase(t, NoPermissionsException.class, HttpStatus.FORBIDDEN),
                    defaultCase(t)
                ))
        );
  }

  private void checkPrincipalPermissions(TokenInput tokenInput) {
    boolean hasAdminRole = tokenInput.getRoles().stream()
        .anyMatch(r -> r.equals(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN));
    if (!hasAdminRole) {
      throw new NoPermissionsException();
    }
  }

  private void checkForLastAdminLeft(User user) {
    boolean userIsAdmin = user.getRoles().stream()
        .anyMatch(r -> r.getType().equals(RoleEnum.ADMIN));
    if (!userIsAdmin) {
      return;
    }

    long adminsCount = userRepository.countUsersWithAdminRole();
    if (adminsCount <= 1) {
      throw new LastAdminDemotionException(LAST_ADMIN_DEMOTE_MESSAGE);
    }
  }

  private void checkForSelfDemotion(User user, TokenInput tokenInput) {
    if (user.getUsername().equals(tokenInput.getUsername())) {
      throw new SelfModificationNotAllowedException(SELF_DEMOTE_MESSAGE);
    }
  }

  private void demoteUser(User user) {
    RoleEnum adminEnum = RoleEnum.ADMIN;
    Role adminRole = roleRepository.findByType(adminEnum)
        .orElseThrow(() -> new UnknownRoleException(String.format(UNKNOWN_ROLE_FORMAT, adminEnum)));

    user.getRoles().removeIf(role -> role.getId().equals(adminRole.getId()));

    userRepository.save(user);
  }

  private DemoteOutput createOutput() {
    return DemoteOutput.builder().build();
  }
}
