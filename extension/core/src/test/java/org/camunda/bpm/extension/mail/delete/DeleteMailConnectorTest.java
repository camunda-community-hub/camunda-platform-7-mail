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
package org.camunda.bpm.extension.mail.delete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import javax.mail.Flags.Flag;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.extension.mail.MailConnectors;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

public class DeleteMailConnectorTest {

  @Rule
  public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void createMails() {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-1", "body");
    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-2", "body");
  }

  @Test
  public void deleteMailByNumber() throws MessagingException {

    MailConnectors.deleteMails()
      .createRequest()
        .folder("INBOX")
        .messageNumbers(1)
      .execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(2);
    assertThat(mails[0].isSet(Flag.DELETED)).isTrue();
    assertThat(mails[1].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void deleteMailById() throws MessagingException {

    MimeMessage[] mails = greenMail.getReceivedMessages();
    String messageId = mails[0].getMessageID();

    MailConnectors.deleteMails()
      .createRequest()
        .folder("INBOX")
        .messageIds(messageId)
      .execute();

    mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(2);
    assertThat(mails[0].isSet(Flag.DELETED)).isTrue();
    assertThat(mails[1].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void deleteMailByGivenMail() throws MessagingException {

    Mail mail = MailConnectors.pollMails()
      .createRequest()
        .folder("INBOX")
        .downloadAttachments(false)
      .execute()
      .getMails()
      .get(0);

    MailConnectors.deleteMails()
      .createRequest()
        .folder("INBOX")
        .mails(mail)
      .execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(2);
    assertThat(mails[0].isSet(Flag.DELETED)).isTrue();
    assertThat(mails[1].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void folderFromConfiguration() throws MessagingException {

    MailConnectors.deleteMails()
      .createRequest()
        .messageNumbers(1)
      .execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(2);
    assertThat(mails[0].isSet(Flag.DELETED)).isTrue();
    assertThat(mails[1].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void missingFolder() throws MessagingException {
    DeleteMailConnector connector = new DeleteMailConnector();
    connector.setConfiguration(mock(MailConfiguration.class));

    thrown.expect(RuntimeException.class);
    thrown.expectMessage("The request is invalid");

    connector
      .createRequest()
        .messageNumbers(0)
      .execute();
  }

  @Test
  public void missingMessageCriteria() throws MessagingException {

    thrown.expect(RuntimeException.class);
    thrown.expectMessage("The request is invalid");

    MailConnectors.deleteMails()
      .createRequest()
        .folder("INBOX")
      .execute();
  }

}
