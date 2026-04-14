package com.sait.workshop05.analytics;

import com.sait.workshop05.api.AnalyticsApi;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Groups daily analytics series into bi-weekly or monthly buckets for compressed charts.
 */
public final class AnalyticsTimeSeriesCompressor {

    private AnalyticsTimeSeriesCompressor() {
    }

    public record DateBucket(LocalDate label, LocalDate rangeStart, LocalDate rangeEnd) {
    }

    public static List<DateBucket> listBuckets(LocalDate start,
                                                LocalDate end,
                                                TimeSeriesGranularity granularity) {
        return switch (granularity) {
            case DAILY -> listDailyBuckets(start, end);
            case BIWEEKLY -> listBiweeklyBuckets(start, end);
            case MONTHLY -> listMonthBuckets(start, end);
        };
    }

    private static List<DateBucket> listDailyBuckets(LocalDate start, LocalDate end) {
        List<DateBucket> out = new ArrayList<>();
        LocalDate d = start;
        while (!d.isAfter(end)) {
            out.add(new DateBucket(d, d, d));
            d = d.plusDays(1);
        }
        return out;
    }

    private static List<DateBucket> listBiweeklyBuckets(LocalDate start, LocalDate end) {
        List<DateBucket> out = new ArrayList<>();
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            LocalDate rs = cur;
            LocalDate re = cur.plusDays(13);
            if (re.isAfter(end)) {
                re = end;
            }
            out.add(new DateBucket(rs, rs, re));
            cur = cur.plusDays(14);
        }
        return out;
    }

    private static List<DateBucket> listMonthBuckets(LocalDate start, LocalDate end) {
        List<DateBucket> out = new ArrayList<>();
        YearMonth ymStart = YearMonth.from(start);
        YearMonth ymEnd = YearMonth.from(end);
        for (YearMonth ym = ymStart; !ym.isAfter(ymEnd); ym = ym.plusMonths(1)) {
            LocalDate monthFirst = ym.atDay(1);
            LocalDate monthLast = ym.atEndOfMonth();
            LocalDate rs = start.isAfter(monthFirst) ? start : monthFirst;
            LocalDate re = end.isBefore(monthLast) ? end : monthLast;
            if (!rs.isAfter(re)) {
                out.add(new DateBucket(monthFirst, rs, re));
            }
        }
        return out;
    }

    /**
     * @param recognized true = primary (recognized) series, false = in-progress companion series
     */
    public static List<DataPoint> compress(KPIType kpiType,
                                            boolean recognized,
                                            List<DataPoint> dailySeries,
                                            LocalDate rangeStart,
                                            LocalDate rangeEnd,
                                            TimeSeriesGranularity granularity,
                                            String bakerySelection) throws Exception {

        List<DateBucket> buckets = listBuckets(rangeStart, rangeEnd, granularity);

        return switch (kpiType) {
            case REVENUE_OVER_TIME -> compressBySummingDaily(dailySeries, buckets);
            case AVERAGE_ORDER_VALUE -> compressAverageOrderValue(
                    recognized, buckets, bakerySelection);
            case COMPLETION_RATE -> compressCompletionRate(recognized, buckets, bakerySelection);
            default -> dailySeries;
        };
    }

    private static List<DataPoint> compressBySummingDaily(List<DataPoint> dailySeries,
                                                         List<DateBucket> buckets) {
        Map<LocalDate, Double> byDay = new HashMap<>();
        if (dailySeries != null) {
            for (DataPoint dp : dailySeries) {
                LocalDate d = LocalDate.parse(dp.getLabel());
                byDay.merge(d, dp.getValue(), Double::sum);
            }
        }
        List<DataPoint> out = new ArrayList<>();
        for (DateBucket b : buckets) {
            double sum = 0.0;
            LocalDate d = b.rangeStart();
            while (!d.isAfter(b.rangeEnd())) {
                sum += byDay.getOrDefault(d, 0.0);
                d = d.plusDays(1);
            }
            out.add(new DataPoint(b.label().toString(), sum));
        }
        return out;
    }

    private static List<DataPoint> compressAverageOrderValue(boolean recognized,
                                                             List<DateBucket> buckets,
                                                             String bakerySelection) throws Exception {
        List<DataPoint> out = new ArrayList<>();
        for (DateBucket b : buckets) {
            double v = recognized
                    ? AnalyticsApi.getAverageOrderValue(b.rangeStart(), b.rangeEnd(), bakerySelection)
                    : AnalyticsApi.getInProgressAverageOrderValue(b.rangeStart(), b.rangeEnd(), bakerySelection);
            out.add(new DataPoint(b.label().toString(), v));
        }
        return out;
    }

    private static List<DataPoint> compressCompletionRate(boolean recognized,
                                                          List<DateBucket> buckets,
                                                          String bakerySelection) throws Exception {
        List<DataPoint> out = new ArrayList<>();
        for (DateBucket b : buckets) {
            double v = recognized
                    ? AnalyticsApi.getCompletionRate(b.rangeStart(), b.rangeEnd(), bakerySelection)
                    : AnalyticsApi.getInProgressRate(b.rangeStart(), b.rangeEnd(), bakerySelection);
            out.add(new DataPoint(b.label().toString(), v));
        }
        return out;
    }

    public static long inclusiveDayCount(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }
}
