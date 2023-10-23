package org.camunda.bpm.extension.mail.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JakartaMailProperties {
  /**
   * Optional property. Value is jndi-name for preconfigure container Mail Session. If defined then
   * container Mail Session will be accessed. example: {@code
   * mail.session.jndi.name="java:jboss/mail/Default"}
   */
  public static final String PROP_NAME_MAIL_SESSION_JNDI_NAME = "mail.session.jndi.name";

  private static final String ENV_PROPERTIES_PATH = "MAIL_CONFIG";
  private static final String PROPERTIES_CLASSPATH_PREFIX = "classpath:";

  /**
   * Prefix to recognize that mail session should be loaded from jndi tree like {@code
   * jndi:java:jboss/mail/Default}
   */
  static final String PROPERTIES_JNDI_PREFIX = "jndi:";

  private static final String DEFAULT_PROPERTIES_PATH =
      PROPERTIES_CLASSPATH_PREFIX + "mail-config.properties";
  private static final Logger LOG = LoggerFactory.getLogger(JakartaMailProperties.class);
  private static String propertiesPath;
  private static Properties properties;

  private JakartaMailProperties() {}

  public static void set(Properties properties) {
    JakartaMailProperties.properties = properties;
  }

  public static void setPropertiesPath(String path) {
    propertiesPath = path;
  }

  public static Properties get() {
    if (properties != null) {
      return properties;
    }
    Properties properties = new Properties();
    String path = getPropertiesPath();
    if (isJndiPath(path)) {
      properties.setProperty(PROP_NAME_MAIL_SESSION_JNDI_NAME, extractJndiName(path));
      return properties;
    }
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
    return Optional.ofNullable(propertiesPath)
        .orElse(
            Optional.ofNullable(System.getenv(ENV_PROPERTIES_PATH))
                .orElse(DEFAULT_PROPERTIES_PATH));
  }

  private static boolean isJndiPath(String path) {
    return path.startsWith(PROPERTIES_JNDI_PREFIX);
  }

  private static boolean isClasspathPath(String path) {
    return path.startsWith(PROPERTIES_CLASSPATH_PREFIX);
  }

  private static String extractJndiName(String path) {
    return path.substring(PROPERTIES_JNDI_PREFIX.length());
  }

  private static String extractClasspath(String path) {
    String classpath = path.substring(PROPERTIES_CLASSPATH_PREFIX.length());
    if (classpath.startsWith("/")) {
      classpath = classpath.substring(1);
    }
    return classpath;
  }

  protected static InputStream getPropertiesAsStream(String path) throws IOException {
    if (isClasspathPath(path)) {
      String classpath = extractClasspath(path);
      LOG.debug("load mail properties from classpath '{}'", classpath);
      return JakartaMailProperties.class.getClassLoader().getResourceAsStream(classpath);
    } else {
      Path config = Paths.get(path);
      LOG.debug("load mail properties from path '{}'", config.toAbsolutePath());
      if (config.toFile().exists()) {
        return Files.newInputStream(config);
      } else {
        return null;
      }
    }
  }
}
