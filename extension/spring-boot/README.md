# Camunda-platform-7-mail Starter for Spring Boot

This starter wraps the camunda-platform-7-mail community connector and is intended to be used with camunda spring-boot.
The plugin configures the connectors for sending, polling, and deleting e-mails based on the YAML/Properties file used for configuring your spring boot app, and registers the connectors upon startup.

## Install

This plugin can be used with Camunda 7 Spring Boot Starter.

1. Add the dependency:
   ```xml
   <dependency>
     <groupId>org.camunda.bpm.extension</groupId>
     <artifactId>camunda-bpm-mail-spring-boot-starter</artifactId>
     <version>${version.camunda-bpm-mail}</version>
   </dependency>
   ```

2. Configure the connector.

## How to Use it?

For instructions on how to use the connectors from a service task,
see [the root project's readme](/README.md).

To use the notification service, please register Beans of type `Consumer<Mail>` or `MessageHandler` which will automatically be picked up and registered to the notification service.

## How to Configure it?

### General Configuration

Configure the plugin via a YAML/Properties file (i.e., the `application.yml`).
Precede all properties with the prefix `camunda.bpm.plugin.mail`.

The `mail.` prefix that comes with the old bootstrapping strategy will be appended to remain compatible with Jakarta Mail.

### Special configuration properties

To not use the notification service, please set `camunda.bpm.plugin.mail.notification.enabled=false`. By default, the notification service will be active.

### Example

An Example configuration can look like this

```yml
camunda.bpm.plugin.mail:
  # send mails via SMTP
  transport.protocol: smtp

  smtp:
    host: smtp.gcom
    port: 465
    auth: true
    ssl.enable: true
    socketFactory:
      port: 465
      class: javax.net.ssl.SSLSocketFactory

  # poll mails via IMAPS
  store.protocol: imaps

  imaps:
    host: imap.gcom
    port: 993
    timeout: 10000
    # if peek :   false then the polled mails are marked as read
    peek: false

  # additional config
  poll.folder: INBOX
  sender: USER@google.com
  sender.alias: User Inc

  attachment:
    download: true
    path: attachments

  # credentials
  user: USER@gcom
  password: PASSWORD
```
