package com.tinqinacademy.authentication.api.operations.register.input;

import com.tinqinacademy.authentication.api.base.OperationInput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class RegisterInput implements OperationInput {

  @NotBlank(message = "Username cannot be blank")
  @Size(min = 2, message = "Username must be at least 2 characters in length")
  @Schema(example = "ivan120")
  private String username;

  @NotBlank(message = "Password cannot be blank")
  @Size(min = 6, message = "Password must be at least 6 characters in length")
  @Schema(example = "ivan123456")
  private String password;

  @NotBlank(message = "First name cannot be blank")
  @Size(min = 2, max = 40, message = "First name must be between 2 and 40 characters long")
  @Schema(example = "Ivan")
  private String firstName;

  @NotBlank(message = "Last name cannot be blank")
  @Size(min = 2, max = 40, message = "Last name must be between 2 and 40 characters long")
  @Schema(example = "Petrov")
  private String lastName;

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Invalid email format", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
  @Schema(example = "ivan.petrov@example.com")
  private String email;

  @NotBlank(message = "PhoneNo cannot be blank")
  @Pattern(regexp = "^\\+\\d{1,3} \\d{9,11}$", message = "Invalid phoneNo format")
  @Schema(example = "+359 863125171")
  private String phoneNo;
}
