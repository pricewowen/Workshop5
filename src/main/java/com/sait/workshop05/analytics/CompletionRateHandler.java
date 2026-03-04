// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.analytics;

import com.sait.workshop05.database.AnalyticsDAO;

import java.time.LocalDate;
import java.util.List;

public class CompletionRateHandler implements KPIHandler {

    private final AnalyticsDAO dao = new AnalyticsDAO();

    @Override
    public double getPrimaryValue(LocalDate start, LocalDate end, String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        return dao.getCompletionRate(start, end, bakerySelection, scopeBakeryIds);
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start, LocalDate end, String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        return dao.getCompletionRateOverTime(start, end, bakerySelection, scopeBakeryIds);
    }

    @Override
    public String getTitle() {
        return "Completion Rate";
    }
}