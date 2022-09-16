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

import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.connect.impl.AbstractConnectorRequest;
import org.camunda.connect.spi.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollMailRequest extends AbstractConnectorRequest<PollMailResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PollMailRequest.class);

  protected static final String PARAM_FOLDER = "folder";
  protected static final String PARAM_DOWNLOAD_ATTACHMENTS = "download-attachements";

  protected final MailConfiguration configuration;

  public PollMailRequest(Connector<?> connector, MailConfiguration configuration) {
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

  public PollMailRequest folder(String folder) {
    setRequestParameter(PARAM_FOLDER, folder);
    return this;
  }

  public boolean downloadAttachments() {
    Boolean downloadAttachments = getRequestParameter(PARAM_DOWNLOAD_ATTACHMENTS);
    if (downloadAttachments == null) {
      downloadAttachments = configuration.downloadAttachments();
    }
    return downloadAttachments;
  }

  public PollMailRequest downloadAttachments(boolean downloadAttachments) {
    setRequestParameter(PARAM_DOWNLOAD_ATTACHMENTS, downloadAttachments);
    return this;
  }

  @Override
  protected boolean isRequestValid() {

    if (getFolder() == null || getFolder().isEmpty()) {
      LOGGER.warn("invalid request: missing parameter 'folder' in {}", this);
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return "PollMailRequest [folder="
        + getFolder()
        + ", download-attachments="
        + downloadAttachments()
        + "]";
  }
}
