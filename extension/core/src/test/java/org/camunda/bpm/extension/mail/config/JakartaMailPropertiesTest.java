package org.camunda.bpm.extension.mail.config;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class JakartaMailPropertiesTest {
  @Test
  public void testLoadPropertiesFromClasspath_absolutePath() throws IOException {
    try (InputStream in =
        JakartaMailProperties.getPropertiesAsStream("classpath:/mail-config.properties")) {
      final Properties properties = streamToProperties(in);
      assertThat(properties.getProperty("mail.smtp.host")).isEqualTo("localhost");
    }
  }

  @Test
  public void testLoadPropertiesFromClasspath_relativePath() throws IOException {
    try (InputStream in =
        JakartaMailProperties.getPropertiesAsStream("classpath:mail-config.properties")) {
      final Properties properties = streamToProperties(in);
      assertThat(properties.getProperty("mail.smtp.host")).isEqualTo("localhost");
    }
  }

  @Test
  public void testLoadPropertiesFromJndi() throws IOException {
    final String JNDI_NAME = "java:jboss/mail/MyCustomMailSession";
    try (InputStream in = JakartaMailProperties.getPropertiesAsStream("jndi:" + JNDI_NAME)) {
      final Properties properties = streamToProperties(in);
      assertThat(properties.getProperty(JakartaMailProperties.PROPNAME_MAIL_SESSION_JNDI_NAME))
          .isEqualTo(JNDI_NAME);
    }
  }

  /**
   * Populate new {@link Properties} object from input stream with the property file content.
   *
   * <p><strong>Attention:</strong> can be used only once with the same stream, because the
   * procedure 'consume' the supplied stream. After {@link InputStream#reset()} the same stream can
   * be used again. Please keep returned property object to access properties.
   *
   * @param in stream
   * @return loaded from stream properties
   * @throws IOException
   */
  private static Properties streamToProperties(InputStream in) throws IOException {
    assertThat(in).isNotNull();
    Properties properties = new Properties();
    properties.load(in);
    return properties;
  }
}
