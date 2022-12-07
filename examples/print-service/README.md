# camunda-bpm-mail - example - print service

This example demonstrate how to

* use the `MailNotificationService` to get informed while receive a new mail,
* send a mail with an attached file

![Process](docs/printProcess.png)

## How to run it

1. Build the executable JAR using Maven `mvn clean install`. If you want to adjust the predefined configuration, use `src/main/resources/application.yaml` before you build.
2. Run the application using `java -jar target/camunda-bpm-mail-example-print-service-<PROJECT_VERSION>.jar`. To use an external configuration, please find instructions [in the guide](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/html/boot-features-external-config.html#boot-features-external-config).
3. Send a mail with the attached file which should be printed
4. Check that a user task is created - complete it
5. Now, check your mails

Also try to send a mail without attachment.

### Run it as JUnit test

You can also use the [ProcessIntegrationTest](src/test/java/org/camunda/bpm/extension/mail/example/ProcessIntegrationTest.java) to run an example using a Spring Boot Test and a mocked mail server.

## How it works

### Using the MailNotificationService

Spring Boot automatically instantiates the NotificationService as described [here](./../../extension/spring-boot/README.md).

For this application, it is enough to just register a Bean of type `Consumer<Mail>` [here](./src/main/java/org/camunda/bpm/extension/mail/example/PrintServiceProcessApplication.java).

### Send a Mail with Attachment

When the order is processed (i.e. complete user task 'print it') then the process sends a mail with the attached invoice (`invoice.pdf`) using the `mail-send` connector. The path of the invoice is stored in the process variable `invoice`.

```xml

<bpmn:serviceTask id="ServiceTask_1ry54cw" name="send invoice">
  <bpmn:extensionElements>
    <camunda:connector>
      <camunda:inputOutput>
        <camunda:inputParameter name="to">${mail.getFrom()}</camunda:inputParameter>
        <camunda:inputParameter name="subject">invoice</camunda:inputParameter>
        <camunda:inputParameter name="fileNames">
          <camunda:list>
            <camunda:value>${invoice}</camunda:value>
          </camunda:list>
        </camunda:inputParameter>
      </camunda:inputOutput>
      <camunda:connectorId>mail-send</camunda:connectorId>
    </camunda:connector>
  </bpmn:extensionElements>
  <!-- ... -->
</bpmn:serviceTask>
```
