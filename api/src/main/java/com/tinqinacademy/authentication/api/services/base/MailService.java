package com.tinqinacademy.authentication.api.services.base;

import java.util.Map;

public interface MailService {

  void sendEmail(String subject, String recipient, Map<String, Object> model, String template);
}
