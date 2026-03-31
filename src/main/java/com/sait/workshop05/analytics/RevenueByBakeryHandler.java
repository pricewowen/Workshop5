// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.analytics;

import com.sait.workshop05.database.AnalyticsDAO;

import java.time.LocalDate;
import java.util.List;

public class RevenueByBakeryHandler implements KPIHandler {

    private final AnalyticsDAO dao = new AnalyticsDAO();

    @Override
    public double getPrimaryValue(LocalDate start, LocalDate end, String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        // This KPI is really about the chart; primary value can be total revenue in scope.
        return dao.getTotalRevenue(start, end, "All Bakeries", scopeBakeryIds);
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start, LocalDate end, String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        // Admin: scopeBakeryIds null/empty => global by bakery
        // Employee: scopeBakeryIds set => "by bakery" but only within their bakeries
        return dao.getRevenueByBakery(start, end, scopeBakeryIds);
    }

    @Override
    public String getTitle() {
        return "Revenue By Bakery";
    }
}