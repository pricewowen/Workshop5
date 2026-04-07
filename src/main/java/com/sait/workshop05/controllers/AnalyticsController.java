// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.controllers;

import com.sait.workshop05.analytics.*;
import com.sait.workshop05.api.AnalyticsApi;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.ErrorHandler;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.util.*;

public class AnalyticsController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> bakeryComboBox;
    @FXML private ComboBox<String> kpiComboBox;
    @FXML private ComboBox<String> chartTypeComboBox;
    @FXML private Label kpiValueLabel;
    @FXML private Label kpiTitleLabel;
    @FXML private Label secondaryValueLabel;
    @FXML private Label secondaryTitleLabel;
    @FXML private StackPane chartContainer;

    private final UserSession session = UserSession.getInstance();

    private static final String ALL_BAKERIES_ADMIN = "All Bakeries";
    private static final String ALL_MY_BAKERIES = "All My Bakeries";

    @FXML
    public void initialize() {

        if (!session.canAccessAnalytics()) {
            ErrorHandler.showErrorDialog(
                    "Access Denied",
                    "Analytics not available",
                    "This account is not eligible for analytics."
            );
            disableAnalyticsUI();
            return;
        }

        configureKpiOptions();
        configureChartOptions();
        loadBakeryOptions();

        configureDatePickers(bakeryComboBox.getValue());

        bakeryComboBox.setOnAction(e -> {
            configureDatePickers(bakeryComboBox.getValue());
            onRefresh();
        });

        kpiComboBox.setOnAction(e -> {
            KPIType type = KPIType.fromDisplayName(kpiComboBox.getValue());

            if (type == KPIType.REVENUE_BY_BAKERY) {
                if (session.isAdmin()) {
                    bakeryComboBox.setValue(ALL_BAKERIES_ADMIN);
                } else {
                    bakeryComboBox.setValue(ALL_MY_BAKERIES);
                }
                bakeryComboBox.setDisable(true);
                configureDatePickers(bakeryComboBox.getValue());
            } else {
                bakeryComboBox.setDisable(false);
                configureDatePickers(bakeryComboBox.getValue());
            }

            onRefresh();
        });

        chartTypeComboBox.setOnAction(e -> onRefresh());

        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (endDatePicker.getValue() != null &&
                    newVal != null &&
                    newVal.isAfter(endDatePicker.getValue())) {
                endDatePicker.setValue(newVal);
            }
            onRefresh();
        });

        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (startDatePicker.getValue() != null &&
                    newVal != null &&
                    newVal.isBefore(startDatePicker.getValue())) {
                startDatePicker.setValue(newVal);
            }
            onRefresh();
        });

        onRefresh();
    }

    private void disableAnalyticsUI() {
        if (bakeryComboBox != null) bakeryComboBox.setDisable(true);
        if (kpiComboBox != null) kpiComboBox.setDisable(true);
        if (chartTypeComboBox != null) chartTypeComboBox.setDisable(true);
        if (startDatePicker != null) startDatePicker.setDisable(true);
        if (endDatePicker != null) endDatePicker.setDisable(true);

        if (kpiValueLabel != null) kpiValueLabel.setText("N/A");
        if (kpiTitleLabel != null) kpiTitleLabel.setText("Not Authorized");
        if (secondaryValueLabel != null) secondaryValueLabel.setText("N/A");
        if (secondaryTitleLabel != null) secondaryTitleLabel.setText("Not Authorized");
        if (chartContainer != null) chartContainer.getChildren().clear();
    }

    private void configureKpiOptions() {

        if (session.isAdmin()) {
            kpiComboBox.setItems(FXCollections.observableArrayList(
                    KPIType.REVENUE_OVER_TIME.getDisplayName(),
                    KPIType.REVENUE_BY_BAKERY.getDisplayName(),
                    KPIType.AVERAGE_ORDER_VALUE.getDisplayName(),
                    KPIType.COMPLETION_RATE.getDisplayName(),
                    KPIType.TOP_PRODUCTS.getDisplayName(),
                    KPIType.SALES_BY_EMPLOYEE.getDisplayName()
            ));
        } else {
            kpiComboBox.setItems(FXCollections.observableArrayList(
                    KPIType.REVENUE_OVER_TIME.getDisplayName(),
                    KPIType.AVERAGE_ORDER_VALUE.getDisplayName(),
                    KPIType.COMPLETION_RATE.getDisplayName(),
                    KPIType.TOP_PRODUCTS.getDisplayName()
            ));
        }

        kpiComboBox.setValue(KPIType.REVENUE_OVER_TIME.getDisplayName());
    }

    private void configureChartOptions() {
        chartTypeComboBox.setItems(FXCollections.observableArrayList(
                ChartType.LINE.getDisplayName(),
                ChartType.BAR.getDisplayName(),
                ChartType.PIE.getDisplayName()
        ));
        chartTypeComboBox.setValue(ChartType.LINE.getDisplayName());
    }

    private void loadBakeryOptions() {

        try {
            if (session.isAdmin()) {
                List<String> bakeries = AnalyticsApi.getBakeryNames();
                bakeries.add(0, ALL_BAKERIES_ADMIN);
                bakeryComboBox.setItems(FXCollections.observableArrayList(bakeries));
                bakeryComboBox.setValue(ALL_BAKERIES_ADMIN);
            } else {
                List<Integer> scopeIds = session.getAccessibleBakeryIds();
                List<String> bakeries = AnalyticsApi.getBakeryNamesByIds(scopeIds);

                bakeries.add(0, ALL_MY_BAKERIES);

                bakeryComboBox.setItems(FXCollections.observableArrayList(bakeries));
                bakeryComboBox.setValue(ALL_MY_BAKERIES);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRefresh() {

        if (kpiComboBox.getValue() == null) return;

        try {
            KPIType type = KPIType.fromDisplayName(kpiComboBox.getValue());
            KPIHandler handler = type.createHandler();

            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            String bakerySelection = bakeryComboBox.getValue();

            if (start != null && end != null && start.isAfter(end)) {
                kpiValueLabel.setText("Invalid Date Range");
                kpiTitleLabel.setText("");
                secondaryValueLabel.setText("Invalid Date Range");
                secondaryTitleLabel.setText("");
                chartContainer.getChildren().clear();
                return;
            }

            List<Integer> scopeBakeryIds = session.isAdmin() ? null : session.getAccessibleBakeryIds();

            double primaryValue = handler.getPrimaryValue(start, end, bakerySelection, scopeBakeryIds);
            double secondaryValue = getSecondaryValue(type, start, end, bakerySelection, scopeBakeryIds);

            kpiValueLabel.setText(formatValueForType(type, primaryValue));
            kpiTitleLabel.setText(handler.getTitle() + " (Recognized)");

            secondaryValueLabel.setText(formatValueForType(type, secondaryValue));
            secondaryTitleLabel.setText(getSecondaryTitle(type));

            ChartType chartType = ChartType.fromDisplayName(chartTypeComboBox.getValue());

            List<DataPoint> primaryData =
                    handler.getChartData(start, end, bakerySelection, scopeBakeryIds);

            List<DataPoint> secondaryData =
                    getSecondaryChartData(type, start, end, bakerySelection, scopeBakeryIds);

            if (usesDateLabels(type)) {
                Comparator<DataPoint> byDate =
                        Comparator.comparing(dp -> LocalDate.parse(dp.getLabel()));

                primaryData.sort(byDate);
                secondaryData.sort(byDate);
            }

            renderChart(
                    primaryData,
                    secondaryData,
                    type,
                    chartType,
                    primaryValue,
                    secondaryValue
            );

        } catch (Exception e) {
            e.printStackTrace();
            kpiValueLabel.setText("Error");
            kpiTitleLabel.setText("Failed to load KPI");
            secondaryValueLabel.setText("Error");
            secondaryTitleLabel.setText("Failed to load KPI");
            chartContainer.getChildren().clear();
        }
    }

    private boolean usesDateLabels(KPIType type) {
        return type == KPIType.REVENUE_OVER_TIME
                || type == KPIType.AVERAGE_ORDER_VALUE
                || type == KPIType.COMPLETION_RATE;
    }

    private double getSecondaryValue(KPIType type,
                                     LocalDate start,
                                     LocalDate end,
                                     String bakerySelection,
                                     List<Integer> scopeBakeryIds) throws Exception {

        return switch (type) {
            case REVENUE_OVER_TIME, REVENUE_BY_BAKERY ->
                    AnalyticsApi.getInProgressRevenue(start, end,
                            type == KPIType.REVENUE_BY_BAKERY ? ALL_BAKERIES_ADMIN : bakerySelection);

            case AVERAGE_ORDER_VALUE ->
                    AnalyticsApi.getInProgressAverageOrderValue(start, end, bakerySelection);

            case COMPLETION_RATE ->
                    AnalyticsApi.getInProgressRate(start, end, bakerySelection);

            case TOP_PRODUCTS -> {
                double sum = 0;
                for (DataPoint dp : AnalyticsApi.getInProgressTopProducts(start, end, bakerySelection)) {
                    sum += dp.getValue();
                }
                yield sum;
            }

            case SALES_BY_EMPLOYEE ->
                    AnalyticsApi.getInProgressTotalSalesByEmployee(start, end, bakerySelection);
        };
    }

    private List<DataPoint> getSecondaryChartData(KPIType type,
                                                  LocalDate start,
                                                  LocalDate end,
                                                  String bakerySelection,
                                                  List<Integer> scopeBakeryIds) throws Exception {

        return switch (type) {
            case REVENUE_OVER_TIME ->
                    AnalyticsApi.getInProgressRevenueOverTime(start, end, bakerySelection);

            case REVENUE_BY_BAKERY ->
                    AnalyticsApi.getInProgressRevenueByBakery(start, end);

            case AVERAGE_ORDER_VALUE ->
                    AnalyticsApi.getInProgressAverageOrderValueOverTime(start, end, bakerySelection);

            case COMPLETION_RATE ->
                    AnalyticsApi.getInProgressRateOverTime(start, end, bakerySelection);

            case TOP_PRODUCTS ->
                    AnalyticsApi.getInProgressTopProducts(start, end, bakerySelection);

            case SALES_BY_EMPLOYEE ->
                    AnalyticsApi.getInProgressSalesByEmployee(start, end, bakerySelection);
        };
    }

    private String getSecondaryTitle(KPIType type) {
        return switch (type) {
            case REVENUE_OVER_TIME, REVENUE_BY_BAKERY -> "Revenue (In Progress)";
            case AVERAGE_ORDER_VALUE -> "Avg Order Value (In Progress)";
            case COMPLETION_RATE -> "In Progress Rate";
            case TOP_PRODUCTS -> "Units (In Progress)";
            case SALES_BY_EMPLOYEE -> "Sales (In Progress)";
        };
    }

    private String formatValueForType(KPIType type, double value) {
        return switch (type) {
            case TOP_PRODUCTS -> String.format("%.0f Units", value);
            case COMPLETION_RATE -> String.format("%.2f%%", value);
            case REVENUE_OVER_TIME,
                 REVENUE_BY_BAKERY,
                 AVERAGE_ORDER_VALUE,
                 SALES_BY_EMPLOYEE -> String.format("$%.2f", value);
        };
    }

    private void renderChart(List<DataPoint> primaryData,
                             List<DataPoint> secondaryData,
                             KPIType kpiType,
                             ChartType chartType,
                             double primaryValue,
                             double secondaryValue) {

        chartContainer.getChildren().clear();

        switch (chartType) {
            case LINE -> renderLineChart(primaryData, secondaryData, kpiType);
            case BAR -> renderBarChart(primaryData, secondaryData, kpiType);
            case PIE -> renderPieChart(primaryData, secondaryData, kpiType, primaryValue, secondaryValue);
        }
    }

    private void renderLineChart(List<DataPoint> primaryData,
                                 List<DataPoint> secondaryData,
                                 KPIType kpiType) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> recognizedSeries = new XYChart.Series<>();
        recognizedSeries.setName("Recognized");

        XYChart.Series<String, Number> inProgressSeries = new XYChart.Series<>();
        inProgressSeries.setName("In Progress");

        if (usesDateLabels(kpiType)) {
            xAxis.setAutoRanging(false);

            List<String> orderedCategories = buildOrderedDateCategories(primaryData, secondaryData);
            xAxis.setCategories(FXCollections.observableArrayList(orderedCategories));

            Map<String, Double> primaryMap = toValueMap(primaryData);
            Map<String, Double> secondaryMap = toValueMap(secondaryData);

            for (String category : orderedCategories) {
                Double recognizedValue = primaryMap.get(category);
                if (recognizedValue != null) {
                    recognizedSeries.getData().add(new XYChart.Data<>(category, recognizedValue));
                }

                if (secondaryData != null && !secondaryData.isEmpty()) {
                    Double inProgressValue = secondaryMap.get(category);
                    if (inProgressValue != null) {
                        inProgressSeries.getData().add(new XYChart.Data<>(category, inProgressValue));
                    }
                }
            }
        } else {
            for (DataPoint dp : primaryData) {
                recognizedSeries.getData().add(new XYChart.Data<>(dp.getLabel(), dp.getValue()));
            }

            if (secondaryData != null && !secondaryData.isEmpty()) {
                for (DataPoint dp : secondaryData) {
                    inProgressSeries.getData().add(new XYChart.Data<>(dp.getLabel(), dp.getValue()));
                }
            }
        }

        chart.getData().add(recognizedSeries);

        if (secondaryData != null && !secondaryData.isEmpty()) {
            chart.getData().add(inProgressSeries);
            chart.setLegendVisible(true);
        } else {
            chart.setLegendVisible(false);
        }

        chartContainer.getChildren().add(chart);
    }

    private void renderBarChart(List<DataPoint> primaryData,
                                List<DataPoint> secondaryData,
                                KPIType kpiType) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> recognizedSeries = new XYChart.Series<>();
        recognizedSeries.setName("Recognized");

        XYChart.Series<String, Number> inProgressSeries = new XYChart.Series<>();
        inProgressSeries.setName("In Progress");

        if (usesDateLabels(kpiType)) {
            xAxis.setAutoRanging(false);

            List<String> orderedCategories = buildOrderedDateCategories(primaryData, secondaryData);
            xAxis.setCategories(FXCollections.observableArrayList(orderedCategories));

            Map<String, Double> primaryMap = toValueMap(primaryData);
            Map<String, Double> secondaryMap = toValueMap(secondaryData);

            for (String category : orderedCategories) {
                recognizedSeries.getData().add(
                        new XYChart.Data<>(category, primaryMap.getOrDefault(category, 0.0))
                );

                if (secondaryData != null && !secondaryData.isEmpty()) {
                    inProgressSeries.getData().add(
                            new XYChart.Data<>(category, secondaryMap.getOrDefault(category, 0.0))
                    );
                }
            }
        } else {
            for (DataPoint dp : primaryData) {
                recognizedSeries.getData().add(new XYChart.Data<>(dp.getLabel(), dp.getValue()));
            }

            if (secondaryData != null && !secondaryData.isEmpty()) {
                for (DataPoint dp : secondaryData) {
                    inProgressSeries.getData().add(new XYChart.Data<>(dp.getLabel(), dp.getValue()));
                }
            }
        }

        chart.getData().add(recognizedSeries);

        if (secondaryData != null && !secondaryData.isEmpty()) {
            chart.getData().add(inProgressSeries);
            chart.setLegendVisible(true);
        } else {
            chart.setLegendVisible(false);
        }

        chartContainer.getChildren().add(chart);
    }

    private List<String> buildOrderedDateCategories(List<DataPoint> primaryData,
                                                    List<DataPoint> secondaryData) {

        Set<LocalDate> dates = new TreeSet<>();

        if (primaryData != null) {
            for (DataPoint dp : primaryData) {
                dates.add(LocalDate.parse(dp.getLabel()));
            }
        }

        if (secondaryData != null) {
            for (DataPoint dp : secondaryData) {
                dates.add(LocalDate.parse(dp.getLabel()));
            }
        }

        List<String> ordered = new ArrayList<>();
        for (LocalDate date : dates) {
            ordered.add(date.toString());
        }

        return ordered;
    }

    private Map<String, Double> toValueMap(List<DataPoint> data) {
        Map<String, Double> map = new HashMap<>();

        if (data != null) {
            for (DataPoint dp : data) {
                map.put(dp.getLabel(), dp.getValue());
            }
        }

        return map;
    }

    private void renderPieChart(List<DataPoint> primaryData,
                                List<DataPoint> secondaryData,
                                KPIType kpiType,
                                double primaryValue,
                                double secondaryValue) {

        PieChart chart = new PieChart();
        chart.setLegendVisible(true);

        switch (kpiType) {

            case REVENUE_OVER_TIME, AVERAGE_ORDER_VALUE -> {
                double recognizedTotal = sumValues(primaryData);
                double inProgressTotal = sumValues(secondaryData);
                double grandTotal = recognizedTotal + inProgressTotal;

                if (recognizedTotal > 0) {
                    chart.getData().add(new PieChart.Data(
                            formatPieLabel("Recognized", recognizedTotal, grandTotal),
                            recognizedTotal
                    ));
                }
                if (inProgressTotal > 0) {
                    chart.getData().add(new PieChart.Data(
                            formatPieLabel("In Progress", inProgressTotal, grandTotal),
                            inProgressTotal
                    ));
                }
            }

            case COMPLETION_RATE -> {
                double recognizedRate = primaryValue;
                double inProgressRate = secondaryValue;
                double grandTotal = recognizedRate + inProgressRate;

                if (recognizedRate > 0) {
                    chart.getData().add(new PieChart.Data(
                            formatPieLabel("Recognized", recognizedRate, grandTotal),
                            recognizedRate
                    ));
                }
                if (inProgressRate > 0) {
                    chart.getData().add(new PieChart.Data(
                            formatPieLabel("In Progress", inProgressRate, grandTotal),
                            inProgressRate
                    ));
                }
            }

            case REVENUE_BY_BAKERY, TOP_PRODUCTS, SALES_BY_EMPLOYEE -> {
                Map<String, Double> merged = mergeByLabel(primaryData, secondaryData);
                double grandTotal = sumMapValues(merged);

                for (Map.Entry<String, Double> entry : merged.entrySet()) {
                    if (entry.getValue() > 0) {
                        chart.getData().add(new PieChart.Data(
                                formatPieLabel(entry.getKey(), entry.getValue(), grandTotal),
                                entry.getValue()
                        ));
                    }
                }
            }
        }

        chartContainer.getChildren().add(chart);
    }

    private String formatPieLabel(String label, double value, double total) {
        if (total <= 0) {
            return label;
        }
        double percentage = (value / total) * 100.0;
        return String.format("%s (%.1f%%)", label, percentage);
    }

    private double sumValues(List<DataPoint> data) {
        double sum = 0.0;
        if (data != null) {
            for (DataPoint dp : data) {
                sum += dp.getValue();
            }
        }
        return sum;
    }

    private double sumMapValues(Map<String, Double> data) {
        double sum = 0.0;
        for (double value : data.values()) {
            sum += value;
        }
        return sum;
    }

    private Map<String, Double> mergeByLabel(List<DataPoint> primaryData, List<DataPoint> secondaryData) {
        Map<String, Double> merged = new LinkedHashMap<>();

        if (primaryData != null) {
            for (DataPoint dp : primaryData) {
                merged.merge(dp.getLabel(), dp.getValue(), Double::sum);
            }
        }

        if (secondaryData != null) {
            for (DataPoint dp : secondaryData) {
                merged.merge(dp.getLabel(), dp.getValue(), Double::sum);
            }
        }

        return sortDescendingByValue(merged);
    }

    private Map<String, Double> sortDescendingByValue(Map<String, Double> input) {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(input.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        Map<String, Double> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : entries) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        return sorted;
    }

    private void configureDatePickers(String bakerySelection) {

        try {
            List<Integer> scopeBakeryIds = session.isAdmin() ? null : session.getAccessibleBakeryIds();

            List<LocalDate> validDates = AnalyticsApi.getAvailableOrderDates(bakerySelection, scopeBakeryIds);

            if (validDates.isEmpty()) {
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
                return;
            }

            validDates.sort(LocalDate::compareTo);

            LocalDate first = validDates.get(0);
            LocalDate last  = validDates.get(validDates.size() - 1);

            if (startDatePicker.getValue() == null) {
                startDatePicker.setValue(first);
            }
            if (endDatePicker.getValue() == null) {
                endDatePicker.setValue(last);
            }

            Set<LocalDate> validSet = new HashSet<>(validDates);

            startDatePicker.setDayCellFactory(picker ->
                    new DateCell() {
                        @Override
                        public void updateItem(LocalDate date, boolean empty) {
                            super.updateItem(date, empty);
                            if (empty || !validSet.contains(date)) {
                                setDisable(true);
                            }
                        }
                    });

            endDatePicker.setDayCellFactory(picker ->
                    new DateCell() {
                        @Override
                        public void updateItem(LocalDate date, boolean empty) {
                            super.updateItem(date, empty);
                            if (empty || !validSet.contains(date)) {
                                setDisable(true);
                            }
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}