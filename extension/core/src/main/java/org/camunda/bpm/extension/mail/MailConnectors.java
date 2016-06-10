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

import org.camunda.bpm.extension.mail.delete.DeleteMailConnector;
import org.camunda.bpm.extension.mail.poll.PollMailConnector;
import org.camunda.bpm.extension.mail.send.SendMailConnector;
import org.camunda.connect.Connectors;

public class MailConnectors {

  public static SendMailConnector sendMail() {
    return Connectors.getConnector(SendMailConnector.CONNECTOR_ID);
  }

  public static PollMailConnector pollMails() {
    return Connectors.getConnector(PollMailConnector.CONNECTOR_ID);
  }

  public static DeleteMailConnector deleteMails() {
    return Connectors.getConnector(DeleteMailConnector.CONNECTOR_ID);
  }

}
