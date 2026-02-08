module com.sait.workshop05 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens com.sait.workshop05 to javafx.fxml;
    opens com.sait.workshop05.models to javafx.base;

    exports com.sait.workshop05;
    exports com.sait.workshop05.models;
    exports com.sait.workshop05.controllers;
    opens com.sait.workshop05.controllers to javafx.fxml;
}