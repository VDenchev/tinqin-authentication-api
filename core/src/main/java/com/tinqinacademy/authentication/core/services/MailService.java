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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

  private final MailgunMessagesApi mailgunMessagesApi;
  private final Configuration freeMarkerConfig;

  @Value("${mailgun.domain}")
  private String DOMAIN;

  public void sendConfirmationEmail(String recipient, String otp) {
    Map<String, Object> model = new HashMap<>();
    model.put("verificationCode", otp);

    try {
      String emailTemplateString = getHtmlFromTemplate("email-verification-template.ftl", model);

      Message message = Message.builder()
          .from("tinqin.hotel@tinqin.com")
          .to(recipient)
          .subject("Verify account")
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
