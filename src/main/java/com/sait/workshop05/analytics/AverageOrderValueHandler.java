// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.analytics;

import com.sait.workshop05.database.AnalyticsDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AverageOrderValueHandler implements KPIHandler {

    private final AnalyticsDAO dao = new AnalyticsDAO();

    @Override
    public String getTitle() {
        return "Average Order Value";
    }

    @Override
    public double getPrimaryValue(LocalDate start,
                                  LocalDate end,
                                  String bakerySelection,
                                  List<Integer> scopeBakeryIds) throws SQLException {
        return dao.getAverageOrderValue(start, end, bakerySelection, scopeBakeryIds);
    }

    @Override
    public List<DataPoint> getChartData(LocalDate start,
                                        LocalDate end,
                                        String bakerySelection,
                                        List<Integer> scopeBakeryIds) throws SQLException {
        return dao.getAverageOrderValueOverTime(start, end, bakerySelection, scopeBakeryIds);
    }
}