# Camunda-platform-7-mail Plugin for Camunda Run

This plugin wraps the camunda-platform-7-mail community connector and is intended to be used with
camunda run.
The plugin configures the connectors for sending, polling, and deleting e-mails based on the YAML
used for configuring Camunda run, and registers the connectors upon startup.

## Install

This plugin can be used with Camunda 7 Run.

1. Add the `camunda-bpm-mail-extension-run-1.3.1.jar`to the `configuration/userlib`folder.

2. Configure the plugin.

## How to Use it?

For instructions on how to use the connectors from a service task,
see [the root project's readme](/README.md).

## How to Configure it?

Configure the plugin via a YAML file (i.e., the `default.yml`).
Preceed all properties with the prefix `camunda.bpm.plugin.mail`.

The `mail.` prefix that comes with the old bootstrapping strategy will be appended to remain compatible.

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