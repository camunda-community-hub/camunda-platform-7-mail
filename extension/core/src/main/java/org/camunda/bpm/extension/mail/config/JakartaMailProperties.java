package org.camunda.bpm.extension.mail.config;

import java.io.File;
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
  private static final String ENV_PROPERTIES_PATH = "MAIL_CONFIG";
  private static final String PROPERTIES_CLASSPATH_PREFIX = "classpath:";
  private static final String DEFAULT_PROPERTIES_PATH =
      PROPERTIES_CLASSPATH_PREFIX + "mail-config.properties";
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
