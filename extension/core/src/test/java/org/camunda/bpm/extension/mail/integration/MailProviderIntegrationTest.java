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
package org.camunda.bpm.extension.mail.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;

import org.camunda.bpm.extension.mail.MailConnectors;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.config.PropertiesMailConfiguration;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.notification.MailNotificationService;
import org.junit.Before;
import org.junit.Test;

public class MailProviderIntegrationTest {

	// replace by the configuration you want to test
	private static final String CONFIG_PATH = "configs/gmail.properties";

	// replace by the mail address you want to send the mails to
	private static final String MAIL_ADDRESS = "USER@gmail.com";

	@Before
	public void initConfig() {
		PropertiesMailConfiguration configuration = new PropertiesMailConfiguration(CONFIG_PATH);
		MailConfigurationFactory.setConfiguration(configuration);
	}

	@Test
	public void textMessage() throws MessagingException {

		// send a new mail
		MailConnectors.sendMail().createRequest().from(MAIL_ADDRESS).fromAlias("testing").to(MAIL_ADDRESS).subject("camunda-bpm-mail")
				.text("integration test").execute();

		// poll the mail
		List<Mail> mails = MailConnectors.pollMails().createRequest().downloadAttachments(false).execute().getMails();

		assertThat(mails).hasSize(1).extracting("subject").contains("camunda-bpm-mail");

		// delete the mail
		MailConnectors.deleteMails().createRequest().mails(mails.get(0)).execute();

		// verify that the mail is deleted
		mails = MailConnectors.pollMails().createRequest().downloadAttachments(false).execute().getMails();

		assertThat(mails).hasSize(0);
	}

	@Test
	public void notificationService() throws Exception {
		MailNotificationService notificationService = new MailNotificationService(
				MailConfigurationFactory.getConfiguration());

		// register the handler
		final List<Mail> receivedMails = new ArrayList<>();
		final CountDownLatch countDownLatch = new CountDownLatch(1);

		notificationService.registerMailHandler(mail -> {
			receivedMails.add(mail);
			countDownLatch.countDown();
		});

		// start the service
		notificationService.start();

		// send a new mail
		MailConnectors.sendMail().createRequest().from(MAIL_ADDRESS).to(MAIL_ADDRESS).subject("camunda-bpm-mail")
				.text("integration test").execute();

		// wait till handler is invoked
		countDownLatch.await(100 * 30, TimeUnit.SECONDS);

		assertThat(receivedMails).hasSize(1).extracting("subject").contains("camunda-bpm-mail");

		// clean up - delete the mail
		MailConnectors.deleteMails().createRequest().mails(receivedMails.get(0)).execute();

		notificationService.stop();
	}

}
