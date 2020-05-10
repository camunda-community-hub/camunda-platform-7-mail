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
package org.camunda.bpm.extension.mail.poll;

import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;

import org.camunda.bpm.extension.mail.MailConnectorException;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;
import org.camunda.connect.impl.AbstractConnector;
import org.camunda.connect.spi.ConnectorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollMailConnector extends AbstractConnector<PollMailRequest, PollMailResponse> {

  private final static Logger LOGGER = LoggerFactory.getLogger(PollMailConnector.class);

  public static final String CONNECTOR_ID = "mail-poll";

  protected MailConfiguration configuration;

  public PollMailConnector() {
    super(CONNECTOR_ID);
  }

  @Override
  public PollMailRequest createRequest() {
    return new PollMailRequest(this, getConfiguration());
  }

  @Override
  public ConnectorResponse execute(PollMailRequest request) {
    MailService mailService = MailServiceFactory.getService(getConfiguration());

    try {

      Folder folder = mailService.ensureOpenFolder(request.getFolder());

      PollMailInvocation invocation = new PollMailInvocation(folder, request, requestInterceptors, mailService);

      @SuppressWarnings("unchecked")
      List<Message> messages = (List<Message>) invocation.proceed();

      LOGGER.debug("poll {} mails from folder '{}'", messages.size(), folder.getName());

      return new PollMailResponse(messages, mailService, request.downloadAttachments(), getConfiguration().getAttachmentPath());

    } catch (Exception e) {
      throw new MailConnectorException("Failed to poll mails: " + e.getMessage(), e);
    }
  }

  protected MailConfiguration getConfiguration() {
    if (configuration == null) {
      configuration = MailConfigurationFactory.getConfiguration();
    }
    return configuration;
  }

  public void setConfiguration(MailConfiguration configuration) {
    this.configuration = configuration;
  }

}
