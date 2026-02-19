package com.sait.workshop05.analytics;

public enum ChartType {

    LINE("Line Chart"),
    BAR("Bar Chart"),
    PIE("Pie Chart");

    private final String displayName;

    ChartType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ChartType fromDisplayName(String displayName) {
        for (ChartType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown chart type: " + displayName);
    }
}
