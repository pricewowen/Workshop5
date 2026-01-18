module com.sait.workshop05 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.sait.workshop05 to javafx.fxml;
    exports com.sait.workshop05;
}