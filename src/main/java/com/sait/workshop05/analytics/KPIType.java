package com.sait.workshop05.analytics;

public enum KPIType {

    REVENUE_OVER_TIME("Revenue Over Time"),
    REVENUE_BY_BAKERY("Revenue By Bakery"),
    AVERAGE_ORDER_VALUE("Average Order Value"),
    COMPLETION_RATE("Completion Rate"),
    TOP_PRODUCTS("Top Products");

    private final String displayName;

    KPIType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static KPIType fromDisplayName(String displayName) {
        for (KPIType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown KPI: " + displayName);
    }

    public KPIHandler createHandler() {
        return switch (this) {
            case REVENUE_OVER_TIME -> new RevenueOverTimeHandler();
            case REVENUE_BY_BAKERY -> new RevenueByBakeryHandler();
            case AVERAGE_ORDER_VALUE -> new AOVHandler();
            case COMPLETION_RATE -> new CompletionRateHandler();
            case TOP_PRODUCTS -> new TopProductsHandler();
        };
    }
}
