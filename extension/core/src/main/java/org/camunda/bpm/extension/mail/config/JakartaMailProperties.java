package org.camunda.bpm.extension.mail.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class JakartaMailProperties {
  private static final String ENV_PROPERTIES_PATH = "MAIL_CONFIG";
  private static final String PROPERTIES_CLASSPATH_PREFIX = "classpath:";

  /**
   * Prefix to recognize that mail session should be loaded from jndi tree like {@code
   * jndi:java:jboss/mail/Default}
   */
  private static final String PROPERTIES_JNDI_PREFIX = "jndi:";

  private static final String DEFAULT_PROPERTIES_PATH =
      PROPERTIES_CLASSPATH_PREFIX + "mail-config.properties";

  /**
   * Optional property. Value is jndi-name for preconfigure container Mail Session. If defined then
   * container Mail Session will be accessed. example: {@code
   * mail.session.jndi.name="java:jboss/mail/Default"}
   */
  public static final String PROPNAME_MAIL_SESSION_JNDI_NAME = "mail.session.jndi.name";

  private static final Logger LOG = LoggerFactory.getLogger(JakartaMailProperties.class);
  private static Properties properties;

  private JakartaMailProperties() {}

  public static void set(Properties properties) {
    JakartaMailProperties.properties = properties;
  }

  public static Properties get() {
    if (properties != null) {
      return properties;
    }
    Properties properties = new Properties();
    String path = getPropertiesPath();

    try (InputStream inputStream = getPropertiesAsStream(path)) {
      if (inputStream != null) {
        properties.load(inputStream);
        JakartaMailProperties.properties = properties;
        return JakartaMailProperties.properties;
      } else {
        throw new IllegalStateException("Cannot find mail configuration at: " + path);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to load mail configuration from: " + path, e);
    }
  }

  private static String getPropertiesPath() {
    return Optional.ofNullable(System.getenv(ENV_PROPERTIES_PATH)).orElse(DEFAULT_PROPERTIES_PATH);
  }

  protected static InputStream getPropertiesAsStream(String path) throws IOException {

    if (path.startsWith(PROPERTIES_CLASSPATH_PREFIX)) {
      String pathWithoutPrefix = path.substring(PROPERTIES_CLASSPATH_PREFIX.length());
      if (pathWithoutPrefix.startsWith("/")) {
        pathWithoutPrefix = pathWithoutPrefix.substring(1);
      }
      LOG.debug("load mail properties from classpath '{}'", pathWithoutPrefix);

      return JakartaMailProperties.class.getClassLoader().getResourceAsStream(pathWithoutPrefix);
    } else if (path.startsWith(PROPERTIES_JNDI_PREFIX)) {
      final String jndiName = path.substring(PROPERTIES_JNDI_PREFIX.length());
      LOG.debug("use jndi-name '{}' to load preconfigured mail session", jndiName);
      // create property file to be compatible with the rest of configuration properties
      return new ByteArrayInputStream(
          String.format("%s=%s%n", PROPNAME_MAIL_SESSION_JNDI_NAME, jndiName)
              .getBytes(StandardCharsets.UTF_8));
    } else {
      Path config = Paths.get(path);

      LOG.debug("load mail properties from path '{}'", config.toAbsolutePath());

      File file = config.toFile();
      if (file.exists()) {
        return Files.newInputStream(file.toPath());
      } else {
        return null;
      }
    }
  }
}
