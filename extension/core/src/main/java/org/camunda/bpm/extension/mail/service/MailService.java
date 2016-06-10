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
package org.camunda.bpm.extension.mail.service;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;

import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

  private final MailConfiguration configuration;

  public MailService(MailConfiguration configuration) {
    this.configuration = configuration;
  }

  private static Session session = null;
  private static Store store = null;

  public Session getSession() {
    if (session == null) {
      LOGGER.debug("open session");

      Properties props = configuration.getProperties();
      session = Session.getInstance(props);
    }
    return session;
  }

  public Folder ensureOpenFolder(String folderName) throws MessagingException {
    if (store == null) {
      store = getSession().getStore();
    }

    ensureConnectedStore(store);
    Folder folder = store.getFolder(folderName);
    return ensureOpenFolder(folder);
  }

  public Folder ensureOpenFolder(Folder folder) throws MessagingException {
    ensureConnectedStore(folder.getStore());

    if (!folder.isOpen()) {
      openFolder(folder);
    }

    return folder;
  }

  private void ensureConnectedStore(Store store) throws MessagingException {
    if (!store.isConnected()) {
      LOGGER.debug("connect to sore");

      store.connect(configuration.getUserName(), configuration.getPassword());
    }
  }

  private void openFolder(Folder folder) throws MessagingException {

    LOGGER.debug("open folder '{}'", folder.getName());

    folder.open(Folder.READ_WRITE);

    if (!folder.isOpen()) {
      throw new IllegalStateException("folder is not open");
    }
  }

  public Transport getTransport() throws IOException, MessagingException {
    Transport transport = getSession().getTransport();
    if (!transport.isConnected()) {
      LOGGER.debug("connect transport");

      transport.connect(configuration.getUserName(), configuration.getPassword());
    }
    return transport;
  }

  public void close() throws Exception {
    if (store != null) {
      LOGGER.debug("close the store");

      store.close();
      store = null;
    }
  }

}
