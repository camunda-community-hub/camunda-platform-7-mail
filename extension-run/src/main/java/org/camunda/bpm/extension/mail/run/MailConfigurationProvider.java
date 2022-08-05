package org.camunda.bpm.extension.mail.run;

import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.config.PropertiesMailConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Configuration
public class MailConfigurationProvider {
  private static final Logger LOG = LoggerFactory.getLogger(MailConfigurationProvider.class);

  @Bean
  @Qualifier("mail-connector-configuration")
  @ConfigurationProperties(prefix = "camunda.bpm.plugin.mail")
  public Properties mailConnectorConfigurationProperties() {
    return new Properties();
  }

  @Bean
  public MailConfiguration mailConfiguration(@Qualifier("mail-connector-configuration") Properties properties) {
    LOG.debug("Appending 'mail.' prefix if missing:");
    Properties fixedProperties = new Properties();
    properties
        .stringPropertyNames()
        .forEach(key -> {
          if (key.startsWith("mail.")) {
            LOG.debug("Key '{}' starts with 'mail.', putting directly", key);
            fixedProperties.put(key, properties.getProperty(key));
          } else {
            String fixedKey = "mail." + key;
            LOG.debug("Key '{}' was fixed to '{}', putting fixed key", key, fixedKey);
            fixedProperties.put(fixedKey, properties.getProperty(key));
          }
        });
    MailConfiguration configuration = new PropertiesMailConfiguration(fixedProperties) {
      @Override
      protected Properties loadProperties() {
        // never search for properties on the classpath, only use application.yaml
        return new Properties();
      }
    };
    MailConfigurationFactory.setConfiguration(configuration);
    return configuration;
  }
}
