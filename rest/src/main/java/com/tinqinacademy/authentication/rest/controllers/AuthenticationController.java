package com.tinqinacademy.authentication.rest.controllers;

import com.tinqinacademy.authentication.api.base.OperationOutput;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.operations.changepassword.input.ChangePasswordInput;
import com.tinqinacademy.authentication.api.operations.changepassword.operation.ChangePasswordOperation;
import com.tinqinacademy.authentication.api.operations.changepassword.output.ChangePasswordOutput;
import com.tinqinacademy.authentication.api.operations.changepasswordusingrecoverycode.input.ChangePasswordUsingRecoveryCodeInput;
import com.tinqinacademy.authentication.api.operations.changepasswordusingrecoverycode.operation.ChangePasswordUsingRecoveryCodeOperation;
import com.tinqinacademy.authentication.api.operations.changepasswordusingrecoverycode.output.ChangePasswordUsingRecoveryCodeOutput;
import com.tinqinacademy.authentication.api.operations.confirmregistration.input.ConfirmRegistrationInput;
import com.tinqinacademy.authentication.api.operations.confirmregistration.operation.ConfirmRegistrationOperation;
import com.tinqinacademy.authentication.api.operations.confirmregistration.output.ConfirmRegistrationOutput;
import com.tinqinacademy.authentication.api.operations.demote.input.DemoteInput;
import com.tinqinacademy.authentication.api.operations.demote.operation.DemoteOperation;
import com.tinqinacademy.authentication.api.operations.demote.output.DemoteOutput;
import com.tinqinacademy.authentication.api.operations.login.input.LoginInput;
import com.tinqinacademy.authentication.api.operations.login.operation.LoginOperation;
import com.tinqinacademy.authentication.api.operations.login.output.LoginOutput;
import com.tinqinacademy.authentication.api.operations.promote.input.PromoteInput;
import com.tinqinacademy.authentication.api.operations.promote.operation.PromoteOperation;
import com.tinqinacademy.authentication.api.operations.promote.output.PromoteOutput;
import com.tinqinacademy.authentication.api.operations.recoverpassword.input.RecoverPasswordInput;
import com.tinqinacademy.authentication.api.operations.recoverpassword.operation.RecoverPasswordOperation;
import com.tinqinacademy.authentication.api.operations.recoverpassword.output.RecoverPasswordOutput;
import com.tinqinacademy.authentication.api.operations.register.input.RegisterInput;
import com.tinqinacademy.authentication.api.operations.register.operation.RegisterOperation;
import com.tinqinacademy.authentication.api.operations.register.output.RegisterOutput;
import com.tinqinacademy.authentication.rest.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.CHANGE_PASSWORD;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.CHANGE_PASSWORD_USING_RECOVERY;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.CONFIRM_REGISTRATION;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.DEMOTE;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.LOGIN;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.PROMOTE;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.RECOVER_PASSWORD;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.REGISTER;

@RestController
@RequiredArgsConstructor
public class AuthenticationController extends BaseController {

  private final LoginOperation loginOperation;
  private final RegisterOperation registerOperation;
  private final RecoverPasswordOperation recoverPasswordOperation;
  private final ConfirmRegistrationOperation confirmRegistrationOperation;
  private final ChangePasswordUsingRecoveryCodeOperation changePasswordUsingRecoveryCodeOperation;
  private final ChangePasswordOperation changePasswordOperation;
  private final PromoteOperation promoteOperation;
  private final DemoteOperation demoteOperation;

  @Operation(
      summary = "Logins a user",
      description = "Logins a user and issues a JWT with a 5 minute validity"
  )
  @ApiResponses(value = {
      @ApiResponse(description = "Returns the JWT in the header and empty body", responseCode = "200"),
      @ApiResponse(description = "Wrong credentials ", responseCode = "400"),
      @ApiResponse(description = "Validation error", responseCode = "422")
  })
  @PostMapping(LOGIN)
  public ResponseEntity<OperationOutput> login(@RequestBody LoginInput input) {
    Either<? extends ErrorOutput, LoginOutput> output = loginOperation.process(input);
    return createResponseWithAuthHeader(output, HttpStatus.OK);
  }


