package org.camunda.bpm.extension.mail.run;

import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailConfigurationTest {

  @RegisterExtension
  static GreenMailExtension greenMailExtension =
      new GreenMailExtension()
          .withConfiguration(GreenMailConfiguration.aConfig().withUser("test@camunda.com", "bpmn"))
          .withPerMethodLifecycle(false);

  @Test
  public void shouldMapConfigValue() {
    assertTrue(
        MailConfigurationFactory.getConfiguration().getProperties().containsKey("mail.smtp.host"));
    assertEquals("test@camunda.com", MailConfigurationFactory.getConfiguration().getUserName());
  }
}
