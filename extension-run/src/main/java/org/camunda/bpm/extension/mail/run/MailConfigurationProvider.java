package org.camunda.bpm.extension.mail.run;

import org.camunda.bpm.extension.mail.config.PropertiesMailConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Configuration
@ConfigurationProperties(prefix = "plugin.mail")
public class MailConfigurationProvider extends PropertiesMailConfiguration {
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  @Override
  public Properties getProperties() {
    return this.properties;
  }
}
