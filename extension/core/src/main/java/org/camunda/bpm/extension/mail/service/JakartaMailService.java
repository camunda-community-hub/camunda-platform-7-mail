package org.camunda.bpm.extension.mail.service;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class JakartaMailService implements MailService {
  private final Session session;

  public JakartaMailService(Session session) {
    this.session = session;
  }

  @Override
  public Message createMessage() {
    return new MimeMessage(session);
  }

  @Override
  public void sendMessage(Message message) throws MessagingException {
    try (Transport transport = session.getTransport()) {
      transport.connect();
      transport.sendMessage(message, message.getAllRecipients());
    }
  }

  public Store getStore() throws MessagingException {
    Store store = session.getStore();
    store.connect();
    return store;
  }

  @Override
  public Folder getFolder(Store store, String folderName) throws MessagingException {
    Folder folder = store.getFolder(folderName);
    folder.open(Folder.READ_WRITE);
    return folder;
  }
}
