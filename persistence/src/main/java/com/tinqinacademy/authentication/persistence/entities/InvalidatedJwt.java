package com.tinqinacademy.authentication.persistence.entities;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document
public class InvalidatedJwt {

  @Id
  private ObjectId id;
  private String token;
  @Indexed(expireAfterSeconds = 0)
  private Instant expiryTime;
}
