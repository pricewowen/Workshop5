// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.analytics;

import java.time.LocalDate;
import java.util.List;

public interface KPIHandler {

    double getPrimaryValue(LocalDate start, LocalDate end, String bakery) throws Exception;

    List<DataPoint> getChartData(LocalDate start, LocalDate end, String bakery) throws Exception;

    String getTitle();
}
