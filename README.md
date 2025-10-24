# Camunda Platform 7 Mail Extension

<p align="center">
  <a href="https://github.com/camunda-community-hub/community">
    <img src="https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700" alt="Community Extension"/>
  </a>
  <a href="https://github.com/camunda-community-hub/community/blob/main/extension-lifecycle.md#compatiblilty">
    <img src="https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%207-26d07c" alt="Compatible with Camunda 7"/>
  </a>
  <a href="https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-">
    <img src="https://img.shields.io/badge/Lifecycle-Stable-brightgreen" alt="Stable"/>
  </a>
  <a href="https://opensource.org/licenses/Apache-2.0">
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License"/>
  </a>
  <img src="https://github.com/camunda-community-hub/camunda-bpm-mail/workflows/Build%20project%20with%20Maven/badge.svg" alt="Build with Maven"/>
</p>

<p align="center">
  <img src="docs/camunda-mail-architecture.png" alt="Camunda Mail Extension Architecture" width="600"/>
</p>

**Tags:** #Hacktoberfest #BeginnerFriendly #Camunda #Java

A community extension for **Camunda Platform 7** to integrate emails in a process and interact with them. Previously known as `camunda-bpm-mail`.  

---

## 📘 Getting Started

### Requirements
- Camunda Platform 7 >= 7.20.0  
- Java 17  

### Setup Options

| Engine Type               | Dependency / Action                                                                                   |
|----------------------------|------------------------------------------------------------------------------------------------------|
| Spring Boot               | Follow standard Spring Boot setup instructions                                                      |
| Embedded Process Engine    | Add to `pom.xml`: `<dependency><groupId>org.camunda.bpm.extension</groupId><artifactId>camunda-bpm-mail-core</artifactId><version>1.5.1</version></dependency>` |
| Shared Process Engine      | Add `camunda-bpm-mail-core-1.5.1.jar` to `lib` folder and include `camunda-connect-core`, `JakartaMail`, `Eclipse Angus Mail`, `slf4j-api` |

> ⚠️ If using **Wildfly**, follow [special instructions](docs/shared-process-engine-wildfly.md).

---

## 🔧 How to Use It

