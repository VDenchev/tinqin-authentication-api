package com.tinqinacademy.authentication.persistence.entities;

import com.tinqinacademy.authentication.persistence.entities.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name = "users")
public class User extends BaseEntity {

  @Column(name = "first_name", nullable = false, length = 40)
  private String firstName;
  @Column(name = "last_name", nullable = false, length = 40)
  private String lastName;
  @Column(name = "phone_number", nullable = false, length = 16, unique = true)
  private String phoneNumber;
  @Column(name = "username", nullable = false, unique = true)
  private String username;
  @Column(name = "email", nullable = false, unique = true)
  private String email;
  @Column(name = "password", nullable = false)
  private String password;
  @Column(name="is_verified", nullable = false)
  private Boolean isVerified;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")}
  )
  private List<Role> roles = new ArrayList<>();
}
