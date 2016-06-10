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
package org.camunda.bpm.extension.mail.send;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.camunda.bpm.extension.mail.MailConnectors;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

public class SendMailConnectorTest {

  @Rule
  public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void messageHeader() throws MessagingException {

    MailConnectors.sendMail()
      .createRequest()
        .from("test")
        .to("test@camunda.com")
        .subject("subject")
      .execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(1);

    MimeMessage mail = mails[0];

    assertThat(mail.getFrom())
      .hasSize(1)
      .extracting("address").contains("test");

    assertThat(mail.getRecipients(RecipientType.TO))
      .hasSize(1)
      .extracting("address").contains("test@camunda.com");

    assertThat(mail.getSubject()).isEqualTo("subject");
    assertThat(mail.getSentDate()).isNotNull();
  }

  @Test
  public void messageWithCc() throws MessagingException {

   MailConnectors.sendMail()
      .createRequest()
        .from("test")
        .to("test@camunda.com")
        .cc("cc@camunda.com")
        .subject("subject")
      .execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(2);

    assertThat(mails[0].getRecipients(RecipientType.CC))
      .hasSize(1)
      .extracting("address").contains("cc@camunda.com");
  }

  @Test
  public void messageWithBcc() throws MessagingException {

   MailConnectors.sendMail()
      .createRequest()
        .from("test")
        .to("test@camunda.com")
        .bcc("bcc@camunda.com")
        .subject("subject")
      .execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(2);

    assertThat(mails[0].getRecipients(RecipientType.TO))
      .hasSize(1)
      .extracting("address").contains("test@camunda.com");

    assertThat(mails[0].getRecipients(RecipientType.BCC)).isNull();
  }

  @Test
  public void textMessage() throws MessagingException {

   MailConnectors.sendMail()
      .createRequest()
        .from("test")
        .to("test@camunda.com")
        .subject("subject")
        .text("body")
      .execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    MimeMessage mail = mails[0];

    assertThat(GreenMailUtil.getBody(mail)).isEqualTo("body");
  }

  @Test
  public void htmlMessage() throws Exception {

   MailConnectors.sendMail()
      .createRequest()
        .from("test")
        .to("test@camunda.com")
        .subject("subject")
        .text("test")
        .html("<b>test</b>")
      .execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    MimeMessage mail = mails[0];

    assertThat(mail.getContent()).isInstanceOf(MimeMultipart.class);
    MimeMultipart multiPart = (MimeMultipart) mail.getContent();

    assertThat(multiPart.getCount()).isEqualTo(2);
    assertThat(GreenMailUtil.getBody(multiPart.getBodyPart(0))).isEqualTo("test");
    assertThat(GreenMailUtil.getBody(multiPart.getBodyPart(1))).isEqualTo("<b>test</b>");
  }

  @Test
  public void messageWithFileName() throws Exception {

   File attachment = new File(getClass().getResource("/attachment.txt").toURI());
   assertThat(attachment.exists()).isTrue();

   MailConnectors.sendMail()
      .createRequest()
        .from("test")
        .to("test@camunda.com")
        .subject("subject")
        .fileNames(attachment.getPath())
      .execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    MimeMessage mail = mails[0];

    assertThat(mail.getContent()).isInstanceOf(MimeMultipart.class);
    MimeMultipart multiPart = (MimeMultipart) mail.getContent();

    assertThat(multiPart.getCount()).isEqualTo(1);
    assertThat(GreenMailUtil.getBody(multiPart.getBodyPart(0))).isEqualTo("plain text");
  }

  @Test
  public void senderFromConfiguration() throws MessagingException {

   MailConnectors.sendMail()
      .createRequest()
        .to("test@camunda.com")
        .subject("subject")
      .execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(1);

    MimeMessage mail = mails[0];

    assertThat(mail.getFrom())
      .hasSize(1)
      .extracting("address").contains("from@camunda.com");
  }

  @Test
  public void missingFrom() throws MessagingException {
    SendMailConnector connector = new SendMailConnector();
    connector.setConfiguration(mock(MailConfiguration.class));

    thrown.expect(RuntimeException.class);
    thrown.expectMessage("The request is invalid");

   connector
      .createRequest()
        .to("test@camunda.com")
        .subject("subject")
        .text("body")
      .execute();
  }

  @Test
  public void missingTo() throws MessagingException {

    thrown.expect(RuntimeException.class);
    thrown.expectMessage("The request is invalid");

    MailConnectors.sendMail()
      .createRequest()
        .from("test")
        .subject("subject")
        .text("body")
      .execute();
  }

}
