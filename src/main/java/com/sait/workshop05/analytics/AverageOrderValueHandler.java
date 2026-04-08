package com.sait.workshop05.analytics;

import com.sait.workshop05.api.AnalyticsApi;

import java.time.LocalDate;
import java.util.List;

public class AverageOrderValueHandler implements KPIHandler {

    @Override
    public String getTitle() {
        return "Average Order Value";
    }

    @Override
    public double getPrimaryValue(LocalDate start,
                                  LocalDate end,
                                  String bakerySelection,
                                  List<Integer> scopeBakeryIds) throws Exception {
        return AnalyticsApi.getAverageOrderValue(start, end, bakerySelection);
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start,
                                        LocalDate end,
                                        String bakerySelection,
                                        List<Integer> scopeBakeryIds) throws Exception {
        return AnalyticsApi.getAverageOrderValueOverTime(start, end, bakerySelection);
    }
}