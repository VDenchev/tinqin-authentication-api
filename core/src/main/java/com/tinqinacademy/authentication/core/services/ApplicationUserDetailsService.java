package com.tinqinacademy.authentication.core.services;

import com.tinqinacademy.authentication.api.models.CustomUserDetails;
import com.tinqinacademy.authentication.persistence.entities.User;
import com.tinqinacademy.authentication.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static com.tinqinacademy.authentication.api.constants.ExceptionMessages.USERNAME_NOT_FOUND_MESSAGE;

@Component
@RequiredArgsConstructor
public class ApplicationUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsernameIgnoreCase(username)
        .orElseThrow(() -> new UsernameNotFoundException(USERNAME_NOT_FOUND_MESSAGE));

    Set<GrantedAuthority> authorities = user.getRoles().stream()
        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getType().name()))
        .collect(Collectors.toSet());

    return CustomUserDetails.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .password(user.getPassword())
        .authorities(authorities)
        .accountNonExpired(true)
        .accountNonLocked(true)
        .enabled(true)
        .credentialsNonExpired(true)
        .build();
  }
}
