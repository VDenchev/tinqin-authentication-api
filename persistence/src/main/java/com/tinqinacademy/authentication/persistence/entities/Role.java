package com.tinqinacademy.authentication.persistence.entities;

import com.tinqinacademy.authentication.persistence.entities.base.BaseEntity;
import com.tinqinacademy.authentication.persistence.enums.RoleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "role_type", nullable = false)
  private RoleEnum type;
}
