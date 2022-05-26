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
import org.camunda.bpm.extension.mail.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Folder;
import javax.mail.MessagingException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdleNotificationWorker implements NotificationWorker {

    private final static Logger LOGGER = LoggerFactory.getLogger(IdleNotificationWorker.class);

    protected final MailService mailService;
    protected final IMAPFolder folder;
    protected InterruptIdleRunnable interruptIdleRunnable;
    protected ExecutorService executorService = null;
    protected boolean running = true;

    public IdleNotificationWorker(MailService mailService, IMAPFolder folder) {
        this(mailService, folder, null);
    }

    public IdleNotificationWorker(MailService mailService, IMAPFolder folder, InterruptIdleRunnable interruptIdleRunnable) {
        this.mailService = mailService;
        this.folder = folder;
        this.interruptIdleRunnable = interruptIdleRunnable;
    }

    @Override
    public void run() {
        if (interruptIdleRunnable != null) {
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(interruptIdleRunnable);
        }
        while (running) {
            waitingForMails();
        }
    }

    protected void waitingForMails() {
        try {
            mailService.ensureOpenFolder(folder);
            LOGGER.debug("waiting for mails in idle method");
            folder.idle();
        } catch (Exception e) {
            LOGGER.debug("exception while waiting for mails", e);
        }
    }

    @Override
    public void stop() {
        running = false;
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        // perform a NOOP to interrupt IDLE
        try {
            performNoobOnFolder(this.folder);
        } catch (MessagingException e) {
            // ignore
        }
    }

    protected static void performNoobOnFolder(IMAPFolder folder) throws MessagingException {
        LOGGER.debug("Performing a NOOP to interrupt the idle method");
        folder.doCommand(p -> {
            p.simpleCommand("NOOP", null);
            return null;
        });
    }

    @Override
    public String toString() {
        return "IdleNotificationWorker [folder=" + folder.getName() + ", runnning=" + running + "]";
    }

    /**
     * Runnable for interrupt idle method. Used to keep alive the connection to the IMAP server
     */
    public static class InterruptIdleRunnable implements Runnable {

        private static final long INTERRUPT_IDLE_DEFAULT_FREQ = 1740000; // 29 minutes

        private final IMAPFolder folder;

        private final Duration duration;

        public InterruptIdleRunnable(Folder folder) {
            this(folder, null);
        }

        public InterruptIdleRunnable(Folder folder, Duration duration) {
            if (folder instanceof IMAPFolder) {
                this.folder = (IMAPFolder) folder;
            } else {
                throw new IllegalArgumentException("Folder not type of IMAPFolder.");
            }
            this.duration = duration;
        }

        @Override
        public void run() {
            LOGGER.debug("Running interrupt idle runnable");
            while (!Thread.interrupted()) {
                try {
                    long sleepDurationMillis = INTERRUPT_IDLE_DEFAULT_FREQ;
                    if (duration != null) {
                        sleepDurationMillis = duration.toMillis();
                    }
                    Thread.sleep(sleepDurationMillis);
                    // Perform a NOOP just to keep alive the connection
                    performNoobOnFolder(this.folder);
                } catch (InterruptedException e) {
                    // Ignore, just aborting the thread...
                } catch (MessagingException e) {
                    // Shouldn't really happen...
                    LOGGER.warn("Unexpected exception while interrupting the IDLE connection", e);
                }
            }
        }

    }

}
