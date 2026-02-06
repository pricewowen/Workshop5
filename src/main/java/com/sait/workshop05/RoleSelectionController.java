package com.sait.workshop05;

import com.sait.workshop05.logging.LogData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the role selection screen
 */
public class RoleSelectionController {

    @FXML
    private Button employeeButton;

    @FXML
    private Button customerButton;

    @FXML
    private void initialize() {
        System.out.println("RoleSelectionController initialized");
    }

    /**
     * Handle employee/admin role selection
     */
    @FXML
    private void handleEmployeeSelection() {
        try {
            LogData.logAction("ROLE_SELECTION", "Employee/Admin role selected");
            openLoginView("EMPLOYEE");
        } catch (Exception e) {
            System.err.println("Error opening employee login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle customer role selection
     */
    @FXML
    private void handleCustomerSelection() {
        try {
            LogData.logAction("ROLE_SELECTION", "Customer role selected");
            openLoginView("CUSTOMER");
        } catch (Exception e) {
            System.err.println("Error opening customer login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Open the login view with the selected role
     */
    private void openLoginView(String role) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load());

        // Pass the selected role to the login controller
        LoginController loginController = loader.getController();
        loginController.setSelectedRole(role);

        // Get the current stage and set the new scene
        Stage stage = (Stage) employeeButton.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Peelin' Good - Login");
        stage.centerOnScreen();
    }
}

