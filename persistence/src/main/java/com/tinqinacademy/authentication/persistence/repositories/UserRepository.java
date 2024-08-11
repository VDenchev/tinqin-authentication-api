package com.tinqinacademy.authentication.persistence.repositories;

import com.tinqinacademy.authentication.persistence.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByUsernameIgnoreCase(String username);
  Optional<User> findByEmailIgnoreCase(String email);

  @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.type = 'ADMIN'")
  Long countUsersWithAdminRole();

  boolean existsByUsernameIgnoreCase(String username);

  boolean existsByEmailIgnoreCase(String email);
  boolean existsByPhoneNumber(String email);
}
