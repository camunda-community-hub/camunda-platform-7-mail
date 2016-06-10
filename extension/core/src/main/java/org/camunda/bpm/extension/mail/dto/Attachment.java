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
package org.camunda.bpm.extension.mail.dto;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Part;

public class Attachment implements Serializable {

  private static final long serialVersionUID = 1L;

  private String fileName;
  private String path;

  // transient object for download the attachment
  private transient Part part;

  public String getFileName() {
    return fileName;
  }

  public String getPath() {
    return path;
  }

  public Path download(Path downloadPath) throws MessagingException, IOException {
    Path newFile = downloadPath.resolve(fileName);

    DataHandler dataHandler = part.getDataHandler();
    Files.copy(dataHandler.getInputStream(), newFile);

    path = newFile.toAbsolutePath().toString();

    return newFile;
  }

  @Override
  public String toString() {
    return "Attachment [fileName=" + fileName + ", path=" + path + "]";
  }

  public static Attachment from(Part part) throws MessagingException {
    Attachment attachment = new Attachment();

    attachment.fileName = part.getFileName();

    attachment.part = part;

    return attachment;
  }

}
