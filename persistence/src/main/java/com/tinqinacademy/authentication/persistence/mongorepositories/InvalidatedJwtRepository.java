package com.tinqinacademy.authentication.persistence.mongorepositories;

import com.tinqinacademy.authentication.persistence.entities.InvalidatedJwt;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvalidatedJwtRepository extends MongoRepository<InvalidatedJwt, ObjectId> {

  boolean existsByToken(String token);
}
