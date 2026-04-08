package com.sait.workshop05.analytics;

import com.sait.workshop05.api.AnalyticsApi;

import java.time.LocalDate;
import java.util.List;

public class RevenueByBakeryHandler implements KPIHandler {

    @Override
    public double getPrimaryValue(LocalDate start,
                                  LocalDate end,
                                  String bakerySelection,
                                  List<Integer> scopeBakeryIds) throws Exception {
        return AnalyticsApi.getTotalRevenue(start, end, "All Bakeries");
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start,
                                        LocalDate end,
                                        String bakerySelection,
                                        List<Integer> scopeBakeryIds) throws Exception {
        return AnalyticsApi.getRevenueByBakery(start, end);
    }

    @Override
    public String getTitle() {
        return "Revenue By Bakery";
    }
}