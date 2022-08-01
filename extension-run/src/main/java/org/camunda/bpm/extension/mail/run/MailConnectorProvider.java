package org.camunda.bpm.extension.mail.run;

import org.camunda.connect.Connectors;
import org.camunda.connect.spi.Connector;

public class MailConnectorProvider extends Connectors {
  public static void registerConnector(Connector<?> connector) {
    Connectors.registerConnector(connector);
  }

  public static void unregisterConnector(String connectorId) {
    Connectors.unregisterConnector(connectorId);
  }
}
