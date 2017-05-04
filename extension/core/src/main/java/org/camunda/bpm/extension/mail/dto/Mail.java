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
package org.camunda.bpm.extension.mail.dto;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.extension.mail.MailContentType;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mail implements Serializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Mail.class);

  private static final long serialVersionUID = 1L;

  private String from;
  private String to;
  private String cc;

  private String subject;
  private Date sentDate;
  private Date receivedDate;

  private int messageNumber;
  private String messageId;

  private String text;
  private String html;

  private List<Attachment> attachments = new ArrayList<Attachment>();

  public String getFrom() {
    return from;
  }

  public String getSubject() {
    return subject;
  }

  public String getText() {
    return text;
  }

  public String getHtml() {
    return html;
  }

  public String getTo() {
    return to;
  }

  public String getCc() {
    return cc;
  }

  public Date getSentDate() {
    return sentDate;
  }

  public Date getReceivedDate() {
    return receivedDate;
  }

  public int getMessageNumber() {
    return messageNumber;
  }

  public String getMessageId() {
    return messageId;
  }

  public List<Attachment> getAttachments() {
    return attachments;
  }

  public static Mail from(Message message) throws MessagingException, IOException {
    Mail mail = new Mail();

    mail.from = InternetAddress.toString(message.getFrom());
    mail.to =  InternetAddress.toString(message.getRecipients(RecipientType.TO));
    mail.cc = InternetAddress.toString(message.getRecipients(RecipientType.CC));

    mail.subject = message.getSubject();
    mail.sentDate = message.getSentDate();
    mail.receivedDate = message.getReceivedDate();

    mail.messageNumber = message.getMessageNumber();

    if (message instanceof MimeMessage) {
      MimeMessage mimeMessage = (MimeMessage) message;
      // extract more informations
      mail.messageId = mimeMessage.getMessageID();
    }

    processMessageContent(message, mail);

    return mail;
  }

  protected static void processMessageContent(Message message, Mail mail) throws MessagingException, IOException {

    if (isMultipartMessage(message)) {
      Multipart multipart = (Multipart) message.getContent();

      int numberOfParts = multipart.getCount();
      for (int partCount = 0; partCount < numberOfParts; partCount++) {
        BodyPart bodyPart = multipart.getBodyPart(partCount);

        processMessagePartContent(bodyPart, mail);
      }

    } else {
      processMessagePartContent(message, mail);
    }
  }

  protected static boolean isMultipartMessage(Message message) throws MessagingException, IOException {
    return message.isMimeType("multipart")
        || message.getContent() instanceof Multipart;
  }

  protected static void processMessagePartContent(Part part, Mail mail) throws MessagingException, IOException {

    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {

      Attachment attachment = Attachment.from(part);
      mail.attachments.add(attachment);

    } else {

      if (part.isMimeType("text/plain")) {
        mail.text = (String) part.getContent();

      } else if (part.isMimeType("text/html")) {
        mail.html = (String) part.getContent();
      }
    }
  }

  public void downloadAttachments() throws IOException, MessagingException {
    if (!attachments.isEmpty()) {

      LOGGER.debug("download attachments of mail: {}", this);

      // use an unique folder for each mail
      String uuid = UUID.randomUUID().toString();

      MailConfiguration configuration = MailConfigurationFactory.getConfiguration();
      Path downloadPath = Paths.get(configuration.getAttachmentPath(), uuid);
      Files.createDirectories(downloadPath);

      for (Attachment attachment : attachments) {
        attachment.download(downloadPath);

        LOGGER.debug("downloaded '{}'", attachment);
      }
    }
  }

  @Override
  public String toString() {
    return "Mail [from=" + from + ", to=" + to + ", cc=" + cc + ", subject=" + subject + ", sentDate=" + sentDate + ", receivedDate=" + receivedDate
        + ", messageNumber=" + messageNumber + ", messageId=" + messageId + ", attachments=" + attachments + " ]";
  }

}
