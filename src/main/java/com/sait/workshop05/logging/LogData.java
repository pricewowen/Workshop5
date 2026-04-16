package com.sait.workshop05.logging;

import com.sait.workshop05.models.Log;
import io.sentry.Sentry;
import io.sentry.SentryLevel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogData {

    private static final String fileName = "Log.txt";

    public static void saveLog(Log log) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {

            String line = log.getCurrentDate()
                    + " | ACTION=" + log.getAction()
                    + " | TARGET=" + log.getTarget();

            out.println(line);

        } catch (IOException e) {
            System.err.println("ERROR: Could not write to log file");
            e.printStackTrace();
        }
    }

    public static void logError(Log log) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {

            String line = log.getCurrentDate()
                    + " | ACTION=" + log.getAction() + "_FAILED"
                    + " | TARGET=" + log.getTarget();

            out.println(line);

        } catch (IOException e) {
            System.err.println("ERROR: Could not write to log file");
            e.printStackTrace();

            Sentry.withScope(scope -> {
                scope.setLevel(SentryLevel.ERROR);
                scope.setTag("action", log.getAction());
                Sentry.captureException(e);
            });
        }
    }

    public static void handleException(String action, Exception e) {
        Sentry.withScope(scope -> {
            scope.setLevel(SentryLevel.ERROR);
            scope.setTag("action", action);
            Sentry.captureException(e);
        });

        logError(new Log(action, e.getMessage() != null ? e.getMessage() : "no detail"));
    }

    public static void logAction(String action, String target) {
        saveLog(new Log(action, target));
    }
}