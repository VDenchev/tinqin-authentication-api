package com.tinqinacademy.authentication.api.services.base;

public interface MailService {

  void sendConfirmationEmail(String recipient, String verificationCode);
  void sendPasswordRecoveryEmail(String recipient, String recoveryCode);
}
