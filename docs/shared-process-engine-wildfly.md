# Install the Extension for a Shared Process Engine on Wildfly

1. Create a module for camunda-bpm-mail-core:

  Copy the jar into `\server\wildfly-10.0.0.Final\modules\org\camunda\bpm\extension\camunda-bpm-mail-core\main` and add a `module.xml` with following content:
  
  ```
  <module xmlns="urn:jboss:module:1.0" name="org.camunda.bpm.extension.camunda-bpm-mail-core">
    <resources>
      <resource-root path="camunda-bpm-mail-core-1.0.0.jar" />
    </resources>
  
    <dependencies>
      <module name="javax.mail.api" />
      <module name="org.slf4j.api" />
      
      <module name="org.camunda.bpm.camunda-engine" />
      <module name="org.camunda.connect.camunda-connect-core" />
    </dependencies>
  </module>
  ```

2. Create a module for slf4j.api:

  Copy the jar into `\server\wildfly-10.0.0.Final\modules\org\slf4j\api\main` and add a `module.xml` with the following content:
  
  ```
  <module xmlns="urn:jboss:module:1.0" name="org.slf4j.api">
    <resources>
      <resource-root path="slf4j-api-1.7.21.jar" />
    </resources>
  </module>
  ```

3. Import the mail module in the connect-plugin:

  Change the `module.xml` in `\server\wildfly-10.0.0.Final\modules\org\camunda\bpm\camunda-engine-plugin-connect\main` and add the line
  
  ```
  <module name="org.camunda.bpm.extension.camunda-bpm-mail-core" services="import" />
  ```
