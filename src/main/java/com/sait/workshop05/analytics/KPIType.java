package com.sait.workshop05.analytics;

public enum KPIType {
    REVENUE_OVER_TIME("Revenue Over Time"),
    REVENUE_BY_BAKERY("Revenue by Bakery"),
    AVERAGE_ORDER_VALUE("Average Order Value"),
    COMPLETION_RATE("Order Completion Rate"),
    TOP_PRODUCTS("Top Selling Products");

    private final String displayName;

    KPIType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static KPIType fromDisplay(String display) {
        for (KPIType type : values()) {
            if (type.displayName.equals(display)) {
                return type;
            }
        }
        return null;
    }
}
