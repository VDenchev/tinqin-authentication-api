package com.tinqinacademy.authentication.api.operations.demote.input;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tinqinacademy.authentication.api.base.OperationInput;
import com.tinqinacademy.authentication.api.models.TokenInput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DemoteInput implements OperationInput {

  @UUID(message = "User id must be a valid UUID string")
  private String userId;

  @JsonIgnore
  private TokenInput tokenInput;
}
