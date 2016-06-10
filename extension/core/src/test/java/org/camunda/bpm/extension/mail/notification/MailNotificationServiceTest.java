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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.extension.mail.MailTestUtil;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.dto.Attachment;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

public class MailNotificationServiceTest {

	@Rule
	public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	private MailNotificationService notificationService;

	@Before
	public void init() {
		greenMail.setUser("test@camunda.com", "bpmn");

		notificationService = new MailNotificationService(MailConfigurationFactory.getConfiguration());
	}

	@After
	public void cleanup() {
		notificationService.stop();
	}

	@Test
	public void messageHandler() throws Exception {
		GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "existing mail", "body");

		notificationService.start();

		final List<Message> receivedMessages = new ArrayList<>();
		final CountDownLatch countDownLatch = new CountDownLatch(1);

		notificationService.registerMessageHandler(messages -> {
			receivedMessages.addAll(messages);
			countDownLatch.countDown();
		});

		GreenMailUtil.sendTextEmailTest("test@camunda.com", "from@camunda.com", "new mail", "body");

		countDownLatch.await(10, TimeUnit.SECONDS);

		assertThat(receivedMessages).hasSize(1);
	}

	@Test
	public void mailHandler() throws Exception {
		File attachment = new File(getClass().getResource("/attachment.txt").toURI());
		assertThat(attachment.exists()).isTrue();

		notificationService.start();

		final List<Mail> receivedMails = new ArrayList<>();
		final CountDownLatch countDownLatch = new CountDownLatch(1);

		notificationService.registerMailHandler(mail -> {
			receivedMails.add(mail);
			countDownLatch.countDown();
		});

		Session session = greenMail.getSmtp().createSession();
		MimeMessage message = MailTestUtil.createMimeMessageWithAttachment(session, attachment);
		GreenMailUtil.sendMimeMessage(message);

		countDownLatch.await(10, TimeUnit.SECONDS);

		assertThat(receivedMails).hasSize(1);

		Mail mail = receivedMails.get(0);
		assertThat(mail.getAttachments()).hasSize(1);

		Attachment mailAttachment = mail.getAttachments().get(0);
		assertThat(mailAttachment.getFileName()).isEqualTo("attachment.txt");
		assertThat(mailAttachment.getPath()).isNotNull();
	}

}
