package com.tinqinacademy.authentication.core.converters;

import com.tinqinacademy.authentication.api.operations.register.input.RegisterInput;
import com.tinqinacademy.authentication.core.converters.base.BaseConverter;
import com.tinqinacademy.authentication.persistence.entities.User;
import org.springframework.stereotype.Component;

@Component
public class RegisterInputToUser extends BaseConverter<RegisterInput, User> {

  @Override
  protected User doConvert(RegisterInput source) {
    return User.builder()
        .email(source.getEmail())
        .firstName(source.getFirstName())
        .lastName(source.getLastName())
        .username(source.getUsername())
        .phoneNumber(source.getPhoneNo())
        .build();
  }
}
