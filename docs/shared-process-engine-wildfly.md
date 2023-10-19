# Install the Extension for a Shared Process Engine on Wildfly

Tested with Camunda Version 7.17 running on WildFly Full 26.0.1.Final

1. Create a module for camunda-bpm-mail-core:

    Copy the jar into `\server\wildfly-26.0.1.Final\modules\org\camunda\bpm\extension\camunda-bpm-mail-core\main` and add a `module.xml` with following content:
  
    ```
    <module xmlns="urn:jboss:module:1.0" name="org.camunda.bpm.extension.camunda-bpm-mail-core">
      <resources>
        <resource-root path="camunda-bpm-mail-core-${VERSION}.jar" />
      </resources>
  
      <dependencies>
        <module name="javax.mail.api" />
        <module name="org.slf4j.slf4j-api" />
      
        <module name="org.camunda.connect.camunda-connect-core" />
      </dependencies>
    </module>
    ```

2. Create a module for slf4j.api:

    Add a `module.xml` with the following content into `\server\wildfly-26.0.1.Final\modules\org\slf4j\slf4j-api\main`:
  
    ```
    <module xmlns="urn:jboss:module:1.0" name="org.slf4j.slf4j-api">
      <resources>
        <resource-root path="slf4j-api-1.7.26.jar" />
      </resources>
    </module>
    ```

3. Import the mail module in the connect-plugin module:

    Change the `module.xml` in `\server\wildfly-26.0.1.Final\modules\org\camunda\bpm\camunda-engine-plugin-connect\main` and add the line
  
    ```
    <module name="org.camunda.bpm.extension.camunda-bpm-mail-core" services="import" />
    ```
4. Import the mail module in the camunda-engine module:

    Change the `module.xml` in `\server\wildfly-26.0.1.Final\modules\org\camunda\bpm\camunda-engine\main` and add the line
  
    ```
    <module name="org.camunda.bpm.extension.camunda-bpm-mail-core" services="import" />
    ```
  
5. An easy way to configure the connection is to copy the `mail-config.properties` into the `\server\wildfly26.0.1.Final\standalone\config` folder and add an environment variable `MAIL_CONFIG` that points to the file. 
Alternative you can configure the Wildfly `Mail Subsystem` and mail session and acquire it over jndi.  Have a look at the configuration section for further details.
