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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.service.FolderWrapper;
import org.camunda.bpm.extension.mail.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailNotificationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MailNotificationService.class);

  private final MailService mailService;
  private final MailConfiguration configuration;

  private final Set<MessageHandler> messageHandlers = new HashSet<>();

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private final AtomicBoolean running = new AtomicBoolean(false);
  private Future<?> completion;
  private NotificationWorker notificationWorker;

  public MailNotificationService(MailConfiguration configuration, MailService mailService) {
    this.configuration = configuration;
    this.mailService = mailService;
  }

  public void start() {
    if (!running.get()) {
      running.set(true);
      try {
        doStart();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void doStart() {
    this.notificationWorker = getNotificationWorker();
    completion =
        executorService.submit(
            () -> {
              while (running.get()) {
                try (FolderWrapper folder = mailService.getFolder(configuration.getPollFolder())) {
                  folder
                      .getFolder()
                      .addMessageCountListener(
                          new MessageCountAdapter() {
                            @Override
                            public void messagesAdded(MessageCountEvent event) {
                              List<Message> messages = Arrays.asList(event.getMessages());
                              messageHandlers.forEach(handler -> handler.accept(messages));
                            }
                          });
                  LOGGER.debug("start notification service: {}", notificationWorker);
                  notificationWorker.accept(folder.getFolder());
                } catch (Exception e) {
                  LOGGER.error("Error while waiting for notifications", e);
                  // never leave the loop
                }
              }
            });
  }

  private NotificationWorker getNotificationWorker() {
    if (supportsIdle()) {
      return new IdleNotificationWorker();
    } else {
      return new PollNotificationWorker(configuration.getNotificationLookupTime());
    }
  }

  public void stop() {
    LOGGER.debug("stop notification service");
    running.set(false);
    notificationWorker.stop();
    try {
      completion.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isRunning() {
    return running.get();
  }

  protected boolean supportsIdle() {
    try (FolderWrapper folder = mailService.getFolder(configuration.getPollFolder())) {
      Store store = folder.getFolder().getStore();
      if (store instanceof IMAPStore) {
        IMAPStore imapStore = (IMAPStore) store;
        try {
          return imapStore.hasCapability("IDLE") && folder.getFolder() instanceof IMAPFolder;
        } catch (MessagingException e) {
          throw new RuntimeException(e);
        }
      } else {
        return false;
      }
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }

  public void registerMessageHandler(MessageHandler handler) {
    messageHandlers.add(handler);
  }

  public MessageHandler registerMailHandler(Consumer<Mail> consumer) {
    MessageTransformationHandler handler =
        new MessageTransformationHandler(
            consumer, configuration.isDownloadAttachments(), configuration.getAttachmentPath());
    registerMessageHandler(handler);
    return handler;
  }

  public void unregisterMessageHandler(MessageHandler messageHandler) {
    messageHandlers.remove(messageHandler);
  }
}
