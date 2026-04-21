// Contributor(s): Robbie
// Main: Robbie - Activity log view for staff actions.

package com.sait.workshop05.controllers;

import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Log;
import com.sait.workshop05.session.UserSession;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Activity log lines from the server log file with admin versus employee filtering.
 */
public class ActivityLogController {

    @FXML
    private ListView<String> lstLogs;

    @FXML
    void initialize() {
        if (lstLogs != null) {
            lstLogs.setPlaceholder(new Label("Loading activity log…"));
            lstLogs.setItems(FXCollections.observableArrayList());
        }
        UserSession session = UserSession.getInstance();
        if (session.isAdmin()) {
            loadLogsAsync(true, null);
        } else if (session.isEmployee()) {
            String username = session.getCurrentUser() != null
                    ? session.getCurrentUser().getUsername()
                    : "";
            loadLogsAsync(false, username);
        }
    }

    private void loadLogsAsync(boolean admin, String employeeUsername) {
        Task<ArrayList<String>> task = new Task<>() {
            @Override
            protected ArrayList<String> call() {
                ArrayList<String> lines = admin
                        ? readLogs()
                        : readEmpLogs(employeeUsername);
                Collections.reverse(lines);
                return lines;
            }
        };
        task.setOnSucceeded(e -> {
            if (lstLogs != null) {
                lstLogs.getItems().setAll(task.getValue());
                lstLogs.setPlaceholder(new Label("No log entries to show."));
            }
        });
        task.setOnFailed(e -> {
            if (lstLogs != null) {
                lstLogs.getItems().clear();
                lstLogs.setPlaceholder(new Label("Could not read activity log."));
            }
            Throwable t = task.getException();
            Exception wrapped = t instanceof Exception cause ? cause : new Exception("log load failed", t);
            LogData.handleException("GET_LOGS", wrapped);
        });
        Thread.ofVirtual().name("activity-log-read").start(task);
    }

    /**
     * Returns only log lines for the selected employee username.
     */
    private static ArrayList<String> readEmpLogs(String username) {
        // Start empty so file read failures still return a safe result.
        ArrayList<String> logs = new ArrayList<String>();

        String line;
        String[] fields;
        Log tempLog;

        // Read from newest persisted desktop activity log file.
        try (BufferedReader in = new BufferedReader(new FileReader("Log.txt"))) {
            line = in.readLine();

            while (line != null) {

                // Filter lines to the active employee marker used in log entries.
                if (line.contains("USER=" + username.toUpperCase())) {
                    // Keep the full raw entry so UI preserves original context.
                    logs.add(line);
                }
                line = in.readLine();
            }
        } catch (IOException e) {
            LogData.handleException("GET_LOGS", e);
        }

        return logs;
    }

    /**
     * Returns all activity log lines for admin views.
     */
    public static ArrayList<String> readLogs() {
        // Start empty so read errors degrade to an empty log view.
        ArrayList<String> logs = new ArrayList<String>();

        String line;
        String[] fields;
        Log tempLog;

        // Read the full activity file without employee level filtering.
        try (BufferedReader in = new BufferedReader(new FileReader("Log.txt"))) {
            line = in.readLine();

            while (line != null) {
                logs.add(line);
                line = in.readLine();
            }
        } catch (IOException e) {
            LogData.handleException("GET_LOGS", e);
        }

        return logs;
    }
}
