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
package org.camunda.bpm.extension.mail.delete;

import java.util.List;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.connect.impl.AbstractRequestInvocation;
import org.camunda.connect.spi.ConnectorRequestInterceptor;

public class DeleteMailInvocation extends AbstractRequestInvocation<List<Message>> {

  protected final MailService mailService;

  public DeleteMailInvocation(
      List<Message> messages,
      DeleteMailRequest request,
      List<ConnectorRequestInterceptor> requestInterceptors,
      MailService mailService) {
    super(messages, request, requestInterceptors);
    this.mailService = mailService;
  }

  @Override
  public Object invokeTarget() throws Exception {

    for (Message message : target) {
      message.setFlag(Flag.DELETED, true);
    }

    return null;
  }
}
