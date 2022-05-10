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

import com.sun.xml.internal.ws.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.StringContent;

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
    public static final String DEFAULT_NOTIFICATION_LOOKUP_TIME = Duration.ofSeconds(60).toString();

    protected Properties properties = null;
    protected String path = null;

    public PropertiesMailConfiguration() {
    }

    public PropertiesMailConfiguration(String path) {
        this.path = path;
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
        String looukupTime = getProperties().getProperty(PROPERTY_NOTIFICATION_LOOKUP_TIME, DEFAULT_NOTIFICATION_LOOKUP_TIME);
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

        properties = loadPropertiesFromEnvironments();

        if (hasEnvironmentVariables(properties)) {
            return properties;
        } else {
            try {
              String path = getPropertiesPath();

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
    }

    protected String getPropertiesPath() {
        return Optional.ofNullable(path).orElseGet(() ->
                Optional.ofNullable(System.getenv(ENV_PROPERTIES_PATH))
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

    /**
     * Load properties from System Enviroments Variables to use
     * with docker or configmaps kubernetes
     * @Author Washington Ferreira
     */
    private Properties loadPropertiesFromEnvironments() {

        Properties properties = new Properties();

        if (isVariableExists(System.getenv("MAIL_TRANSPORT_PROTOCOL")))
            properties.setProperty("mail.transport.protocol", System.getenv("MAIL_TRANSPORT_PROTOCOL"));

        if (isVariableExists(System.getenv("MAIL_SMTP_HOST")))
            properties.setProperty("mail.smtp.host", System.getenv("MAIL_SMTP_HOST"));

        if (isVariableExists(System.getenv("MAIL_SMTP_HOST")))
            properties.setProperty("mail.smtp.port", System.getenv("MAIL_SMTP_HOST"));

        if (isVariableExists(System.getenv("MAIL_SMTP_AUTH")))
            properties.setProperty("mail.smtp.auth", System.getenv("MAIL_SMTP_AUTH"));

        if (isVariableExists(System.getenv("MAIL_SMTP_SSL_ENABLE")))
            properties.setProperty("mail.smtp.ssl.enable", System.getenv("MAIL_SMTP_SSL_ENABLE"));

        if (isVariableExists(System.getenv("MAIL_SMTP_SOCKETFACTORY_PORT")))
            properties.setProperty("mail.smtp.socketFactory.port", System.getenv("MAIL_SMTP_SOCKETFACTORY_PORT"));

        if (isVariableExists(System.getenv("MAIL_IMAPS_PORT")))
            properties.setProperty("mail.imaps.port", System.getenv("MAIL_IMAPS_PORT"));

        if (isVariableExists(System.getenv("MAIL_STORE_PROTOCOL")))
            properties.setProperty("mail.store.protocol", System.getenv("MAIL_STORE_PROTOCOL"));

        if (isVariableExists(System.getenv("MAIL_IMAPS_HOST")))
            properties.setProperty("mail.imaps.host", System.getenv("MAIL_IMAPS_HOST"));

        if (isVariableExists(System.getenv("MAIL_IMAPS_TIMEOUT")))
            properties.setProperty("mail.imaps.timeout", System.getenv("MAIL_IMAPS_TIMEOUT"));

        if (isVariableExists(System.getenv("MAIL_IMAPS_PEEK")))
            properties.setProperty("mail.imaps.peek", System.getenv("MAIL_IMAPS_PEEK"));

        if (isVariableExists(System.getenv("MAIL_POLL_FOLDER")))
            properties.setProperty("mail.poll.folder", System.getenv("MAIL_POLL_FOLDER"));

        if (isVariableExists(System.getenv("MAIL_SENDER")))
            properties.setProperty("mail.sender", System.getenv("MAIL_SENDER"));

        if (isVariableExists(System.getenv("MAIL_SENDER_ALIAS")))
            properties.setProperty("mail.sender.alias", System.getenv("MAIL_SENDER_ALIAS"));

        if (isVariableExists(System.getenv("MAIL_ATTACHMENT_DOWNLOAD")))
            properties.setProperty("mail.attachment.download", System.getenv("MAIL_ATTACHMENT_DOWNLOAD"));

        if (isVariableExists(System.getenv("MAIL_ATTACHMENT_PATH")))
            properties.setProperty("mail.attachment.path", System.getenv("MAIL_ATTACHMENT_PATH"));

        if (isVariableExists(System.getenv("MAIL_PASSWORD")))
            properties.setProperty("mail.password", System.getenv("MAIL_PASSWORD"));

        if (isVariableExists(System.getenv("MAIL_USER")))
            properties.setProperty("mail.user", System.getenv("MAIL_USER"));

        return properties;

    }

    private boolean hasEnvironmentVariables(Properties props) {
        return props.size() > 0;
    }

    private boolean isVariableExists(String valor) {
        return (valor == null || valor.trim().length() == 0) ? false : true;
    }

}

