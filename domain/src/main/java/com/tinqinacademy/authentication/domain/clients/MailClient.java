package com.tinqinacademy.authentication.domain.clients;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MailClient {

  @Value("${mailgun.api-key}")
  private String apiKey;

  @Bean
  public MailgunMessagesApi mailgunMessagesApi() {
    return MailgunClient.config(apiKey)
        .logLevel(Logger.Level.BASIC)
        .retryer(new Retryer.Default())
        .logger(new Logger.NoOpLogger())
        .errorDecoder(new ErrorDecoder.Default())
        .options(new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true))
        .createAsyncApi(MailgunMessagesApi.class);
  }
}
