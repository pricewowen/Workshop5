module com.sait.workshop05 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires jbcrypt;
    requires com.fasterxml.jackson.databind;
    requires sentry;

    opens com.sait.workshop05 to javafx.fxml;
    opens com.sait.workshop05.models to javafx.base;
    opens com.sait.workshop05.api.dto to com.fasterxml.jackson.databind;
    opens com.sait.workshop05.api to com.fasterxml.jackson.databind;

    exports com.sait.workshop05;
    exports com.sait.workshop05.models;
    exports com.sait.workshop05.controllers;
    opens com.sait.workshop05.controllers to javafx.fxml;
}