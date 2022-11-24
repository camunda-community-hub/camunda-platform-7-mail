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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.extension.mail.EmptyResponse;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.connect.impl.AbstractConnectorRequest;
import org.camunda.connect.spi.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMailRequest extends AbstractConnectorRequest<EmptyResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendMailRequest.class);

  protected static final String PARAM_FROM = "from";
  protected static final String PARAM_FROM_ALIAS = "fromAlias";

  protected static final String PARAM_TO = "to";
  protected static final String PARAM_CC = "cc";
  protected static final String PARAM_BCC = "bcc";

  protected static final String PARAM_SUBJECT = "subject";

  protected static final String PARAM_TEXT = "text";
  protected static final String PARAM_HTML = "html";

  protected static final String PARAM_FILE_NAMES = "fileNames";
  protected static final String PARAM_FILES = "files";
  protected final MailConfiguration configuration;

  public SendMailRequest(Connector<?> connector) {
    super(connector);
    this.configuration = MailConfigurationFactory.getInstance().get();
  }

  public String getTo() {
    return getRequestParameter(PARAM_TO);
  }

  public SendMailRequest to(String to) {
    setRequestParameter(PARAM_TO, to);
    return this;
  }

  public String getFrom() {
    String from = getRequestParameter(PARAM_FROM);
    if (from == null) {
      from = configuration.getSender();
    }
    return from;
  }

  public SendMailRequest from(String from) {
    setRequestParameter(PARAM_FROM, from);
    return this;
  }

  public String getFromAlias() {
    String alias = getRequestParameter(PARAM_FROM_ALIAS);
    if (alias == null) {
      alias = configuration.getSenderAlias();
    }
    return alias;
  }

  public SendMailRequest fromAlias(String alias) {
    setRequestParameter(PARAM_FROM_ALIAS, alias);
    return this;
  }

  public String getCc() {
    return getRequestParameter(PARAM_CC);
  }

  public SendMailRequest cc(String cc) {
    setRequestParameter(PARAM_CC, cc);
    return this;
  }

  public String getBcc() {
    return getRequestParameter(PARAM_BCC);
  }

  public SendMailRequest bcc(String bcc) {
    setRequestParameter(PARAM_BCC, bcc);
    return this;
  }

  public String getSubject() {
    return getRequestParameter(PARAM_SUBJECT);
  }

  public SendMailRequest subject(String subject) {
    setRequestParameter(PARAM_SUBJECT, subject);
    return this;
  }

  public String getText() {
    return getRequestParameter(PARAM_TEXT);
  }

  public SendMailRequest text(String text) {
    setRequestParameter(PARAM_TEXT, text);
    return this;
  }

  public String getHtml() {
    return getRequestParameter(PARAM_HTML);
  }

  public SendMailRequest html(String html) {
    setRequestParameter(PARAM_HTML, html);
    return this;
  }

  public List<String> getFileNames() {
    return getRequestParameter(PARAM_FILE_NAMES);
  }

  public SendMailRequest fileNames(String... fileNames) {
    setRequestParameter(PARAM_FILE_NAMES, Arrays.asList(fileNames));
    return this;
  }

  public Map<String, ByteArrayInputStream> getFiles() {
    return getRequestParameter(PARAM_FILES);
  }

  public SendMailRequest files(Map<String, ByteArrayInputStream> files) {
    setRequestParameter(PARAM_FILES, files);
    return this;
  }

  @Override
  protected boolean isRequestValid() {

    if (getTo() == null || getTo().isEmpty()) {
      LOGGER.warn("invalid request: missing parameter 'to' in {}", this);
      return false;
    }

    if (getFrom() == null || getFrom().isEmpty()) {
      LOGGER.warn("invalid request: missing parameter 'from' in {}", this);
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return "SendMailRequest [from="
        + getFrom()
        + ", from-alias="
        + getFromAlias()
        + ", to="
        + getTo()
        + ", cc="
        + getCc()
        + ", bcc="
        + getBcc()
        + ", subject="
        + getSubject()
        + ", text="
        + getText()
        + ", html="
        + getHtml()
        + "]";
  }
}
