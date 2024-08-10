package com.tinqinacademy.authentication.api.services.base;

import com.tinqinacademy.authentication.api.enums.RoleEnum;

import java.util.List;

public interface TokenProvider {
  String createToken(String username, List<RoleEnum> roles);
  String getUsernameFromToken(String token);
  List<RoleEnum> getRolesFromToken(String token);
}
