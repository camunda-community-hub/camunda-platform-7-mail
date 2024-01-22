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

import jakarta.mail.MessagingException;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdleNotificationWorker extends AbstractNotificationWorker<IMAPFolder> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdleNotificationWorker.class);

  @Override
  protected void interrupt() {
    try {
      folder.doCommand(
          p -> {
            p.simpleCommand("NOOP", null);
            return null;
          });
    } catch (MessagingException e) {
      // ignore
    }
  }

  @Override
  protected void idle() {
    try {
      LOGGER.debug("waiting for mails");

      folder.idle();

    } catch (Exception e) {
      LOGGER.debug("exception while waiting for mails", e);
    }
  }
}
