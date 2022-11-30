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

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import javax.mail.Flags.Flag;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.camunda.bpm.extension.mail.MailConnectors;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DeleteMailConnectorTest {

  @Rule public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

  @Before
  public void createMails() {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-1", "body");
    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-2", "body");

    MailConfigurationFactory.getInstance().set(null);
    MailServiceFactory.getInstance().set(null);
  }

  @Test
  public void deleteMailByNumber() throws MessagingException {

    MailConnectors.deleteMails().createRequest().folder("INBOX").messageNumbers(1).execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(1);
    assertThat(mails[0].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void deleteMailById() throws MessagingException {

    MimeMessage[] mails = greenMail.getReceivedMessages();
    String messageId = mails[0].getMessageID();

    MailConnectors.deleteMails().createRequest().folder("INBOX").messageIds(messageId).execute();

    mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(1);
    assertThat(mails[0].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void deleteMailByGivenMail() throws MessagingException {

    Mail mail =
        MailConnectors.pollMails()
            .createRequest()
            .folder("INBOX")
            .downloadAttachments(false)
            .execute()
            .getMails()
            .get(0);

    MailConnectors.deleteMails().createRequest().folder("INBOX").mails(mail).execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(1);
    assertThat(mails[0].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void folderFromConfiguration() throws MessagingException {

    MailConnectors.deleteMails().createRequest().messageNumbers(1).execute();

    MimeMessage[] mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(1);
    assertThat(mails[0].isSet(Flag.DELETED)).isFalse();
  }

  @Test
  public void missingFolder() {
    MailConfigurationFactory.getInstance().set(mock(MailConfiguration.class));
    DeleteMailConnector connector = new DeleteMailConnector();
    RuntimeException exception =
        catchThrowableOfType(
            () -> connector.createRequest().messageNumbers(0).execute(), RuntimeException.class);
    assertEquals("The request is invalid", exception.getMessage());
  }

  @Test
  public void missingMessageCriteria() {
    RuntimeException exception =
        catchThrowableOfType(
            () -> MailConnectors.deleteMails().createRequest().folder("INBOX").execute(),
            RuntimeException.class);
    assertEquals("The request is invalid", exception.getMessage());
  }
}