This extension is built on top of the [Connectors API](http://docs.camunda.org/manual/latest/reference/connect/) and provides connectors for interacting with emails inside a process.

```xml
<serviceTask id="sendMail" name="Send Mail Task">
  <extensionElements>
    <camunda:connector>
      <camunda:connectorId>mail-send</camunda:connectorId>
      <!-- input / output mapping -->
    </camunda:connector>
  </extensionElements>
</serviceTask>

```

See the [connectors user guide](http://docs.camunda.org/manual/latest/user-guide/process-engine/connectors/) how to configure the process engine to use connectors.

### 📤 Send Mails

![icon](docs/mail-send-icon.png)

Connector-Id: mail-send

| Input parameter | Type                                   | Required?             |
|-----------------|----------------------------------------|-----------------------|
| from            | String                                 | no (read from config) |
| fromAlias       | String                                 | no (read from config) |
| to              | String                                 | yes                   |
| cc              | String                                 | no                    |
| bcc             | String                                 | no                    |
| subject         | String                                 | yes                   |
| text            | String                                 | no                    |
| html            | String                                 | no                    |
| fileNames       | List of String (path to files)         | no                    |
| files           | Map of String to file process variable | no                    |

Text or HTML body can be generated from a template (e.g., FreeMarker). See the [example](examples/pizza#send-a-mail).

### 📥 Poll Mails

![icon](docs/mail-poll-icon.png)

Connector-Id: mail-poll

| Input parameter      | Type                  | Required?             |
|----------------------|-----------------------|-----------------------|
| folder               | String (e.g. 'INBOX') | no (read from config) |
| download-attachments | Boolean               | no (read from config) |

| Output parameter | Type                                                                                      |
|------------------|-------------------------------------------------------------------------------------------|
| mails            | List of [Mail](extension/core/src/main/java/org/camunda/bpm/extension/mail/dto/Mail.java) |

If `download-attachments` is set to `true` then it stores the attachments of the mails in the folder which is provided by the configuration. The path of the stored attachments can be gotten from the [Attachment](extension/core/src/main/java/org/camunda/bpm/extension/mail/dto/Attachment.java)s of the [Mail](extension/core/src/main/java/org/camunda/bpm/extension/mail/dto/Mail.java).

By default, the polled mails are marked as read. If the property `mail.imaps.peek` is set to `true` then the mails are just polled and not marked as read.

### 🗑️ Delete Mails

<details>
  <summary>🗑️ Delete Mails Connector</summary>

This connector allows you to delete emails from a folder. You can specify mails by object, message IDs, or message numbers.

![icon](docs/mail-delete-icon.png)

Connector-Id: mail-delete

| Input parameter | Type                  | Required?             |
|-----------------|-----------------------|-----------------------|
| folder          | String (e.g. 'INBOX') | no (read from config) |
| mails           | List of Mail          | no<sup>1</sup>        |
| messageIds      | List of String        | no<sup>1</sup>        |
| messageNumbers  | List of Integer       | no<sup>1</sup>        |

<sup>1</sup> Either `mails`, `messageIds` or `messageNumbers` have to be set.

</details>



### 📬 React on incoming Mails

![icon](docs/mail-notification-icon.png)

The extension provide the [MailNotificationService](extension/core/src/main/java/org/camunda/bpm/extension/mail/notification/MailNotificationService.java) to react on incoming mails (e.g. start a process instance or correlate a message). You can register handlers / consumers which are invoked when a new mail is received.

```java

MailNotificationService notificationService = new MailNotificationService(configuration);

notificationService.registerMailHandler(mail -> {
    runtimeService.startProcessInstanceByKey("process",
        Variables.createVariables().putValue("mail", mail));
});

notificationService.start();
notificationService.stop();
```



If you use a mail handler and enabled `downloadAttachments` in the configuration then it stores the attachments of the mail before invoking the handler. Otherwise, you can also trigger the download manual by calling [Mail.downloadAttachments()](extension/core/src/main/java/org/camunda/bpm/extension/mail/dto/Mail.java).

## ⚙️ How to configure it?

Configure using mail-config.properties or MAIL_CONFIG environment variable.

<details> 
  <summary>📥 Poll Mails Connector</summary>
![icon](docs/mail-poll-icon.png)

Connector-Id: mail-poll

| Input parameter      | Type                  | Required?             |
|----------------------|-----------------------|-----------------------|
| folder               | String (e.g. 'INBOX') | no (read from config) |
| download-attachments | Boolean               | no (read from config) |

| Output parameter | Type                                                                                      |
|------------------|-------------------------------------------------------------------------------------------|
| mails            | List of [Mail](extension/core/src/main/java/org/camunda/bpm/extension/mail/dto/Mail.java) |

</details>

You can find some sample configurations at [extension/core/configs](extension/core/configs). If you use a mail provider which has no configuration yet, feel free to add one. You can verify your configuration with the [integration tests](extension/core/src/test/java/org/camunda/bpm/extension/mail/integration/MailProviderIntegrationTest.java).

### Alternative Configuration

if you are running camunda in the environment that supports Mail Service and [Java Naming and Directory Interface (JNDI)](https://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface), you can configure mail session in the container and make it available through jndi. Provide the jndi-name of your bound mail session within properties file `mail-config.properties` like this:

```
mail.session.jndi.name=java:jboss/mail/MyMailSessionName
```

Please refer you container documentation to configure mail service.

If you do not need other properties then session configuration, you can even skip property file `mail-config.properties` and specify your mail session jndi-name directly via `MAIL_CONFIG` environment variable like this:`jndi:java:jboss/mail/MyMailSessionName`. _Ensure it starts with `jndi:`_

## 📝 Examples
These examples show how to use the mail connectors in your process. Beginners can follow these step-by-step.

The following examples shows how to use the connectors and services.

* [Pizza Order](examples/pizza)
  * poll mails
  * send mail with generated text body
  * delete mail
* [Print Service](examples/print-service)
  * using the MailNotificationService
  * send mail with attachment

## Setting up configuration using HELM

The mail connector cannot directly support Helm values files since it cannot assume the deployment environment is Kubernetes.

This is why it is configured via the MAIL_CONFIG environment variable and a properties file.

However, supporting Helm deployment is easily done by following:
1. Accept mail configuration in Values.yaml:
    mail:
       smtp:
           auth: true
           port: 465

2. Render mail.properties with Helm template.

3. Mount it as ConfigMap in /config/mail.properties.

4. Set MAIL_CONFIG=file:/config/mail.properties.

## Next Steps

Depends on the input of the community. Some ideas:

* provide element templates for camunda modeler (not supported yet)
* integration of file process variables
* spring-based configuration

## Contribution

### Hacktoberfest Friendly
We welcome contributions from beginners! Look for issues labeled `hacktoberfest` or `good first issue`.  
You can improve docs, add examples, or fix small bugs. Every contribution counts! 🎉

Found a bug? Please report it using [GitHub Issues](https://github.com/camunda/camunda-platform-7-mail/issues).

Want to extend, improve or fix a bug in the extension? [Pull Requests](https://github.com/camunda/camunda-platform-7-mail/pulls) are very welcome.

Want to discuss something? The [Camunda Forum](https://forum.camunda.io/c/community-extensions) might be the best place for it.

## FAQ

See also

* [JavaMail Project Documentation/FAQ](https://java.net/projects/javamail/pages/Home)
* [Oracle JavaMail FAQ](http://www.oracle.com/technetwork/java/faq-135477.html)

### Can't send / receive mails from Gmail

It can be that Google blocks the requests because it estimates your application as unsafe. You may also receive an email from Google. To fix this go to https://www.google.com/settings/security/lesssecureapps and enable less secure apps.

## License

[Apache License, Version 2.0](./LICENSE)
