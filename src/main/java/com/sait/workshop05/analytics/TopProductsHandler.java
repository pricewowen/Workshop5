// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.analytics;

import com.sait.workshop05.database.AnalyticsDAO;

import java.time.LocalDate;
import java.util.List;

public class TopProductsHandler implements KPIHandler {

    private final AnalyticsDAO dao = new AnalyticsDAO();

    @Override
    public double getPrimaryValue(LocalDate start, LocalDate end, String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        // Primary value: total units sold across top products (sum of chart values)
        double sum = 0;
        for (DataPoint dp : getChartData(start, end, bakerySelection, scopeBakeryIds)) {
            sum += dp.getValue();
        }
        return sum;
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start, LocalDate end, String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        return dao.getTopProducts(start, end, bakerySelection, scopeBakeryIds);
    }

    @Override
    public String getTitle() {
        return "Top Products";
    }
}