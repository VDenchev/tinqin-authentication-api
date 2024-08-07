package com.tinqinacademy.authentication.api.operations.login.input;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class LoginInput {

  @NotBlank(message = "Username must not be blank")
  @Schema(example = "ivan120")
  private String username;

  @NotBlank(message = "Password must not be blank")
  @Schema(example = "ivan123456")
  private String password;
}
