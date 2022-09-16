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
package org.camunda.bpm.extension.mail.poll;

import org.camunda.connect.spi.Connector;
import org.camunda.connect.spi.ConnectorProvider;

public class PollMailProvider implements ConnectorProvider {

  @Override
  public String getConnectorId() {
    return PollMailConnector.CONNECTOR_ID;
  }

  @Override
  public Connector<?> createConnectorInstance() {
    return new PollMailConnector();
  }
}
