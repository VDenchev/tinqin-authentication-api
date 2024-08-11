package com.tinqinacademy.authentication.core.services;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

  public static final String SENDER_NAME = "hotel@tinqinacademy.com";

  private final MailgunMessagesApi mailgunMessagesApi;
  private final Configuration freeMarkerConfig;

  @Value("${mailgun.domain}")
  private String DOMAIN;

  public void sendConfirmationEmail(String recipient, String verificationCode) {
    Map<String, Object> model = new HashMap<>();
    model.put("verificationCode", verificationCode);

    try {
      String emailTemplateString = getHtmlFromTemplate("email-verification-template.ftl", model);

      Message message = Message.builder()
          .from(SENDER_NAME)
          .to(recipient)
          .subject("Verify account")
          .html(emailTemplateString)
          .build();


      MessageResponse messageResponse = mailgunMessagesApi.sendMessage(DOMAIN, message);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Async
  public void sendPasswordRecoveryEmail(String recipient, String recoveryCode) {

    Map<String, Object> model = new HashMap<>();
    model.put("recoveryCode", recoveryCode);

    try {
      String emailTemplateString = getHtmlFromTemplate("password-recovery-template.ftl", model);

      Message message = Message.builder()
          .from(SENDER_NAME)
          .to(recipient)
          .subject("Recover password")
          .html(emailTemplateString)
          .build();


      MessageResponse messageResponse = mailgunMessagesApi.sendMessage(DOMAIN, message);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String getHtmlFromTemplate(String templateName, Map<String, Object> model) throws IOException, TemplateException {
    Template template = freeMarkerConfig.getTemplate(templateName);
    try (StringWriter stringWriter = new StringWriter()) {
      template.process(model, stringWriter);
      return stringWriter.getBuffer().toString();
    }
  }
}
