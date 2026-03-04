
package com.sait.workshop05.analytics;

import java.time.LocalDate;
import java.util.List;

public interface KPIHandler {

    /**
     * @param bakerySelection bakery name OR "All Bakeries" OR "All My Bakeries"
     * @param scopeBakeryIds if null/empty => no restriction (admin)
     *                      if non-empty => restrict all queries to these bakeryIds (employee scope)
     */
    double getPrimaryValue(LocalDate start,
                           LocalDate end,
                           String bakerySelection,
                           List<Integer> scopeBakeryIds) throws Exception;

    List<DataPoint> getChartData(LocalDate start,
                                 LocalDate end,
                                 String bakerySelection,
                                 List<Integer> scopeBakeryIds) throws Exception;

    String getTitle();
}