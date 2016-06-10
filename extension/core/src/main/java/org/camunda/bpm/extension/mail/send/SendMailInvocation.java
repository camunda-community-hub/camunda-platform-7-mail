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
package org.camunda.bpm.extension.mail.send;

import java.util.List;

import javax.mail.Message;
import javax.mail.Transport;

import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.connect.impl.AbstractRequestInvocation;
import org.camunda.connect.spi.ConnectorRequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMailInvocation extends AbstractRequestInvocation<Message> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendMailInvocation.class);

  protected final MailService mailService;

  public SendMailInvocation(Message message, SendMailRequest request, List<ConnectorRequestInterceptor> requestInterceptors, MailService mailService) {
    super(message, request, requestInterceptors);

    this.mailService = mailService;
  }

  @Override
  public Object invokeTarget() throws Exception {
    Message message = target;

    LOGGER.debug("send '{}'", Mail.from(message));

    Transport transport = mailService.getTransport();
    transport.sendMessage(message, message.getAllRecipients());

    return null;
  }

}
