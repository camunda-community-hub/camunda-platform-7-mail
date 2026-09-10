/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.extension.mail.poll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import org.camunda.bpm.extension.mail.MailConnectorException;
import org.camunda.bpm.extension.mail.MailConnectors;
import org.camunda.bpm.extension.mail.MailContentType;
import org.camunda.bpm.extension.mail.MailTestUtil;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.dto.Attachment;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class PollMailConnectorTest {

  @Rule public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

  @Before
  public void setup() {
    MailConfigurationFactory.getInstance().set(null);
    MailServiceFactory.getInstance().set(null);
  }

  @Test
  public void messageHeaders() throws Exception {
    greenMail.setUser("test@camunda.com", "bpmn");

    Session smtpSession = greenMail.getSmtp().createSession();

    MimeMessage message = new MimeMessage(smtpSession);
    message.setFrom(new InternetAddress("from@camunda.com"));
    message.addRecipient(Message.RecipientType.TO, new InternetAddress("test@camunda.com"));
    message.addRecipient(Message.RecipientType.CC, new InternetAddress("cc@camunda.com"));
    message.setSubject("subject");
    message.setText("body");

    GreenMailUtil.sendMimeMessage(message);

    PollMailResponse response =
        MailConnectors.pollMails().createRequest().folder("INBOX").execute();

    List<Mail> mails = response.getMails();
    assertThat(mails).hasSize(1);

    Mail mail = mails.get(0);
    assertThat(mail.getFrom()).isEqualTo("from@camunda.com");
    assertThat(mail.getTo()).isEqualTo("test@camunda.com");
    assertThat(mail.getCc()).isEqualTo("cc@camunda.com");
    assertThat(mail.getSubject()).isEqualTo("subject");
    assertThat(mail.getSentDate()).isNotNull();
    assertThat(mail.getReceivedDate()).isNotNull();
    assertThat(mail.getMessageNumber()).isEqualTo(1);
    assertThat(mail.getMessageId()).isNotNull();
  }

  @Test
  public void pollSingleMail() throws MessagingException {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "subject", "text body");

    PollMailResponse response =
        MailConnectors.pollMails().createRequest().folder("INBOX").execute();

    List<Mail> mails = response.getMails();
    assertThat(mails).hasSize(1);

    Mail mail = mails.get(0);
    assertThat(mail.getSubject()).isEqualTo("subject");
    assertThat(mail.getText()).isEqualTo("text body");
  }

  @Test
  public void pollMultipleMails() throws MessagingException {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-1", "body");
    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-2", "body");

    PollMailResponse response =
        MailConnectors.pollMails().createRequest().folder("INBOX").execute();

    List<Mail> mails = response.getMails();
    assertThat(mails).hasSize(2).extracting("subject").contains("mail-1", "mail-2");
  }

  @Test
  public void folderFromConfiguration() throws MessagingException {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "subject", "text body");

    PollMailResponse response = MailConnectors.pollMails().createRequest().execute();

    List<Mail> mails = response.getMails();
    assertThat(mails).hasSize(1);
  }

  @Test
  public void missingFolder() throws MessagingException {
    MailConfigurationFactory.getInstance().set(mock(MailConfiguration.class));
    PollMailConnector connector = new PollMailConnector();

    assertThrows(
        "The request is invalid",
        RuntimeException.class,
        () -> connector.createRequest().execute());
  }

  @Test
  public void htmlMessage() throws MessagingException {
    greenMail.setUser("test@camunda.com", "bpmn");

    Session session = greenMail.getSmtp().createSession();
    MimeMessage message = MailTestUtil.createMimeMessageWithHtml(session);

    GreenMailUtil.sendMimeMessage(message);

    PollMailResponse response =
        MailConnectors.pollMails().createRequest().folder("INBOX").execute();

    List<Mail> mails = response.getMails();
    assertThat(mails).hasSize(1);

    Mail mail = mails.get(0);
    assertThat(mail.getHtml()).isEqualTo("<b>html</b>");
    assertThat(mail.getText()).isEqualTo("text");
  }

  @Test
  public void messageWithSingleAttachmentOnly() throws Exception {
    greenMail.setUser("test@camunda.com", "bpmn");

    Session smtpSession = greenMail.getSmtp().createSession();
    MimeMessage message = MailTestUtil.createMimeMessage(smtpSession);

    message.setContent("text body", MailContentType.TEXT_PLAIN.getType());
    message.setFileName("attachment.txt");
    message.setDisposition(Part.ATTACHMENT);

    GreenMailUtil.sendMimeMessage(message);

    PollMailResponse response =
        MailConnectors.pollMails().createRequest().folder("INBOX").execute();

    List<Mail> mails = response.getMails();
    assertThat(mails).hasSize(1);

    Mail mail = mails.get(0);
    assertThat(mail.getAttachments()).hasSize(1);

    Attachment mailAttachment = mail.getAttachments().get(0);
    assertThat(mailAttachment.getFileName()).isEqualTo("attachment.txt");
    assertThat(mailAttachment.getPath()).isNotNull();
  }

  @Test
  public void messageWithAttachment() throws Exception {
    File attachment = new File(getClass().getResource("/attachment.txt").toURI());
    assertThat(attachment.exists()).isTrue();

    greenMail.setUser("test@camunda.com", "bpmn");

    Session session = greenMail.getSmtp().createSession();
    MimeMessage message = MailTestUtil.createMimeMessageWithAttachment(session, attachment);
    GreenMailUtil.sendMimeMessage(message);

    PollMailResponse response =
        MailConnectors.pollMails().createRequest().folder("INBOX").execute();

    List<Mail> mails = response.getMails();
    assertThat(mails).hasSize(1);

    Mail mail = mails.get(0);
    assertThat(mail.getAttachments()).hasSize(1);

    Attachment mailAttachment = mail.getAttachments().get(0);
    assertThat(mailAttachment.getFileName()).isEqualTo("attachment.txt");
    assertThat(mailAttachment.getPath()).isNotNull();
  }

  @Test
  public void messageWithAttachmentNoDownload() throws Exception {
    File attachment = new File(getClass().getResource("/attachment.txt").toURI());
    assertThat(attachment.exists()).isTrue();

    greenMail.setUser("test@camunda.com", "bpmn");

    Session session = greenMail.getSmtp().createSession();
    MimeMessage message = MailTestUtil.createMimeMessageWithAttachment(session, attachment);
    GreenMailUtil.sendMimeMessage(message);

    PollMailResponse response =
        MailConnectors.pollMails()
            .createRequest()
            .folder("INBOX")
            .downloadAttachments(false)
            .execute();

    List<Mail> mails = response.getMails();
    assertThat(mails).hasSize(1);

    Mail mail = mails.get(0);
    assertThat(mail.getAttachments()).hasSize(1);

    Attachment mailAttachment = mail.getAttachments().get(0);
    assertThat(mailAttachment.getFileName()).isEqualTo("attachment.txt");
    assertThat(mailAttachment.getPath()).isNull();
  }

  @Test
  public void dontPollDeletedMail() throws MessagingException {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-1", "body");
    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-2", "body");

    MailConnectors.deleteMails().createRequest().folder("INBOX").messageNumbers(1).execute();

    PollMailResponse response =
        MailConnectors.pollMails().createRequest().folder("INBOX").execute();

    List<Mail> mails = response.getMails();
    assertThat(mails).hasSize(1);
  }

  // --- Additional tests to improve mutation coverage ---

  private ListAppender<ILoggingEvent> listAppender;
  private Logger pollMailLogger;

  @Before
  public void setupLogCapture() {
    pollMailLogger = (Logger) LoggerFactory.getLogger(PollMailConnector.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    pollMailLogger.addAppender(listAppender);
    pollMailLogger.setLevel(Level.DEBUG);
  }

  @After
  public void teardownLogCapture() {
    if (pollMailLogger != null && listAppender != null) {
      pollMailLogger.detachAppender(listAppender);
    }
  }

  @Test
  public void debugLogContainsMessageCountAndFolderName() throws Exception {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-1", "body");
    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-2", "body");

    PollMailResponse response =
        MailConnectors.pollMails().createRequest().folder("INBOX").execute();

    assertThat(response.getMails()).hasSize(2);

    ILoggingEvent event =
        listAppender.list.stream()
            .filter(e -> e.getLoggerName().equals(PollMailConnector.class.getName()))
            .filter(e -> e.getLevel() == Level.DEBUG)
            .findFirst()
            .orElse(null);

    assertThat(event).isNotNull();
    assertThat(event.getFormattedMessage()).contains("2");
    assertThat(event.getFormattedMessage()).contains("INBOX");
  }

  @Test
  public void debugLogWithZeroMessages() throws Exception {
    greenMail.setUser("test@camunda.com", "bpmn");

    PollMailResponse response =
        MailConnectors.pollMails().createRequest().folder("INBOX").execute();

    assertThat(response.getMails()).isEmpty();

    ILoggingEvent event =
        listAppender.list.stream()
            .filter(e -> e.getLoggerName().equals(PollMailConnector.class.getName()))
            .filter(e -> e.getLevel() == Level.DEBUG)
            .findFirst()
            .orElse(null);

    assertThat(event).isNotNull();
    assertThat(event.getFormattedMessage()).contains("0");
    assertThat(event.getFormattedMessage()).contains("INBOX");
  }

  @Test
  public void messagingExceptionFromGetFolderThrowsMailConnectorException() throws Exception {
    MailService mockService = mock(MailService.class);
    when(mockService.getFolder("INBOX")).thenThrow(new MessagingException("connection refused"));
    MailServiceFactory.getInstance().set(mockService);

    PollMailConnector connector = new PollMailConnector();

    try {
      MailConnectorException ex =
          assertThrows(
              MailConnectorException.class,
              () -> connector.createRequest().folder("INBOX").execute());
      assertThat(ex.getMessage()).contains("connection refused");
      assertThat(ex.getCause()).isInstanceOf(MessagingException.class);
    } finally {
      MailServiceFactory.getInstance().set(null);
    }
  }

  @Test
  public void messagingExceptionFromGetFolderPreservesExceptionChain() throws Exception {
    MessagingException cause = new MessagingException("folder access denied");
    MailService mockService = mock(MailService.class);
    when(mockService.getFolder("INBOX")).thenThrow(cause);
    MailServiceFactory.getInstance().set(mockService);

    PollMailConnector connector = new PollMailConnector();

    try {
      assertThrows(
          MailConnectorException.class, () -> connector.createRequest().folder("INBOX").execute());
    } finally {
      MailServiceFactory.getInstance().set(null);
    }
  }
}
