package com.tinqinacademy.authentication.persistence.seeders;

import com.tinqinacademy.authentication.persistence.entities.Role;
import com.tinqinacademy.authentication.persistence.enums.RoleEnum;
import com.tinqinacademy.authentication.persistence.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements CommandLineRunner {

  private final RoleRepository roleRepository;
  @Override
  public void run(String... args) throws Exception {
    List<Role> savedRoles = roleRepository.findAll();

    Arrays.stream(RoleEnum.values())
        .filter(bed -> savedRoles.stream()
            .noneMatch(sr -> sr.getType().name().equals(bed.name()))
        ).forEach(b -> {
              Role role = Role.builder()
                  .type(b)
                  .build();
              roleRepository.save(role);
              log.info("RoleSeeder - saved role {}: ", role);
            }
        );

  }
}
