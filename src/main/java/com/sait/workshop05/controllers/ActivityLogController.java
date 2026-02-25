package com.sait.workshop05.controllers;

import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Log;
import com.sait.workshop05.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ActivityLogController {

    @FXML
    private ListView<String> lstLogs;

    @FXML
    void initialize() {
        UserSession session = UserSession.getInstance();
        if(session.isAdmin()) {
            showLogs();
        } else if (session.isEmployee()){
            showEmpLogs();
        }
    }

    /**
     * Displays logs according to logged-in user
     */
    public void showEmpLogs() {
        UserSession session = UserSession.getInstance();
        String username = session.getCurrentUser().getUsername();

        ArrayList<String> listLogs = readEmpLogs(username);

        // reverse logs so newest are on top
        Collections.reverse(listLogs);

        // display the logs in the ListView
        for (String log : listLogs) {
            lstLogs.getItems().add(log);
        }
    }

    /**
     * Reads the logs from the Log.txt file only for USER=username
     * @param username of the USER to show logs for
     * @return an ArrayList of Strings containing all the logs for the specific Employee
     */
    private static ArrayList<String> readEmpLogs(String username) {
        // empty list
        ArrayList<String> logs = new ArrayList<String>();

        String line;
        String[] fields;
        Log tempLog;

        // read the logs from the text file
        try (BufferedReader in = new BufferedReader(new FileReader("Log.txt"))) {
            line = in.readLine();

            while (line != null) {

                // sort file by username
                if (line.contains("USER=" + username.toUpperCase())) {
                    // add the line
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
     * Displays all logs
     */
    public void showLogs() {
        ArrayList<String> listLogs = readLogs();

        // reverse logs so newest are on top
        Collections.reverse(listLogs);

        // display the logs in the ListView
        for (String log : listLogs) {
            lstLogs.getItems().add(log);
        }
    }

    /**
     * reads the logs from the Log.txt file
     * @return an ArrayList of Strings containing all the logs
     */
    public static ArrayList<String> readLogs() {
        // empty list
        ArrayList<String> logs = new ArrayList<String>();

        String line;
        String[] fields;
        Log tempLog;

        // read the logs from the text file
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
