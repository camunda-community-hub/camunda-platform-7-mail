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

import java.time.Duration;
import javax.mail.Folder;
import org.camunda.bpm.extension.mail.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollNotificationWorker implements NotificationWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(PollNotificationWorker.class);

  protected final MailService mailService;
  protected final Folder folder;
  protected final Duration lookupTime;

  protected boolean running = true;

  public PollNotificationWorker(MailService mailService, Folder folder, Duration lookupTime) {
    this.mailService = mailService;
    this.folder = folder;
    this.lookupTime = lookupTime;
  }

  @Override
  public void run() {
    while (running) {

      triggerMailServer();
      waitTillNextLookup();
    }
  }

  protected void triggerMailServer() {
    try {
      LOGGER.debug("trigger the mail server");

      mailService.ensureOpenFolder(folder);
      // This is to force the server to send us EXISTS notifications.
      folder.getMessageCount();

    } catch (Exception e) {
      LOGGER.debug("exception while triggering mail server", e);
    }
  }

  protected void waitTillNextLookup() {
    try {
      Thread.sleep(lookupTime.toMillis());
    } catch (InterruptedException e) {
      // ignore
    }
  }

  @Override
  public void stop() {
    running = false;

    synchronized (this) {
      this.notifyAll();
    }
  }

  @Override
  public String toString() {
    return "PollNotificationWorker [folder="
        + folder.getName()
        + ", lookupTime="
        + lookupTime
        + ", running="
        + running
        + "]";
  }
}
