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
package org.camunda.bpm.extension.mail.config;

import java.time.Duration;
import java.util.Properties;
import org.camunda.bpm.extension.mail.AbstractFactory;

public class MailConfigurationFactory extends AbstractFactory<MailConfiguration> {
  public static final String PROPERTY_POLL_FOLDER = "mail.poll.folder";
  public static final String PROPERTY_SENDER = "mail.sender";
  public static final String PROPERTY_SENDER_ALIAS = "mail.sender.alias";
  public static final String PROPERTY_ATTACHMENT_DOWNLOAD = "mail.attachment.download";
  public static final String PROPERTY_ATTACHMENT_PATH = "mail.attachment.path";
  public static final String DEFAULT_ATTACHMENT_PATH = "attachments";
  public static final String PROPERTY_NOTIFICATION_LOOKUP_TIME = "mail.notification.lookup.time";
  private static final MailConfigurationFactory INSTANCE = new MailConfigurationFactory();

  public static MailConfigurationFactory getInstance() {
    return INSTANCE;
  }

  @Override
  protected MailConfiguration createInstance() {
    Properties properties = JakartaMailProperties.get();
    PropertiesMailConfiguration configuration = new PropertiesMailConfiguration();
    configuration.setAttachmentPath(
        properties.getProperty(PROPERTY_ATTACHMENT_PATH, DEFAULT_ATTACHMENT_PATH));
    configuration.setDownloadAttachments(
        Boolean.parseBoolean(
            properties.getProperty(PROPERTY_ATTACHMENT_DOWNLOAD, Boolean.TRUE.toString())));
    configuration.setNotificationLookupTime(
        Duration.parse(
            properties.getProperty(
                PROPERTY_NOTIFICATION_LOOKUP_TIME, Duration.ofSeconds(60).toString())));
    configuration.setPollFolder(properties.getProperty(PROPERTY_POLL_FOLDER));
    configuration.setSender(properties.getProperty(PROPERTY_SENDER));
    configuration.setSenderAlias(properties.getProperty(PROPERTY_SENDER_ALIAS));
    return configuration;
  }
}
