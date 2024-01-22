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

import jakarta.mail.Folder;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollNotificationWorker extends AbstractNotificationWorker<Folder> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PollNotificationWorker.class);

  protected final Duration lookupTime;

  public PollNotificationWorker(Duration lookupTime) {

    this.lookupTime = lookupTime;
  }

  protected void triggerMailServer() {
    try {
      LOGGER.debug("trigger the mail server");

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
  protected void interrupt() {
    synchronized (this) {
      this.notifyAll();
    }
  }

  @Override
  protected void idle() {
    triggerMailServer();
    waitTillNextLookup();
  }

  @Override
  public String toString() {
    return "PollNotificationWorker [folder="
        + folder.getName()
        + ", lookupTime="
        + lookupTime
        + ", running="
        + isRunning()
        + "]";
  }
}
