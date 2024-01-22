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

import jakarta.mail.Flags;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.search.FlagTerm;
import java.util.Arrays;
import java.util.List;
import org.camunda.bpm.extension.mail.service.MailService;
import org.camunda.connect.impl.AbstractRequestInvocation;
import org.camunda.connect.spi.ConnectorRequestInterceptor;

public class PollMailInvocation extends AbstractRequestInvocation<Folder> {

  protected final MailService mailService;

  public PollMailInvocation(
      Folder folder,
      PollMailRequest request,
      List<ConnectorRequestInterceptor> requestInterceptors,
      MailService mailService) {
    super(folder, request, requestInterceptors);

    this.mailService = mailService;
  }

  @Override
  public Object invokeTarget() throws Exception {
    // poll only messages which are not deleted
    Message[] messages = target.search(new FlagTerm(new Flags(Flag.DELETED), false));

    return Arrays.asList(messages);
  }
}
