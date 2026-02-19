package com.sait.workshop05.analytics;

import com.sait.workshop05.database.AnalyticsDAO;

import java.time.LocalDate;
import java.util.List;

public class RevenueOverTimeHandler implements KPIHandler {

    private final AnalyticsDAO dao = new AnalyticsDAO();

    @Override
    public String getTitle() {
        return "Revenue Over Time";
    }

    @Override
    public double getPrimaryValue(LocalDate start, LocalDate end, String bakery) {
        return dao.getTotalRevenue(start, end, bakery);
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start, LocalDate end, String bakery) {
        return dao.getRevenueOverTime(start, end, bakery);
    }
}
