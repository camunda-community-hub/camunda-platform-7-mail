package org.camunda.bpm.extension.mail.run;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.extension.mail.MailConnectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class MailConnectorPlugin extends AbstractProcessEnginePlugin {
  @Autowired
  protected MailConfigurationProvider mailConfigurationProvider;

  private final Logger LOGGER = Logger.getLogger(this.getClass().getName());

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    LOGGER.info("Configuring Mail Connectors");
    MailConnectors.deleteMails().setConfiguration(mailConfigurationProvider);
    MailConnectors.pollMails().setConfiguration(mailConfigurationProvider);
    MailConnectors.sendMail().setConfiguration(mailConfigurationProvider);
  }

  public MailConfigurationProvider getMailConfigurationProvider() {
    return mailConfigurationProvider;
  }

  public void setMailConfigurationProvider(MailConfigurationProvider mailConfiguration) {
    this.mailConfigurationProvider = mailConfiguration;
  }
}
