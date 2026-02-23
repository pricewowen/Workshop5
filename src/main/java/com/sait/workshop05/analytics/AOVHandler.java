package com.sait.workshop05.analytics;

import com.sait.workshop05.database.AnalyticsDAO;

//η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

import java.time.LocalDate;
import java.util.List;

public class AOVHandler implements KPIHandler {

    private final AnalyticsDAO dao = new AnalyticsDAO();

    @Override
    public double getPrimaryValue(LocalDate start, LocalDate end, String bakery) throws Exception {
        return dao.getAverageOrderValue(start, end, bakery);
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start, LocalDate end, String bakery) throws Exception {
        return dao.getAverageOrderValueOverTime(start, end, bakery);
    }

    @Override
    public String getTitle() {
        return "Average Order Value";
    }
}
