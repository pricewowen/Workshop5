// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.analytics;

import com.sait.workshop05.api.AnalyticsApi;

import java.time.LocalDate;
import java.util.List;

public class CompletionRateHandler implements KPIHandler {

    @Override
    public double getPrimaryValue(LocalDate start, LocalDate end, String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        return AnalyticsApi.getCompletionRate(start, end, bakerySelection);
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start, LocalDate end, String bakerySelection, List<Integer> scopeBakeryIds) throws Exception {
        return AnalyticsApi.getCompletionRateOverTime(start, end, bakerySelection);
    }

    @Override
    public String getTitle() {
        return "Completion Rate";
    }
}