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

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailNotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MailNotificationService.class);

  protected final MailService mailService;
  protected final MailConfiguration configuration;

  protected final List<MessageHandler> handlers = new LinkedList<>();

  protected ExecutorService executorService = null;

  protected NotificationWorker notificationWorker;

  public MailNotificationService(MailConfiguration configuration) {
    this.configuration = configuration;
    this.mailService = MailServiceFactory.getService(configuration);
  }

  public void start() throws Exception {
    start(configuration.getPollFolder());
  }

  public void start(String folderName) throws Exception {
    executorService = Executors.newSingleThreadExecutor();

    Folder folder = mailService.ensureOpenFolder(folderName);

    folder.addMessageCountListener(
        new MessageCountAdapter() {
          @Override
          public void messagesAdded(MessageCountEvent event) {
            List<Message> messages = Arrays.asList(event.getMessages());

            handlers.forEach(handler -> handler.accept(messages));
          }
        });

    if (supportsIdle(folder)) {
      notificationWorker = new IdleNotificationWorker(mailService, (IMAPFolder) folder);
    } else {
      notificationWorker =
          new PollNotificationWorker(
              mailService, folder, configuration.getNotificationLookupTime());
    }

    LOGGER.debug("start notification service: {}", notificationWorker);

    executorService.submit(notificationWorker);
  }

  public void stop() {
    if (notificationWorker != null) {
      LOGGER.debug("stop notification service");

      notificationWorker.stop();

      executorService.shutdown();
      executorService = null;
    }
  }

  protected boolean supportsIdle(Folder folder) throws MessagingException {
    Store store = folder.getStore();

    if (store instanceof IMAPStore) {
      IMAPStore imapStore = (IMAPStore) store;
      return imapStore.hasCapability("IDLE") && folder instanceof IMAPFolder;
    } else {
      return false;
    }
  }

  public void registerMessageHandler(MessageHandler handler) {
    handlers.add(handler);
  }

  public void registerMailHandler(Consumer<Mail> consumer) {
    MessageTransformationHandler handler =
        new MessageTransformationHandler(
            consumer, configuration.downloadAttachments(), configuration.getAttachmentPath());
    registerMessageHandler(handler);
  }
}
