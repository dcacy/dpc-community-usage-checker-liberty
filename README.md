# dpc-connections-user-checker-liberty

Instructions:

This sample application lists all the Communities which can be viewed by the ID you provide, and also shows
various metrics for each Community.

View demo at <a target="top" href="https://dpc-community-usage-checker.mybluemix.net">https://dpc-community-usage-checker.mybluemix.net</a>

## Getting started

1. Copy the file `WebContent/WEB-INF/connections-sample.properties` to `WebContent/WEB-INF/connections.properties`

1. Edit the file to provide your Connections server's host name, and an ID and password.

1. Download `date.format.js` from [https://gist.github.com/jhbsk/4690754](https://gist.github.com/jhbsk/4690754) and copy it to the `WebContent/js` directory.

1. Download `jquery.loadmask.min.js` from [https://github.com/wallynm/jquery-loadmask](https://github.com/wallynm/jquery-loadmask) and copy it to the `WebContent/js` directory.

1. Download the following files and copy them to the `WebContent/WEB-INF/lib` directory

  ```none
  commons-codec-1.6.jar
  commons-logging-1.1.3.jar
  fluent-hc-4.3.4.jar
  httpclient-4.3.4.jar
  httpcore-4.3.2.jar
  ```

6. Deploy the application to a Java server and away you go!

NOTE: Please read the LICENSE file for disclaimers and copyright!
