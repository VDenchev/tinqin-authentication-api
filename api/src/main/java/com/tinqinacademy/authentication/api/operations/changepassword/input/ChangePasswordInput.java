package com.tinqinacademy.authentication.api.operations.changepassword.input;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class ChangePasswordInput {

  @NotBlank(message = "Old password must not be blank")
  @Schema(example = "myoldpassword69")
  private String oldPassword;
  @NotBlank(message = "New password must not be blank")
  @Size(min = 6, message = "New password must be at least 6 characters in length")
  @Schema(example = "newpassword")
  private String newPassword;
  @NotBlank(message = "Email must not be blank")
  @Email(message = "Invalid email format", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
  @Schema(example = "ivan.petrov@example.com")
  private String email;
}
