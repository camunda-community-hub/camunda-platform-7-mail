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

import java.io.IOException;
import java.util.Date;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.extension.mail.EmptyResponse;
import org.camunda.bpm.extension.mail.MailContentType;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;
import org.camunda.connect.impl.AbstractConnector;
import org.camunda.connect.spi.ConnectorResponse;

public class SendMailConnector extends AbstractConnector<SendMailRequest, EmptyResponse> {

  public static final String CONNECTOR_ID = "mail-send";

  protected MailConfiguration configuration;

  public SendMailConnector() {
    super(CONNECTOR_ID);
  }

  @Override
  public SendMailRequest createRequest() {
    return new SendMailRequest(this, getConfiguration());
  }

  @Override
  public ConnectorResponse execute(SendMailRequest request) {

    MailService mailService = MailServiceFactory.getService(getConfiguration());

    try {
      Message message = createMessage(request, mailService.getSession());
      SendMailInvocation invocation = new SendMailInvocation(message, request, requestInterceptors, mailService);

      invocation.proceed();

    } catch (Exception e) {
      throw new ProcessEngineException("failed to send mail", e);
    }

    return new EmptyResponse();
  }

  protected Message createMessage(SendMailRequest request, Session session) throws Exception {

    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(request.getFrom()));
    message.setRecipients(RecipientType.TO, InternetAddress.parse(request.getTo()));

    if (request.getCc() != null) {
      message.setRecipients(RecipientType.CC, InternetAddress.parse(request.getCc()));
    }
    if (request.getBcc() != null) {
      message.setRecipients(RecipientType.BCC, InternetAddress.parse(request.getBcc()));
    }

    message.setSentDate(new Date());
    message.setSubject(request.getSubject());

    if (hasContent(request)) {
      createMessageContent(message, request);
    } else {
      message.setText("");
    }

    return message;
  }

  protected boolean hasContent(SendMailRequest request) {
    return request.getText() != null
        || request.getHtml() != null
        || request.getFileNames() != null && !request.getFileNames().isEmpty();
  }

  protected void createMessageContent(Message message, SendMailRequest request) throws MessagingException, IOException {
    if (isTextOnlyMessage(request)) {
      message.setText(request.getText());

    } else {
      Multipart multiPart = new MimeMultipart();

      if (request.getText() != null) {
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(request.getText());
        multiPart.addBodyPart(textPart);
      }

      if (request.getHtml() != null) {
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(request.getHtml(), MailContentType.TEXT_HTML.getType());
        multiPart.addBodyPart(htmlPart);
      }

      if (request.getFileNames() != null) {
        for (String fileName : request.getFileNames()) {
          MimeBodyPart part = new MimeBodyPart();
          part.attachFile(fileName);
          multiPart.addBodyPart(part);
        }
      }

      message.setContent(multiPart);
    }
  }

  protected boolean isTextOnlyMessage(SendMailRequest request) {
    return request.getHtml() == null
        && request.getFileNames() == null;
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
