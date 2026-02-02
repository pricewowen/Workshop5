module com.sait.workshop05 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.sait.workshop05 to javafx.fxml;
    exports com.sait.workshop05;
}