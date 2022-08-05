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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesMailConfiguration implements MailConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesMailConfiguration.class);

  public static final String ENV_PROPERTIES_PATH = "MAIL_CONFIG";
  public static final String PROPERTIES_CLASSPATH_PREFIX = "classpath:";
  public static final String DEFAULT_PROPERTIES_PATH = PROPERTIES_CLASSPATH_PREFIX + "/mail-config.properties";

  public static final String PROPERTY_USER = "mail.user";
  public static final String PROPERTY_PASSWORD = "mail.password";

  public static final String PROPERTY_POLL_FOLDER = "mail.poll.folder";
  public static final String PROPERTY_SENDER = "mail.sender";
  public static final String PROPERTY_SENDER_ALIAS = "mail.sender.alias";

  public static final String PROPERTY_ATTACHMENT_DOWNLOAD = "mail.attachment.download";
  public static final String PROPERTY_ATTACHMENT_PATH = "mail.attachment.path";
  public static final String DEFAULT_ATTACHMENT_PATH = "attachments";

  public static final String PROPERTY_NOTIFICATION_LOOKUP_TIME = "mail.notification.lookup.time";
  public static final String DEFAULT_NOTIFICATION_LOOKUP_TIME = Duration
      .ofSeconds(60)
      .toString();

  protected Properties properties = null;
  protected String path = null;

  public PropertiesMailConfiguration() {
  }

  public PropertiesMailConfiguration(String path) {
    this.path = path;
  }

  public PropertiesMailConfiguration(Properties properties) {
    this.properties = properties;
  }

  @Override
  public String getUserName() {
    return getProperties().getProperty(PROPERTY_USER);
  }

  @Override
  public String getPassword() {
    return getProperties().getProperty(PROPERTY_PASSWORD);
  }

  @Override
  public String getPollFolder() {
    return getProperties().getProperty(PROPERTY_POLL_FOLDER);
  }

  @Override
  public String getSender() {
    return getProperties().getProperty(PROPERTY_SENDER);
  }

  @Override
  public String getSenderAlias() {
    return getProperties().getProperty(PROPERTY_SENDER_ALIAS);
  }

  @Override
  public boolean downloadAttachments() {
    String downloadAttachments = getProperties().getProperty(PROPERTY_ATTACHMENT_DOWNLOAD);
    return Boolean.parseBoolean(downloadAttachments);
  }

  @Override
  public String getAttachmentPath() {
    return getProperties().getProperty(PROPERTY_ATTACHMENT_PATH, DEFAULT_ATTACHMENT_PATH);
  }

  @Override
  public Duration getNotificationLookupTime() {
    String looukupTime = getProperties().getProperty(
        PROPERTY_NOTIFICATION_LOOKUP_TIME,
        DEFAULT_NOTIFICATION_LOOKUP_TIME
    );
    return Duration.parse(looukupTime);
  }

  @Override
  public Properties getProperties() {
    if (properties == null) {
      properties = loadProperties();
    }
    return properties;
  }

  protected Properties loadProperties() {
    Properties properties = new Properties();
    String path = getPropertiesPath();

    try {
      InputStream inputStream = getProperiesAsStream(path);
      if (inputStream != null) {
        properties.load(inputStream);
        return properties;

      } else {
        throw new IllegalStateException("Cannot find mail configuration at: " + path);
      }

    } catch (IOException e) {
      throw new IllegalStateException("Unable to load mail configuration from: " + path, e);
    }
  }

  protected String getPropertiesPath() {
    return Optional
        .ofNullable(path)
        .orElseGet(() -> Optional
            .ofNullable(System.getenv(ENV_PROPERTIES_PATH))
            .orElse(DEFAULT_PROPERTIES_PATH));
  }

  protected InputStream getProperiesAsStream(String path) throws FileNotFoundException {

    if (path.startsWith(PROPERTIES_CLASSPATH_PREFIX)) {
      String pathWithoutPrefix = path.substring(PROPERTIES_CLASSPATH_PREFIX.length());

      LOGGER.debug("load mail properties from classpath '{}'", pathWithoutPrefix);

      return getClass().getResourceAsStream(pathWithoutPrefix);

    } else {
      Path config = Paths.get(path);

      LOGGER.debug("load mail properties from path '{}'", config.toAbsolutePath());

      File file = config.toFile();
      if (file.exists()) {
        return new FileInputStream(file);
      } else {
        return null;
      }
    }
  }

}
