package com.tinqinacademy.authentication.core.processors;

import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.exceptions.EntityNotFoundException;
import com.tinqinacademy.authentication.api.exceptions.NoPermissionsException;
import com.tinqinacademy.authentication.api.exceptions.SelfModificationNotAllowedException;
import com.tinqinacademy.authentication.api.exceptions.UnknownRoleException;
import com.tinqinacademy.authentication.api.models.TokenWrapper;
import com.tinqinacademy.authentication.api.operations.promote.input.PromoteInput;
import com.tinqinacademy.authentication.api.operations.promote.operation.PromoteOperation;
import com.tinqinacademy.authentication.api.operations.promote.output.PromoteOutput;
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

import java.util.List;
import java.util.UUID;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.SELF_PROMOTE_MESSAGE;
import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.UNKNOWN_ROLE_FORMAT;
import static io.vavr.API.Match;

@Service
@Slf4j
public class PromoteOperationProcessor extends BaseOperationProcessor implements PromoteOperation {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final TokenWrapper tokenWrapper;

  public PromoteOperationProcessor(
      ConversionService conversionService, Validator validator,
      UserRepository userRepository, RoleRepository roleRepository,
      TokenWrapper tokenWrapper
  ) {
    super(conversionService, validator);
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.tokenWrapper = tokenWrapper;
  }

  @Override
  public Either<? extends ErrorOutput, PromoteOutput> process(PromoteInput input) {
    return validateInput(input)
        .flatMap(validInput ->
            Try.of(() -> {
                  log.info("Start promote input: {}", validInput);

                  checkPrincipalPermissions();

                  UUID userId = UUID.fromString(validInput.getUserId());
                  User user = userRepository.findById(userId)
                      .orElseThrow(() -> new EntityNotFoundException("User", userId));

                  checkForSelfPromotion(user);

                  promoteToAdmin(user);

                  PromoteOutput output = createOutput();
                  log.info("End promote output: {}", output);
                  return output;
                })
                .toEither()
                .mapLeft(t -> Match(t).of(
                    customStatusCase(t, SelfModificationNotAllowedException.class, HttpStatus.CONFLICT),
                    customStatusCase(t, EntityNotFoundException.class, HttpStatus.BAD_REQUEST),
                    customStatusCase(t, IllegalArgumentException.class, HttpStatus.UNPROCESSABLE_ENTITY),
                    defaultCase(t)
                ))
        );
  }

  private void checkForSelfPromotion(User user) {
    if (user.getUsername().equals(tokenWrapper.getUsername())) {
      throw new SelfModificationNotAllowedException(SELF_PROMOTE_MESSAGE);
    }
  }

  private void checkPrincipalPermissions() {
    boolean hasAdminRole = tokenWrapper.getRoles().stream()
        .anyMatch(r -> r.equals(com.tinqinacademy.authentication.api.enums.RoleEnum.ADMIN));
    if (!hasAdminRole) {
      throw new NoPermissionsException();
    }
  }

  private PromoteOutput createOutput() {
    return PromoteOutput.builder().build();
  }

  private void promoteToAdmin(User user) {
    RoleEnum role = RoleEnum.ADMIN;
    Role adminRole = roleRepository.findByType(role)
        .orElseThrow(() -> new UnknownRoleException(String.format(UNKNOWN_ROLE_FORMAT, role)));

    List<Role> userRoles = user.getRoles();
    boolean alreadyHasAdminRole = userRoles.stream().
        anyMatch(r -> r.getType().equals(adminRole.getType()));

    if (alreadyHasAdminRole) {
      return;
    }

    userRoles.add(adminRole);
    userRepository.save(user);
  }
}
