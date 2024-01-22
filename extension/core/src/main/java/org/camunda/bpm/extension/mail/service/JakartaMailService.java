package org.camunda.bpm.extension.mail.service;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class JakartaMailService implements MailService {
  private final Session session;
  private final Queue<Store> availableStores = new ArrayBlockingQueue<>(100);
  private boolean usesLifecycle = false;

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

  private Store provideStore() throws MessagingException {
    Store store = availableStores.poll();
    if (store == null) {
      store = session.getStore();
    }
    if (!store.isConnected()) {
      store.connect();
    }
    return store;
  }

  private void returnStore(Store store) {
    if (usesLifecycle && availableStores.offer(store)) {
      return;
    } else {
      try {
        store.close();
      } catch (MessagingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public FolderWrapper getFolder(String folderName) throws MessagingException {
    Store store = provideStore();
    Folder folder = store.getFolder(folderName);
    folder.open(Folder.READ_WRITE);
    return new FolderWrapper(folder, () -> returnStore(store));
  }

  public void start() {
    usesLifecycle = true;
  }

  public void stop() {
    usesLifecycle = false;
    Store store = availableStores.poll();
    while (store != null) {
      try {
        store.close();
      } catch (MessagingException e) {
        // do not hinder stop()

      }
      store = availableStores.poll();
    }
  }

  public boolean isRunning() {
    return usesLifecycle;
  }
}
