package com.sait.workshop05.analytics;

import com.sait.workshop05.api.AnalyticsApi;

import java.time.LocalDate;
import java.util.List;

public class TopProductsHandler implements KPIHandler {

    @Override
    public double getPrimaryValue(LocalDate start,
                                  LocalDate end,
                                  String bakerySelection,
                                  List<Integer> scopeBakeryIds) throws Exception {
        double sum = 0.0;
        for (DataPoint dp : getChartData(start, end, bakerySelection, scopeBakeryIds)) {
            sum += dp.getValue();
        }
        return sum;
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start,
                                        LocalDate end,
                                        String bakerySelection,
                                        List<Integer> scopeBakeryIds) throws Exception {
        return AnalyticsApi.getTopProducts(start, end, bakerySelection);
    }

    @Override
    public String getTitle() {
        return "Top Products";
    }
}