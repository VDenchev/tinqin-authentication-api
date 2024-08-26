package com.tinqinacademy.authentication.api.operations.recoverpassword.input;

import com.tinqinacademy.authentication.api.base.OperationInput;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class RecoverPasswordInput implements OperationInput {

  @Schema(example = "ivan.petrov@example.com")
  private String email;
}
