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
import org.camunda.bpm.extension.mail.EmptyResponse;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.connect.impl.AbstractConnectorRequest;
import org.camunda.connect.spi.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteMailRequest extends AbstractConnectorRequest<EmptyResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteMailRequest.class);

  protected static final String PARAM_FOLDER = "folder";

  protected static final String PARAM_MAILS = "mails";
  protected static final String PARAM_MESSAGE_IDS = "messageIds";
  protected static final String PARAM_MESSAGE_NUMBERS = "messageNumbers";

  protected final MailConfiguration configuration;

  public DeleteMailRequest(Connector<?> connector, MailConfiguration configuration) {
    super(connector);
    this.configuration = configuration;
  }

  public String getFolder() {
    String folder = getRequestParameter(PARAM_FOLDER);
    if (folder == null) {
      folder = configuration.getPollFolder();
    }
    return folder;
  }

  public DeleteMailRequest folder(String folder) {
    setRequestParameter(PARAM_FOLDER, folder);
    return this;
  }

  public List<Mail> getMails() {
    return getRequestParameter(PARAM_MAILS);
  }

  public DeleteMailRequest mails(Mail... mails) {
    setRequestParameter(PARAM_MAILS, Arrays.asList(mails));
    return this;
  }

  public List<String> getMessageIds() {
    return getRequestParameter(PARAM_MESSAGE_IDS);
  }

  public DeleteMailRequest messageIds(String... messageIds) {
    setRequestParameter(PARAM_MESSAGE_IDS, Arrays.asList(messageIds));
    return this;
  }

  public List<Integer> getMessageNumbers() {
    return getRequestParameter(PARAM_MESSAGE_NUMBERS);
  }

  public DeleteMailRequest messageNumbers(Integer... messageNumbers) {
    setRequestParameter(PARAM_MESSAGE_NUMBERS, Arrays.asList(messageNumbers));
    return this;
  }

  @Override
  protected boolean isRequestValid() {

    if (getFolder() == null || getFolder().isEmpty()) {
      LOGGER.warn("invalid request: missing parameter 'folder' in {}", this);
      return false;
    }

    if (!hasMails() && !hasMessageIds() && !hasMessageNumbers()) {
      LOGGER.warn(
          "invalid request: either parameter 'mails', 'messageIds' or 'messageNumbers' must be set in {}",
          this);
      return false;
    }

    return true;
  }

  protected boolean hasMails() {
    return getMails() != null && !getMails().isEmpty();
  }

  protected boolean hasMessageIds() {
    return getMessageIds() != null && !getMessageIds().isEmpty();
  }

  protected boolean hasMessageNumbers() {
    return getMessageNumbers() != null && !getMessageNumbers().isEmpty();
  }

  @Override
  public String toString() {
    return "DeleteMailRequest [folder="
        + getFolder()
        + ", mails="
        + getMails()
        + ", message-ids="
        + getMessageIds()
        + ", message-numbers="
        + getMessageNumbers()
        + "]";
  }
}
