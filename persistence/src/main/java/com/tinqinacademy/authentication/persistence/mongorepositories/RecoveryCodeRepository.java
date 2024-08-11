package com.tinqinacademy.authentication.persistence.mongorepositories;

import com.tinqinacademy.authentication.persistence.entities.RecoveryCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecoveryCodeRepository extends MongoRepository<RecoveryCode, ObjectId> {

  void deleteAllByUserId(UUID userId);
  Optional<RecoveryCode> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);
}
