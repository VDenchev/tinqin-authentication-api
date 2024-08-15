package com.tinqinacademy.authentication.core.services;

import com.tinqinacademy.authentication.api.services.base.MailService;
import com.tinqinacademy.authentication.persistence.entities.User;
import com.tinqinacademy.authentication.persistence.entities.VerificationCode;
import com.tinqinacademy.authentication.persistence.mongorepositories.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConfirmationService {

  private final MailService mailService;
  private final VerificationCodeRepository verificationCodeRepository;

  @Async
  public void sendConfirmationEmail(User user) {

    String code = generateCode();
    mailService.sendConfirmationEmail(user.getEmail(), code);

    VerificationCode verificationCode = VerificationCode.builder()
        .code(code)
        .userId(user.getId())
        .createdAt(Instant.now())
        .build();

    verificationCodeRepository.save(verificationCode);
  }

  private String generateCode() {
    SecureRandom random = new SecureRandom();

    return random.ints(6, 0, 10)
        .mapToObj(Integer::toString)
        .reduce((a, b) -> a + b)
        .get();
  }
}
