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
package org.camunda.bpm.extension.mail;

import java.io.File;
import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailTestUtil {

  public static MimeMessage createMimeMessage(Session session) throws MessagingException, AddressException {
    MimeMessage message = new MimeMessage(session);

    message.setFrom(new InternetAddress("from@camunda.com"));
    message.addRecipient(Message.RecipientType.TO, new InternetAddress("test@camunda.com"));
    message.setSubject("subject");

    return message;
  }

  public static MimeMessage createMimeMessageWithHtml(Session session) throws MessagingException, AddressException {
    MimeMessage message = createMimeMessage(session);

    Multipart multiPart = new MimeMultipart();

    MimeBodyPart textPart = new MimeBodyPart();
    textPart.setText("text");
    multiPart.addBodyPart(textPart);

    MimeBodyPart htmlPart = new MimeBodyPart();
    htmlPart.setContent("<b>html</b>", MailContentType.TEXT_HTML.getType());
    multiPart.addBodyPart(htmlPart);

    message.setContent(multiPart);
    return message;
  }

  public static MimeMessage createMimeMessageWithAttachment(Session session, File attachment) throws MessagingException, AddressException, IOException {
    MimeMessage message = createMimeMessage(session);

    Multipart multiPart = new MimeMultipart();

    MimeBodyPart textPart = new MimeBodyPart();
    textPart.setText("text");
    multiPart.addBodyPart(textPart);

    MimeBodyPart filePart = new MimeBodyPart();
    filePart.attachFile(attachment);
    multiPart.addBodyPart(filePart);

    message.setContent(multiPart);

    return message;
  }

}
