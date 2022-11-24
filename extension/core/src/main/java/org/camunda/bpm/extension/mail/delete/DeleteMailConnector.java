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
package org.camunda.bpm.extension.mail.delete;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.OrTerm;
import org.camunda.bpm.extension.mail.EmptyResponse;
import org.camunda.bpm.extension.mail.MailConnectorException;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;
import org.camunda.connect.impl.AbstractConnector;
import org.camunda.connect.spi.ConnectorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteMailConnector extends AbstractConnector<DeleteMailRequest, EmptyResponse> {

  public static final String CONNECTOR_ID = "mail-delete";
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteMailConnector.class);

  public DeleteMailConnector() {
    super(CONNECTOR_ID);
  }

  @Override
  public DeleteMailRequest createRequest() {
    return new DeleteMailRequest(this);
  }

  @Override
  public ConnectorResponse execute(DeleteMailRequest request) {

    try {

      Folder folder = MailServiceFactory.getInstance().get().ensureOpenFolder(request.getFolder());
      List<Message> messages = Arrays.asList(getMessages(folder, request));

      DeleteMailInvocation invocation =
          new DeleteMailInvocation(
              messages, request, requestInterceptors, MailServiceFactory.getInstance().get());

      invocation.proceed();

      return new EmptyResponse();

    } catch (Exception e) {
      throw new MailConnectorException("Failed to delete mails: " + e.getMessage(), e);
    }
  }

  protected Message[] getMessages(Folder folder, DeleteMailRequest request)
      throws MessagingException {

    if (request.getMails() != null) {
      LOGGER.debug("delete mails: {}", request.getMails());

      List<String> messageIds = collectMessageIds(request.getMails());
      return getMessagesByIds(folder, messageIds);

    } else if (request.getMessageIds() != null) {
      LOGGER.debug("delete mails with message ids: {}", request.getMessageIds());

      return getMessagesByIds(folder, request.getMessageIds());

    } else {
      LOGGER.debug("delete mails with message numbers: {}", request.getMessageNumbers());

      int[] numbers = request.getMessageNumbers().stream().mapToInt(i -> i).toArray();
      return folder.getMessages(numbers);
    }
  }

  protected List<String> collectMessageIds(List<Mail> mails) {

    return mails.stream()
        .map(m -> Optional.ofNullable(m.getMessageId()).orElse(""))
        .filter(id -> !id.isEmpty())
        .collect(Collectors.toList());
  }

  protected Message[] getMessagesByIds(Folder folder, List<String> messageIds)
      throws MessagingException {

    OrTerm searchTerm =
        new OrTerm(messageIds.stream().map(MessageIDTerm::new).toArray(MessageIDTerm[]::new));

    return folder.search(searchTerm);
  }
}
