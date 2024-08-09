package com.tinqinacademy.authentication.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document
public class VerificationCode {

  @Id
  private ObjectId id;
  private String code;
  private UUID userId;
  @Indexed(name = "ttl_index", expireAfterSeconds = 1800)
  private Instant createdAt;
}
