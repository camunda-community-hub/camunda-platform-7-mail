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
package org.camunda.bpm.extension.mail.example;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.PreUndeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.camunda.bpm.extension.mail.notification.MailNotificationService;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.bpm.extension.mail.service.MailServiceFactory;

@ProcessApplication(name="Print Service App")
public class PrintServiceProcessApplication extends ServletProcessApplication {

  private MailConfiguration configuration;
  private MailNotificationService notificationService;

  @PostDeploy
  public void startService(ProcessEngine processEngine) throws Exception {
    RuntimeService runtimeService = processEngine.getRuntimeService();

    configuration = MailConfigurationFactory.getConfiguration();
    notificationService = new MailNotificationService(configuration);

    notificationService.registerMailHandler(mail -> {
      runtimeService.startProcessInstanceByKey("printProcess",
          Variables.createVariables()
            .putValue("mail", mail)
            .putValue("invoice", getInvoicePath()));
    });

    notificationService.start();
  }

  protected String getInvoicePath() {

    URL resource = getClass().getResource("/invoice.pdf");
    if (resource == null) {
      throw new IllegalStateException("Cannot found invoice file: invoice.pdf");
    }

    try {
      File file = new File(resource.toURI());
      return file.getPath();

    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @PreUndeploy
  public void stopService() throws Exception {

    notificationService.stop();

    MailService mailService = MailServiceFactory.getService(configuration);
    mailService.close();
  }

}
