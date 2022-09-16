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

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import javax.mail.MessagingException;
import org.camunda.bpm.extension.mail.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdleNotificationWorker implements NotificationWorker {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdleNotificationWorker.class);

  protected final MailService mailService;
  protected final IMAPFolder folder;

  protected boolean runnning = true;

  public IdleNotificationWorker(MailService mailService, IMAPFolder folder) {
    this.mailService = mailService;
    this.folder = folder;
  }

  @Override
  public void run() {
    while (runnning) {

      waitingForMails();
    }
  }

  protected void waitingForMails() {
    try {
      mailService.ensureOpenFolder(folder);

      LOGGER.debug("waiting for mails");

      folder.idle();

    } catch (Exception e) {
      LOGGER.debug("exception while waiting for mails", e);
    }
  }

  @Override
  public void stop() {
    runnning = false;

    // perform a NOOP to interrupt IDLE
    try {
      folder.doCommand(
          new IMAPFolder.ProtocolCommand() {
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
              p.simpleCommand("NOOP", null);
              return null;
            }
          });
    } catch (MessagingException e) {
      // ignore
    }
  }

  @Override
  public String toString() {
    return "IdleNotificationWorker [folder=" + folder.getName() + ", runnning=" + runnning + "]";
  }
}
