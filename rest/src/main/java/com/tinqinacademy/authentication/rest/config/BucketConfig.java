package com.tinqinacademy.authentication.rest.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class BucketConfig {

  @Bean
  public Bucket bucket() {
    Bandwidth limit = Bandwidth.builder()
        .capacity(5)
        .refillIntervally(1, Duration.ofHours(1))
        .initialTokens(5)
        .build();
    Bucket bucket = Bucket.builder()
        .addLimit(limit)
        .build();
    return bucket;
  }
}
