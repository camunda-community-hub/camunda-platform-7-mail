package org.camunda.bpm.extension.mail.notification;

import jakarta.mail.Folder;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractNotificationWorker<T extends Folder> implements NotificationWorker {
  private final AtomicBoolean running = new AtomicBoolean(false);
  protected T folder;

  @Override
  public final void stop() {
    running.set(false);
    interrupt();
  }

  protected abstract void interrupt();

  @Override
  public final void accept(Folder folder) {
    this.folder = (T) folder;
    running.set(true);
    while (running.get()) {
      idle();
    }
  }

  protected abstract void idle();

  protected boolean isRunning() {
    return running.get();
  }
}
