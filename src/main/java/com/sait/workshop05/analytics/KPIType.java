// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.analytics;

public enum KPIType {

    REVENUE_OVER_TIME("Revenue Over Time") {
        @Override
        public KPIHandler createHandler() {
            return new RevenueOverTimeHandler();
        }
    },

    REVENUE_BY_BAKERY("Revenue by Bakery") {
        @Override
        public KPIHandler createHandler() {
            return new RevenueByBakeryHandler();
        }
    },

    AVERAGE_ORDER_VALUE("Average Order Value") {
        @Override
        public KPIHandler createHandler() {
            return new AverageOrderValueHandler();
        }
    },

    COMPLETION_RATE("Completion Rate") {
        @Override
        public KPIHandler createHandler() {
            return new CompletionRateHandler();
        }
    },

    TOP_PRODUCTS("Top Products") {
        @Override
        public KPIHandler createHandler() {
            return new TopProductsHandler();
        }
    },

    SALES_BY_EMPLOYEE("Sales by Employee") {
        @Override
        public KPIHandler createHandler() {
            return new SalesByEmployeeHandler();
        }
    };

    private final String displayName;

    KPIType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract KPIHandler createHandler();

    public static KPIType fromDisplayName(String name) {
        for (KPIType t : values()) {
            if (t.displayName.equalsIgnoreCase(name)) return t;
        }
        throw new IllegalArgumentException("Unknown KPI: " + name);
    }
}