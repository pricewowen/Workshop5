package com.sait.workshop05.logging;

import com.sait.workshop05.models.Log;
import io.sentry.Sentry;
import io.sentry.SentryLevel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogData {
    final static String fileName = "Log.txt";

    /**
     * saves a log in the Log.txt file
     * @param log the log to be saved
     */
    public static void saveLog(Log log) {
        if (log.getUser() != null && log.getAction() != null && log.getTarget() != null) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))){
                String line = log.getCurrentDate() + " | USER=" + log.getUser() + " | ACTION=" + log.getAction()
                        + " | TARGET=" + log.getTarget();
                out.append(line);
                out.println();
            } catch (IOException e) {
                logError(new Log("error", e.getMessage()));
            }
        }
    }

    /**
     * Logs errors in the Log.txt file
     * @param log the log to be saved
     */
    public static void logError(Log log) {
        if (log.getUser() != null && log.getAction() != null && log.getTarget() != null) {
            try (PrintWriter out =  new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {
                String line = log.getCurrentDate() + " | USER=" + log.getUser() + " | ACTION=" + log.getAction()
                        + "_FAILED | TARGET=" + log.getTarget();
                out.append(line);
                out.println();
            } catch (IOException e) {
                System.err.println("ERROR: Could not write to log file");
                System.err.println(log.getCurrentDate() + " | USER=" + log.getUser() + " | ACTION=" + log.getAction()
                        + "_FAILED | TARGET=" + log.getTarget());
                e.printStackTrace();
                Sentry.withScope(scope -> {
                    scope.setLevel(SentryLevel.ERROR);
                    scope.setTag("action", log.getAction());
                    Sentry.captureException(e);
                });
            }
        }
    }

    /**
     * Log error messages to the log file
//     * @param user User logged in
     * @param action action being attempted
     * @param e error message returned
     */
    public static void handleException(String action, Exception e) {
        Sentry.withScope(scope -> {
            scope.setLevel(SentryLevel.ERROR);
            scope.setTag("action", action);
            Sentry.captureException(e);
        });
        logError(new Log(action, e.getMessage() != null ? e.getMessage() : "no detail"));
    }

    /**
     * Save an action to the log file
//     * @param user the user logged in
     * @param action the action attempted
     * @param target the table/entity being affected
     */
    public static void logAction(String action, String target) {
        saveLog(new Log(action, target));
    }
}
