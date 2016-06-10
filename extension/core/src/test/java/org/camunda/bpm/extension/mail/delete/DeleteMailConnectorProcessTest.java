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

import javax.mail.Flags.Flag;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

public class DeleteMailConnectorProcessTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  @Rule
  public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

  @Before
  public void createMails() {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-1", "body");
    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "mail-2", "body");
  }

  @Test
  @Deployment(resources = "processes/mail-delete.bpmn")
  public void deleteMailById() throws MessagingException {

    MimeMessage[] mails = greenMail.getReceivedMessages();
    String messageId = mails[0].getMessageID();

    engineRule.getRuntimeService().startProcessInstanceByKey("delete-mail",
        Variables.createVariables().putValue("messageId", messageId));

    mails = greenMail.getReceivedMessages();
    assertThat(mails).hasSize(2);
    assertThat(mails[0].isSet(Flag.DELETED)).isTrue();
    assertThat(mails[1].isSet(Flag.DELETED)).isFalse();
  }

}
