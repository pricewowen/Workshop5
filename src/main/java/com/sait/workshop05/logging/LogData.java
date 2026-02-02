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
        // get current date and time
        LocalDateTime currentTime = LocalDateTime.now();
        String currentTimeString = (currentTime.getYear() + "-" + currentTime.getMonthValue() + "-"
                + currentTime.getDayOfMonth() + "|" + currentTime.getHour() + ":" + currentTime.getMinute()
                + ":" + currentTime.getSecond());

        if (log.getUser() != null && log.getAction() != null && log.getDescription() != null) {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))){
                String line = ("USER="+ log.getUser() + " | " + log.getAction() + "=" + log.getDescription() + " | " + currentTimeString);
                out.append(line);
                out.println();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
