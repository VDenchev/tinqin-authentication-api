package com.tinqinacademy.authentication.rest.controllers;

import com.tinqinacademy.authentication.api.base.Output;
import com.tinqinacademy.authentication.api.errors.ErrorOutput;
import com.tinqinacademy.authentication.api.operations.getuserbyphoneno.input.SearchUsersInput;
import com.tinqinacademy.authentication.api.operations.getuserbyphoneno.operation.SearchUsersOperation;
import com.tinqinacademy.authentication.api.operations.getuserbyphoneno.output.SearchUsersOutput;
import com.tinqinacademy.authentication.api.operations.validatetoken.input.ValidateTokenInput;
import com.tinqinacademy.authentication.api.operations.validatetoken.operation.ValidateTokenOperation;
import com.tinqinacademy.authentication.api.operations.validatetoken.output.ValidateTokenOutput;
import com.tinqinacademy.authentication.rest.base.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.SEARCH_USERS;
import static com.tinqinacademy.authentication.api.apiroutes.RestApiRoutes.VALIDATE_TOKEN;

@RestController
@RequiredArgsConstructor
public class ExportsController extends BaseController {

  private final ValidateTokenOperation validateTokenOperation;
  private final SearchUsersOperation searchUsersOperation;

  @Operation(
      summary = "Validates JWT",
      description = "Returns user details if the token is valid"
  )
  @ApiResponses(value = {
      @ApiResponse(description = "Returns user details", responseCode = "200"),
      @ApiResponse(description = "Invalid token", responseCode = "400")
  })
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping(VALIDATE_TOKEN)
  public ResponseEntity<Output> validateToken(@RequestHeader(value = "Authorization", required = false, defaultValue = "") String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    ValidateTokenInput input = ValidateTokenInput.builder()
        .token(token)
        .build();

    Either<? extends ErrorOutput, ValidateTokenOutput> output = validateTokenOperation.process(input);
    return createResponse(output, HttpStatus.OK);
  }

  @Operation(
      summary = "Searches users",
      description = "Returns a list of user ids"
  )
  @ApiResponses(value = {
      @ApiResponse(description = "List of user ids", responseCode = "200"),
  })
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping(SEARCH_USERS)
  public ResponseEntity<Output> searchUsers(@RequestParam(required = false, defaultValue = "") String phoneNo) {
    SearchUsersInput input = SearchUsersInput.builder()
        .phoneNo(phoneNo)
        .build();

    Either<? extends ErrorOutput, SearchUsersOutput> output = searchUsersOperation.process(input);
    return createResponse(output, HttpStatus.OK);
  }
}
