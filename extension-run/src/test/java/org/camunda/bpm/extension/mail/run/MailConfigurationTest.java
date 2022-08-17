package org.camunda.bpm.extension.mail.run;

import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MailConfigurationTest {

  @Test
  public void shouldMapConfigValue() {
    assertTrue(MailConfigurationFactory
        .getConfiguration()
        .getProperties()
        .containsKey("mail.foo"));
  }
}
