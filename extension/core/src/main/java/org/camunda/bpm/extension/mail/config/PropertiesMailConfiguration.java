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
package org.camunda.bpm.extension.mail.config;

import java.time.Duration;

public class PropertiesMailConfiguration implements MailConfiguration {
  private String pollFolder;
  private String sender;
  private String senderAlias;
  private boolean downloadAttachments;
  private String attachmentPath;
  private Duration notificationLookupTime;

  @Override
  public String getPollFolder() {
    return pollFolder;
  }

  public void setPollFolder(String pollFolder) {
    this.pollFolder = pollFolder;
  }

  @Override
  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  @Override
  public String getSenderAlias() {
    return senderAlias;
  }

  public void setSenderAlias(String senderAlias) {
    this.senderAlias = senderAlias;
  }

  @Override
  public boolean isDownloadAttachments() {
    return downloadAttachments;
  }

  public void setDownloadAttachments(boolean downloadAttachments) {
    this.downloadAttachments = downloadAttachments;
  }

  @Override
  public String getAttachmentPath() {
    return attachmentPath;
  }

  public void setAttachmentPath(String attachmentPath) {
    this.attachmentPath = attachmentPath;
  }

  @Override
  public Duration getNotificationLookupTime() {
    return notificationLookupTime;
  }

  public void setNotificationLookupTime(Duration notificationLookupTime) {
    this.notificationLookupTime = notificationLookupTime;
  }
}
