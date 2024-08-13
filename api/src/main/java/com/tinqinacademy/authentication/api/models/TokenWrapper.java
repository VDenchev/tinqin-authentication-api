package com.tinqinacademy.authentication.api.models;

import com.tinqinacademy.authentication.api.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TokenWrapper {

  private String token;
  private String username;
  private List<RoleEnum> roles;
  private Instant expirationTime;
}
