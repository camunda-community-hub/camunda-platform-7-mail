package org.camunda.bpm.extension.mail.spring.boot;

import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.spring.boot.app.TestApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApp.class)
public class MailConfigurationTest {

  @RegisterExtension
  static GreenMailExtension greenMailExtension =
      new GreenMailExtension()
          .withConfiguration(GreenMailConfiguration.aConfig().withUser("test@camunda.com", "bpmn"))
          .withPerMethodLifecycle(false);

  @Autowired MailConfiguration mailConfiguration;

  @Test
  public void shouldMapConfigValue() {
    assertTrue(mailConfiguration.getProperties().containsKey("mail.smtp.host"));
    assertEquals("test@camunda.com", mailConfiguration.getUserName());
  }
}
