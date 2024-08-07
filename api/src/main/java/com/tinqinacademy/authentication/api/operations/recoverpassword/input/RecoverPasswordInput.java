package com.tinqinacademy.authentication.api.operations.recoverpassword.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
public class RecoverPasswordInput {

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Invalid email format", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
  @Schema(example = "ivan.petrov@example.com")
  private String email;
}
