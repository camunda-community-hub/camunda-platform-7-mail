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
package org.camunda.bpm.extension.mail.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.mail.Message;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageTransformationHandler implements MessageHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageTransformationHandler.class);

  protected final Consumer<Mail> consumer;

  protected final boolean downloadAttachments;
  protected final String attachementPath;

  public MessageTransformationHandler(Consumer<Mail> consumer) {
    this(consumer, false, null);
  }

  public MessageTransformationHandler(
      Consumer<Mail> consumer, boolean downloadAttachments, String attachementPath) {
    this.consumer = consumer;
    this.downloadAttachments = downloadAttachments;
    this.attachementPath = attachementPath;
  }

  @Override
  public void accept(List<Message> messages) {
    List<Mail> mails = new ArrayList<>();

    for (Message message : messages) {

      try {
        Mail mail = Mail.from(message);
        if (downloadAttachments) {
          mail.downloadAttachments(attachementPath);
        }

        mails.add(mail);

      } catch (Exception e) {
        LOGGER.warn("exception while transforming a message", e);
      }
    }

    LOGGER.debug("received {} new mails: {}", mails.size(), mails);

    mails.forEach(consumer);
  }
}
