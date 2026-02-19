package com.sait.workshop05.analytics;

import java.time.LocalDate;
import java.util.List;

public interface KPIHandler {

    String getTitle();

    double getPrimaryValue(LocalDate start, LocalDate end, String bakery);

    List<DataPoint> getChartData(LocalDate start, LocalDate end, String bakery);
}
