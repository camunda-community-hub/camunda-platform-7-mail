package org.camunda.bpm.extension.mail.service;

import javax.mail.Folder;
import javax.mail.MessagingException;

public class FolderWrapper implements AutoCloseable {
  private final Folder folder;
  private final Runnable onClose;

  public FolderWrapper(Folder folder, Runnable onClose) {
    this.folder = folder;
    this.onClose = onClose;
  }

  public Folder getFolder() {
    return folder;
  }

  @Override
  public void close() throws MessagingException {
    if (folder.isOpen()) {
      folder.close();
    }
    onClose.run();
  }
}
