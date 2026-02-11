package com.sait.workshop05.controllers;

import com.sait.workshop05.logging.LogData;
import com.sait.workshop05.models.Log;
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
        showLogs();
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
