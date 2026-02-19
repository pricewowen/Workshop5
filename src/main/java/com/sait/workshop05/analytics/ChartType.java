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

    public static ChartType fromDisplay(String display) {
        for (ChartType type : values()) {
            if (type.displayName.equals(display)) {
                return type;
            }
        }
        return LINE;
    }
}
