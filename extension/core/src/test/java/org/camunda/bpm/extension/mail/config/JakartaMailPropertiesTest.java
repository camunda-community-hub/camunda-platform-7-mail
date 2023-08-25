package org.camunda.bpm.extension.mail.config;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.Test;

public class JakartaMailPropertiesTest {
  @Test
  public void testLoadPropertiesFromClasspath_absolutePath() throws IOException {
    try (InputStream in =
        JakartaMailProperties.getPropertiesAsStream("classpath:/mail-config.properties")) {
      assertThat(in).isNotNull();
      Properties properties = new Properties();
      properties.load(in);
      assertThat(properties.getProperty("mail.smtp.host")).isEqualTo("localhost");
    }
  }

  @Test
  public void testLoadPropertiesFromClasspath_relativePath() throws IOException {
    try (InputStream in =
        JakartaMailProperties.getPropertiesAsStream("classpath:mail-config.properties")) {
      assertThat(in).isNotNull();
      Properties properties = new Properties();
      properties.load(in);
      assertThat(properties.getProperty("mail.smtp.host")).isEqualTo("localhost");
    }
  }
}
