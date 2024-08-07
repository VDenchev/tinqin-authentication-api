package com.tinqinacademy.authentication.api.operations.confirmregistration.input;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ConfirmRegistrationInput {

  @NotBlank(message = "Confirmation code cannot be blank")
  private String confirmationCode;
}
