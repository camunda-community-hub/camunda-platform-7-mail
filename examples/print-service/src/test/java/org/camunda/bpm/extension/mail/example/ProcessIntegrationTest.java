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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import javax.activation.DataHandler;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.extension.mail.MailContentType;
import org.camunda.bpm.extension.mail.dto.Mail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProcessIntegrationTest {
  @RegisterExtension
  static GreenMailExtension greenMailExtension =
      new GreenMailExtension()
          .withConfiguration(GreenMailConfiguration.aConfig().withUser("test@camunda.com", "bpmn"))
          .withPerMethodLifecycle(false);

  @Test
  void shouldPrintInvoiceOnAttachmentExists()
      throws MessagingException, InterruptedException, URISyntaxException, IOException {
    sendMessage(true);
    ProcessInstance processInstance = awaitProcessInstanceExists();
    assertThat(processInstance).isWaitingAt(findId("Print the attachment"));
    Map<String, Object> variables = runtimeService().getVariables(processInstance.getId());
    assertThat(variables).containsKey("mail");
    Object mailAsObject = variables.get("mail");
    assertThat(mailAsObject).isNotNull().isInstanceOf(Mail.class);
    Mail mail = (Mail) mailAsObject;
    assertThat(mail.getAttachments()).hasSize(1);
    String path = mail.getAttachments().get(0).getPath();
    assertThat(path).isNotNull();
    byte[] bytes = Files.readAllBytes(new File(path).toPath());
    assertThat(bytes).isNotEmpty();
    assertThat(variables).containsKey("invoice");
    Object invoiceAsObject = variables.get("invoice");
    assertThat(invoiceAsObject).isNotNull().isInstanceOf(String.class);
    String invoice = (String) invoiceAsObject;
    byte[] bytes1 = Files.readAllBytes(new File(invoice).toPath());
    assertThat(bytes1).isNotEmpty();
    complete(task());
    awaitProcessInstanceComplete();
    assertThat(processInstance).isEnded();
    assertThat(
            Arrays.stream(greenMailExtension.getReceivedMessages())
                .filter(
                    m -> {
                      try {
                        return m.getSubject().equals("invoice");
                      } catch (MessagingException e) {
                        throw new RuntimeException(e);
                      }
                    })
                .findFirst())
        .isNotEmpty();
  }

  @Test
  void shouldInformCustomerOnAttachmentMissing()
      throws MessagingException, URISyntaxException, IOException, InterruptedException {
    sendMessage(false);
    awaitProcessInstanceComplete();
    assertThat(
            Arrays.stream(greenMailExtension.getReceivedMessages())
                .filter(
                    m -> {
                      try {
                        return m.getSubject().startsWith("RE:");
                      } catch (MessagingException e) {
                        throw new RuntimeException(e);
                      }
                    })
                .findFirst())
        .isNotEmpty();
  }

  private void sendMessage(boolean withAttachment)
      throws MessagingException, URISyntaxException, IOException {
    Session smtpSession = greenMailExtension.getSmtp().createSession();
    MimeMessage message = new MimeMessage(smtpSession);
    if (withAttachment) {
      Multipart content = new MimeMultipart();
      MimeBodyPart attachment = new MimeBodyPart();
      ByteArrayDataSource ds =
          new ByteArrayDataSource(
              Files.readAllBytes(
                  new File(getClass().getClassLoader().getResource("printme.txt").toURI())
                      .toPath()),
              "text/plain");
      attachment.setDataHandler(new DataHandler(ds));
      attachment.setFileName("printme.txt");
      content.addBodyPart(attachment);
      message.setContent(content);
      message.setDisposition(Part.ATTACHMENT);
    } else {
      message.setContent("text body", MailContentType.TEXT_PLAIN.getType());
      message.setDisposition(Part.INLINE);
    }
    message.setRecipient(RecipientType.TO, new InternetAddress("test@camunda.com"));
    message.setSender(new InternetAddress("from@camunda.com"));
    message.setSubject("Test");
    GreenMailUtil.sendMimeMessage(message);
  }

  private ProcessInstance awaitProcessInstanceExists() throws InterruptedException {
    Thread.sleep(1000L);
    HistoricProcessInstanceQuery query =
        historyService().createHistoricProcessInstanceQuery().active();
    while (query.singleResult() == null) {
      Thread.sleep(1000L);
    }
    return runtimeService()
        .createProcessInstanceQuery()
        .processInstanceId(query.singleResult().getId())
        .singleResult();
  }

  private void awaitProcessInstanceComplete() throws InterruptedException {
    Thread.sleep(1000L);
    HistoricProcessInstanceQuery query =
        historyService().createHistoricProcessInstanceQuery().active();
    while (query.count() > 0) {
      Thread.sleep(1000L);
    }
  }
}
