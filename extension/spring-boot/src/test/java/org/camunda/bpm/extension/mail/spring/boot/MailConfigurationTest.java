package org.camunda.bpm.extension.mail.spring.boot;

import static org.assertj.core.api.Assertions.*;
import static org.camunda.bpm.extension.mail.spring.boot.app.TestApp.*;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.notification.MailNotificationService;
import org.camunda.bpm.extension.mail.service.MailService;
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

  @Autowired MailService mailService;
  @Autowired MailNotificationService mailNotificationService;

  @Test
  public void shouldMapConfigValue() {
    assertThat(mailConfiguration.getSender()).isEqualTo("from@camunda.com");
  }

  @Test
  public void shouldNotifyForReceivedMail() throws InterruptedException {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    assertThat(mailNotificationService.isRunning()).isTrue();
    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-1", "body");
    countDownLatch.await(10, TimeUnit.SECONDS);
    assertThat(RECEIVED_MAILS.size()).isEqualTo(1);
  }
}