  @Operation(
      summary = "Registers a user",
      description = "User receives a confirmation email with a one time code to confirm the email. User cannot login until the email is confirmed"
  )
  @ApiResponses(value = {
      @ApiResponse(description = "Successful registration", responseCode = "201"),
      @ApiResponse(description = "Validation error", responseCode = "422")
  })
  @PostMapping(REGISTER)
  public ResponseEntity<OperationOutput> register(@RequestBody RegisterInput input) {
    Either<? extends ErrorOutput, RegisterOutput> output = registerOperation.process(input);
    return createResponse(output, HttpStatus.CREATED);
  }


  @Operation(
      summary = "Recovers password",
      description = "Sends email with a password recovery code only if the email is registered to a user"
  )
  @ApiResponses(value = {
      @ApiResponse(description = "Sent recovery password if the email is registered", responseCode = "200"),
  })
  @PostMapping(RECOVER_PASSWORD)
  public ResponseEntity<OperationOutput> recoverPassword(@RequestBody RecoverPasswordInput input) {
    Either<? extends ErrorOutput, RecoverPasswordOutput> output = recoverPasswordOperation.process(input);
    return createResponse(output, HttpStatus.OK);
  }

  @Operation(
      summary = "Chane password via a recovery code",
      description = "Changes the user password using a recovery code"
  )
  @ApiResponses(value = {
      @ApiResponse(description = "Changed password", responseCode = "200"),
      @ApiResponse(description = "Invalid recovery code", responseCode = "400"),
  })
  @PostMapping(CHANGE_PASSWORD_USING_RECOVERY)
  public ResponseEntity<OperationOutput> changePasswordUsingRecoveryCode(
      @RequestBody ChangePasswordUsingRecoveryCodeInput input
  ) {
    Either<? extends ErrorOutput, ChangePasswordUsingRecoveryCodeOutput>
        output = changePasswordUsingRecoveryCodeOperation.process(input);
    return createResponse(output, HttpStatus.OK);
  }


  @Operation(
      summary = "Activates registration",
      description = "Activates user account and allows login to to complete successfully"
  )
  @ApiResponses(value = {
      @ApiResponse(description = "Activated user account", responseCode = "200"),
      @ApiResponse(description = "Invalid code", responseCode = "400"),
  })
  @PostMapping(CONFIRM_REGISTRATION)
  public ResponseEntity<OperationOutput> confirmRegistration(@RequestBody ConfirmRegistrationInput input) {
    Either<? extends ErrorOutput, ConfirmRegistrationOutput> output = confirmRegistrationOperation.process(input);
    return createResponse(output, HttpStatus.OK);
  }


  @Operation(
      summary = "Changes user password",
      description = "Changes the password of a user"
  )
  @ApiResponses(value = {
      @ApiResponse(description = "Successfully changed the password", responseCode = "200"),
      @ApiResponse(description = "Validation error", responseCode = "422"),
      @ApiResponse(description = "Not authenticated", responseCode = "401")
  })
  @PostMapping(CHANGE_PASSWORD)
  public ResponseEntity<OperationOutput> changePassword(@RequestBody ChangePasswordInput input) {
    Either<? extends ErrorOutput, ChangePasswordOutput> output = changePasswordOperation.process(input);
    return createResponse(output, HttpStatus.OK);
  }


  @Operation(
      summary = "Promotes a user",
      description = "An admin can promote another user to an admin"
  )
  @ApiResponses(value = {
      @ApiResponse(description = "Successfully promoted user", responseCode = "200"),
      @ApiResponse(description = "Validation error", responseCode = "422"),
      @ApiResponse(description = "Not authenticated", responseCode = "401"),
      @ApiResponse(description = "Not authorized", responseCode = "403")
  })
  @PostMapping(PROMOTE)
  public ResponseEntity<OperationOutput> promote(@RequestBody PromoteInput input) {
    Either<? extends ErrorOutput, PromoteOutput> output = promoteOperation.process(input);
    return createResponse(output, HttpStatus.OK);
  }


  @Operation(
      summary = "Demotes a user",
      description = "An admin can demote another admin to a user. There must be at least one admin"
  )
  @ApiResponses(value = {
      @ApiResponse(description = "Successfully demoted user", responseCode = "200"),
      @ApiResponse(description = "Validation error", responseCode = "422"),
      @ApiResponse(description = "Not authenticated", responseCode = "401"),
      @ApiResponse(description = "Not authorized", responseCode = "403")
  })
  @PostMapping(DEMOTE)
  public ResponseEntity<OperationOutput> demote(@RequestBody DemoteInput input) {
    Either<? extends ErrorOutput, DemoteOutput> output = demoteOperation.process(input);
    return createResponse(output, HttpStatus.OK);
  }
}
