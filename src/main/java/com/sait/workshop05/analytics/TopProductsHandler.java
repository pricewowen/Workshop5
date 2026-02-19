package com.sait.workshop05.analytics;

import com.sait.workshop05.database.AnalyticsDAO;

import java.time.LocalDate;
import java.util.List;

public class TopProductsHandler implements KPIHandler {

    private final AnalyticsDAO dao = new AnalyticsDAO();

    @Override
    public double getPrimaryValue(LocalDate start, LocalDate end, String bakery) throws Exception {
        // Not meaningful for "Top Products", so return 0
        return 0;
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start, LocalDate end, String bakery) throws Exception {
        return dao.getTopProducts(start, end, bakery);
    }

    @Override
    public String getTitle() {
        return "Top Products";
    }
}
