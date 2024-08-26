package com.tinqinacademy.authentication.core.services;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import com.tinqinacademy.authentication.api.services.base.MailService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailgunService implements MailService {

  public static final String SENDER_NAME = "hotel@tinqinacademy.com";

  private final MailgunMessagesApi mailgunMessagesApi;
  private final Configuration freeMarkerConfig;

  @Value("${mailgun.domain}")
  private String DOMAIN;

  @Async
  @Override
  public void sendEmail(String subject, String recipient, Map<String, Object> model, String template) {
    try {
      String emailTemplateString = getHtmlFromTemplate(template, model);
      log.info("Sending email from " + SENDER_NAME);

      Message message = Message.builder()
          .from(SENDER_NAME)
          .to(recipient)
          .subject(subject)
          .html(emailTemplateString)
          .build();

      mailgunMessagesApi.sendMessage(DOMAIN, message);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getHtmlFromTemplate(String templateName, Map<String, Object> model) throws IOException, TemplateException {
    Template template = freeMarkerConfig.getTemplate(templateName);
    try (StringWriter stringWriter = new StringWriter()) {
      template.process(model, stringWriter);
      return stringWriter.getBuffer().toString();
    }
  }
}
