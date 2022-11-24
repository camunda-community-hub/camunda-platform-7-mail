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
import java.util.function.Consumer;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableProcessApplication("Print Service App")
@SpringBootApplication
public class PrintServiceProcessApplication {
  private static final String INVOICE_PATH;
  private static final Logger LOG = LoggerFactory.getLogger(PrintServiceProcessApplication.class);

  static {
    try {
      INVOICE_PATH =
          new File(
                  PrintServiceProcessApplication.class
                      .getClassLoader()
                      .getResource("invoice.pdf")
                      .toURI())
              .getPath();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(PrintServiceProcessApplication.class, args);
  }

  @Bean
  public Consumer<Mail> mailHandler(RuntimeService runtimeService) {
    return mail -> {
      LOG.info("Received mail: {}", mail);
      runtimeService.startProcessInstanceByKey(
          "printProcess",
          Variables.createVariables().putValue("mail", mail).putValue("invoice", INVOICE_PATH));
    };
  }
}
