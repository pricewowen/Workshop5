package com.sait.workshop05.logging;

import com.sait.workshop05.models.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class LogData {
    final static String fileName = "Log.txt";

    /**
     * saves a log in the Log.txt file
     * @param log the log to be saved
     */
    public static void saveLog(Log log) {
        if (log.getUser() != null && log.getAction() != null && log.getDescription() != null) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))){
                String line = ("USER="+ log.getUser() + " | " + log.getAction() + "=" + log.getDescription() + " | " + log.getCurrentDate());
                out.append(line);
                out.println();
            } catch (IOException e) {
                logError(new Log(log.getUser(), "error", e.getMessage()));
            }
        }
    }

    /**
     * Logs errors in the Log.txt file
     * @param log the log to be saved
     */
    public static void logError(Log log) {
        if (log.getUser() != null && log.getAction() != null && log.getDescription() != null) {
            try (PrintWriter out =  new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {
                String line = ("USER=" + log.getUser() + " | " + log.getAction() + "_FAILED=" + log.getDescription() + " | " + log.getCurrentDate());
                out.append(line);
                out.println();
            } catch (IOException e) {
                System.err.println("ERROR: Could not write to log file");
                System.err.println("USER=" + log.getUser() + " | " + log.getAction() + "_FAILED=" + log.getDescription() + " | " + log.getCurrentDate());
                e.printStackTrace();
            }
        }
    }
}
