package com.tinqinacademy.authentication.api.operations.logout.input;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tinqinacademy.authentication.api.base.OperationInput;
import com.tinqinacademy.authentication.api.models.TokenInput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LogoutInput implements OperationInput {

  @JsonIgnore
  private TokenInput tokenInput;
}
