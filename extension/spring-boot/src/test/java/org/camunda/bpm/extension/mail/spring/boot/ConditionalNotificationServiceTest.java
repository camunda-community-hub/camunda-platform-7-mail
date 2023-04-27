package org.camunda.bpm.extension.mail.spring.boot;

import static org.assertj.core.api.Assertions.*;

import org.camunda.bpm.extension.mail.notification.MailNotificationService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@Disabled
public class ConditionalNotificationServiceTest {
  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(MailConnectorConfiguration.class);

  @Test
  void shouldNotInstantiateNotificationService() {
    contextRunner
        .withPropertyValues("camunda.bpm.plugin.mail.notification.enabled=false")
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(MailNotificationService.class);
            });
  }

  @Test
  void shouldNotInstantiateNotificationService2() {
    contextRunner
        .withPropertyValues("camunda.bpm.plugin.mail.mail.notification.enabled=false")
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(MailNotificationService.class);
            });
  }
}
