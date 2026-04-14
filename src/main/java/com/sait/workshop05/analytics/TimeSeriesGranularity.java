package com.sait.workshop05.analytics;

/**
 * Bucketing for time-series charts when "Compressed View" is enabled.
 */
public enum TimeSeriesGranularity {
    DAILY,
    BIWEEKLY,
    MONTHLY;

    /**
     * Longer ranges use coarser buckets so the category axis stays readable.
     */
    public static TimeSeriesGranularity forCompressedRange(long inclusiveDays) {
        if (inclusiveDays > 90) {
            return MONTHLY;
        }
        if (inclusiveDays > 21) {
            return BIWEEKLY;
        }
        return DAILY;
    }
}
