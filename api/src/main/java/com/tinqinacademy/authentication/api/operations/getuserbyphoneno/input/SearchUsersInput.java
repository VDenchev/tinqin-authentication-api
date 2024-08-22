package com.tinqinacademy.authentication.api.operations.getuserbyphoneno.input;

import com.tinqinacademy.authentication.api.base.OperationInput;
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
public class SearchUsersInput implements OperationInput {

  private String phoneNo;
}
