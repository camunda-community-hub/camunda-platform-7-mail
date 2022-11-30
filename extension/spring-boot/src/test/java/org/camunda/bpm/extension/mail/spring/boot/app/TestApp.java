package org.camunda.bpm.extension.mail.spring.boot.app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestApp {
  public static final List<Mail> RECEIVED_MAILS = new ArrayList<>();

  public static void main(String[] args) {
    SpringApplication.run(TestApp.class, args);
  }

  @Bean
  public Consumer<Mail> testConsumer() {
    return RECEIVED_MAILS::add;
  }
}
