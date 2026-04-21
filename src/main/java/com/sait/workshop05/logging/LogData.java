// Contributor(s): Robbie
// Main: Robbie - Structured logging and Sentry integration for staff actions.

package com.sait.workshop05.logging;

import com.sait.workshop05.models.Log;
import io.sentry.Sentry;
import io.sentry.SentryLevel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * File-backed audit lines plus Sentry forwarding for staff action and error paths.
 */
public class LogData {
    final static String fileName = "Log.txt";

    /**
     * Appends one audit line to Log.txt when user action and target are present.
     *
     * @param log row to append
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
     * Appends an error line to Log.txt and falls back to stderr on write failure.
     *
     * @param log row to append
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
     * Reports exceptions to Sentry and mirrors failure context into Log.txt.
     *
     * @param action short label for the failed operation
     * @param e      caught exception
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
     * Writes a structured action line for the current user.
     *
     * @param action verb such as CREATE or READ
     * @param target table or entity name
     */
    public static void logAction(String action, String target) {
        saveLog(new Log(action, target));
    }
}
