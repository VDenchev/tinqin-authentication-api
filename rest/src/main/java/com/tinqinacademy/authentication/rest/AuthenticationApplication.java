package com.tinqinacademy.authentication.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.tinqinacademy.authentication")
@EntityScan(basePackages = "com.tinqinacademy.authentication.persistence.entities")
@EnableJpaRepositories(basePackages = "com.tinqinacademy.authentication.persistence.repositories")
@EnableMongoRepositories(basePackages = "com.tinqinacademy.authentication.persistence.mongorepositories")
@EnableAsync
public class AuthenticationApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuthenticationApplication.class, args);
  }

}
