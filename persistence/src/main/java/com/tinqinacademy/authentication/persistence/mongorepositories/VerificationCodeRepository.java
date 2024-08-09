package com.tinqinacademy.authentication.persistence.mongorepositories;

import com.tinqinacademy.authentication.persistence.entities.VerificationCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationCodeRepository extends MongoRepository<VerificationCode, ObjectId> {
}
