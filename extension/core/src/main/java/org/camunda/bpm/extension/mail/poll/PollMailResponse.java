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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.mail.Message;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;
import org.camunda.connect.impl.AbstractConnectorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollMailResponse extends AbstractConnectorResponse {

  public static final String PARAM_MAILS = "mails";
  private static final Logger LOGGER = LoggerFactory.getLogger(PollMailResponse.class);
  protected final List<Message> messages;
  protected final boolean downloadAttachments;

  public PollMailResponse(List<Message> messages, boolean downloadAttachments) {
    this.messages = messages;
    this.downloadAttachments = downloadAttachments;
  }

  @Override
  protected void collectResponseParameters(Map<String, Object> responseParameters) {

    List<Mail> mails = new ArrayList<>();
    for (Message message : messages) {

      try {
        Mail mail = Mail.from(message);
        if (downloadAttachments) {
          mail.downloadAttachments(
              MailConfigurationFactory.getInstance().get().getAttachmentPath());
        }

        mails.add(mail);

      } catch (Exception e) {
        LOGGER.error("exception while transforming message to dto", e);
      }
    }

    responseParameters.put(PARAM_MAILS, mails);

    MailServiceFactory.getInstance().get().flush();
  }

  public List<Mail> getMails() {
    return getResponseParameter(PARAM_MAILS);
  }

  @Override
  public String toString() {
    return "PollMailResponse [messages=" + messages + "]";
  }
}
