// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.analytics;

import com.sait.workshop05.api.AnalyticsApi;

import java.sql.SQLException;
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
                                  List<Integer> scopeBakeryIds) throws SQLException {
        try {
            return AnalyticsApi.getAverageOrderValue(start, end, bakerySelection);
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start,
                                        LocalDate end,
                                        String bakerySelection,
                                        List<Integer> scopeBakeryIds) throws SQLException {
        try {
            return AnalyticsApi.getAverageOrderValueOverTime(start, end, bakerySelection);
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}