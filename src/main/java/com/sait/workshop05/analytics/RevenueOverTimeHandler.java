// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.analytics;

import com.sait.workshop05.database.AnalyticsDAO;

import java.time.LocalDate;
import java.util.List;

public class RevenueOverTimeHandler implements KPIHandler {

    private final AnalyticsDAO dao = new AnalyticsDAO();

    @Override
    public double getPrimaryValue(LocalDate start, LocalDate end, String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        return dao.getTotalRevenue(start, end, bakerySelection, scopeBakeryIds);
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start, LocalDate end, String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        return dao.getRevenueOverTime(start, end, bakerySelection, scopeBakeryIds);
    }

    @Override
    public String getTitle() {
        return "Revenue Over Time";
    }
}