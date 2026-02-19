package com.sait.workshop05.analytics;

import com.sait.workshop05.database.AnalyticsDAO;

import java.time.LocalDate;
import java.util.List;

public class TopProductsHandler implements KPIHandler {

    private final AnalyticsDAO dao = new AnalyticsDAO();

    @Override
    public double getPrimaryValue(LocalDate start,
                                  LocalDate end,
                                  String bakery) throws Exception {

        List<DataPoint> data = dao.getTopProducts(start, end, bakery);

        if (data.isEmpty()) {
            return 0.0;
        }

        // Total units sold across top 10 products
        return data.stream()
                .mapToDouble(DataPoint::getValue)
                .sum();
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start,
                                        LocalDate end,
                                        String bakery) throws Exception {

        return dao.getTopProducts(start, end, bakery);
    }

    @Override
    public String getTitle() {
        return "Top Products (Units Sold)";
    }
}
