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
import static org.mockito.Mockito.mock;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.io.File;
import java.util.List;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.camunda.bpm.extension.mail.MailConnectors;
import org.camunda.bpm.extension.mail.MailContentType;
import org.camunda.bpm.extension.mail.MailTestUtil;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.dto.Attachment;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PollMailConnectorTest {

  @Rule public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

  @Rule public final ExpectedException thrown = ExpectedException.none();

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

    PollMailConnector connector = new PollMailConnector();
    connector.setConfiguration(mock(MailConfiguration.class));

    thrown.expect(RuntimeException.class);
    thrown.expectMessage("The request is invalid");

    connector.createRequest().execute();
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
}
