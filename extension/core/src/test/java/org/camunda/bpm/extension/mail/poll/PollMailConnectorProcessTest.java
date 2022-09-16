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

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import java.util.List;
import javax.mail.MessagingException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.junit.Rule;
import org.junit.Test;

public class PollMailConnectorProcessTest {

  @Rule public ProcessEngineRule engineRule = new ProcessEngineRule();

  @Rule public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

  @Test
  @Deployment(resources = "processes/mail-poll.bpmn")
  public void pollMailWithTextBody() throws MessagingException {
    greenMail.setUser("test@camunda.com", "bpmn");

    GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "subject", "text body");

    ProcessInstance processInstance =
        engineRule.getRuntimeService().startProcessInstanceByKey("poll-mails");

    @SuppressWarnings("unchecked")
    List<Mail> mails =
        (List<Mail>) engineRule.getRuntimeService().getVariable(processInstance.getId(), "mails");

    assertThat(mails).isNotNull().hasSize(1);

    Mail mail = mails.get(0);
    assertThat(mail.getFrom()).isEqualTo("from@camunda.com");
    assertThat(mail.getSubject()).isEqualTo("subject");
    assertThat(mail.getText()).isEqualTo("text body");
  }
}
