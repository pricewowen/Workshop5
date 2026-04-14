// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.controllers;

import com.sait.workshop05.analytics.*;
import com.sait.workshop05.api.AnalyticsApi;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.ErrorHandler;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
    @FXML private CheckBox compressedViewCheckBox;
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

        if (compressedViewCheckBox != null) {
            compressedViewCheckBox.setSelected(true);
            compressedViewCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                configureDatePickers(bakeryComboBox.getValue());
                onRefresh();
            });
        }

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
        if (compressedViewCheckBox != null) compressedViewCheckBox.setDisable(true);
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

            if (start == null || end == null) {
                kpiValueLabel.setText("No date selected");
                kpiTitleLabel.setText(handler.getTitle());
                secondaryValueLabel.setText("—");
                secondaryTitleLabel.setText(getSecondaryTitle(type));
                chartContainer.getChildren().clear();
                return;
            }

            if (start.isAfter(end)) {
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

            if (type == KPIType.COMPLETION_RATE) {
                double sum = primaryValue + secondaryValue;
                if (sum > 1e-9) {
                    primaryValue = primaryValue / sum * 100.0;
                    secondaryValue = secondaryValue / sum * 100.0;
                }
            }

            kpiValueLabel.setText(formatValueForType(type, primaryValue));
            kpiTitleLabel.setText(primaryKpiTitle(type, handler));

            secondaryValueLabel.setText(formatValueForType(type, secondaryValue));
            secondaryTitleLabel.setText(getSecondaryTitle(type));

            if (type == KPIType.COMPLETION_RATE) {
                Tooltip completionSplitTip = new Tooltip(
                        "Both values use the same orders in this date range. "
                                + "Completed and in progress shares add to 100%.");
                kpiValueLabel.setTooltip(completionSplitTip);
                secondaryValueLabel.setTooltip(completionSplitTip);
            } else {
                kpiValueLabel.setTooltip(null);
                secondaryValueLabel.setTooltip(null);
            }

            ChartType chartType = ChartType.fromDisplayName(chartTypeComboBox.getValue());

            List<DataPoint> primaryData =
                    handler.getChartData(start, end, bakerySelection, scopeBakeryIds);

            List<DataPoint> secondaryData =
                    getSecondaryChartData(type, start, end, bakerySelection, scopeBakeryIds);

            TimeSeriesGranularity chartGranularity = TimeSeriesGranularity.DAILY;
            if (usesDateLabels(type)) {
                long inclusiveDays = AnalyticsTimeSeriesCompressor.inclusiveDayCount(start, end);
                chartGranularity = isCompressedView()
                        ? TimeSeriesGranularity.forCompressedRange(inclusiveDays)
                        : TimeSeriesGranularity.DAILY;
                if (isCompressedView() && chartGranularity != TimeSeriesGranularity.DAILY) {
                    primaryData = AnalyticsTimeSeriesCompressor.compress(
                            type,
                            true,
                            primaryData,
                            start,
                            end,
                            chartGranularity,
                            bakerySelection
                    );
                    secondaryData = AnalyticsTimeSeriesCompressor.compress(
                            type,
                            false,
                            secondaryData,
                            start,
                            end,
                            chartGranularity,
                            bakerySelection
                    );
                }
            }

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
                    secondaryValue,
                    chartGranularity
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

    private String primaryKpiTitle(KPIType type, KPIHandler handler) {
        if (type == KPIType.COMPLETION_RATE) {
            return "Completed";
        }
        return handler.getTitle() + " (Recognized)";
    }

    private boolean usesDateLabels(KPIType type) {
        return type == KPIType.REVENUE_OVER_TIME
                || type == KPIType.AVERAGE_ORDER_VALUE
                || type == KPIType.COMPLETION_RATE;
    }

    private boolean isCompressedView() {
        return compressedViewCheckBox == null || compressedViewCheckBox.isSelected();
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
            case COMPLETION_RATE -> "In progress";
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
                             double secondaryValue,
                             TimeSeriesGranularity chartGranularity) {

        chartContainer.getChildren().clear();

        switch (chartType) {
            case LINE -> renderLineChart(primaryData, secondaryData, kpiType, chartGranularity);
            case BAR -> renderBarChart(primaryData, secondaryData, kpiType, chartGranularity);
            case PIE -> renderPieChart(primaryData, secondaryData, kpiType, primaryValue, secondaryValue);
        }
    }

    private void renderLineChart(List<DataPoint> primaryData,
                                 List<DataPoint> secondaryData,
                                 KPIType kpiType,
                                 TimeSeriesGranularity chartGranularity) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> recognizedSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> inProgressSeries = new XYChart.Series<>();
        applyDualSeriesNames(kpiType, recognizedSeries, inProgressSeries);

        List<String> orderedCategories = buildOrderedCategories(
                primaryData,
                secondaryData,
                kpiType,
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                isCompressedView(),
                chartGranularity
        );

        xAxis.setAutoRanging(false);
        xAxis.setCategories(FXCollections.observableArrayList(orderedCategories));
        applyRotatedDateCategoryAxisLabels(kpiType, xAxis, chart);

        if (kpiType == KPIType.COMPLETION_RATE) {
            applyCompletionRateYAxis(yAxis);
        }

        Map<String, Double> primaryMap = toValueMap(primaryData);
        Map<String, Double> secondaryMap = toValueMap(secondaryData);

        addCompletionAwareSeriesPoints(
                orderedCategories, primaryMap, secondaryMap, kpiType, recognizedSeries, inProgressSeries);

        chart.getData().add(recognizedSeries);
        chart.getData().add(inProgressSeries);
        chart.setLegendVisible(true);

        chartContainer.getChildren().add(chart);
    }

    private void renderBarChart(List<DataPoint> primaryData,
                                List<DataPoint> secondaryData,
                                KPIType kpiType,
                                TimeSeriesGranularity chartGranularity) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> recognizedSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> inProgressSeries = new XYChart.Series<>();
        applyDualSeriesNames(kpiType, recognizedSeries, inProgressSeries);

        List<String> orderedCategories = buildOrderedCategories(
                primaryData,
                secondaryData,
                kpiType,
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                isCompressedView(),
                chartGranularity
        );

        xAxis.setAutoRanging(false);
        xAxis.setCategories(FXCollections.observableArrayList(orderedCategories));
        applyRotatedDateCategoryAxisLabels(kpiType, xAxis, chart);

        if (kpiType == KPIType.COMPLETION_RATE) {
            applyCompletionRateYAxis(yAxis);
        }

        Map<String, Double> primaryMap = toValueMap(primaryData);
        Map<String, Double> secondaryMap = toValueMap(secondaryData);

        addCompletionAwareSeriesPoints(
                orderedCategories, primaryMap, secondaryMap, kpiType, recognizedSeries, inProgressSeries);

        chart.getData().add(recognizedSeries);
        chart.getData().add(inProgressSeries);
        chart.setLegendVisible(true);

        chartContainer.getChildren().add(chart);
    }

    private void applyDualSeriesNames(KPIType kpiType,
                                      XYChart.Series<String, Number> primarySeries,
                                      XYChart.Series<String, Number> secondarySeries) {
        if (kpiType == KPIType.COMPLETION_RATE) {
            primarySeries.setName("Completed");
            secondarySeries.setName("In progress");
        } else {
            primarySeries.setName("Recognized");
            secondarySeries.setName("In Progress");
        }
    }

    private void applyCompletionRateYAxis(NumberAxis yAxis) {
        yAxis.setAutoRanging(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);
        yAxis.setLabel("%");
    }

    private void addCompletionAwareSeriesPoints(List<String> orderedCategories,
                                                Map<String, Double> primaryMap,
                                                Map<String, Double> secondaryMap,
                                                KPIType kpiType,
                                                XYChart.Series<String, Number> primarySeries,
                                                XYChart.Series<String, Number> secondarySeries) {
        for (String category : orderedCategories) {
            double primary = primaryMap.getOrDefault(category, 0.0);
            double secondary = secondaryMap.getOrDefault(category, 0.0);
            if (kpiType == KPIType.COMPLETION_RATE) {
                double sum = primary + secondary;
                if (sum > 1e-9) {
                    primary = primary / sum * 100.0;
                    secondary = secondary / sum * 100.0;
                }
            }
            primarySeries.getData().add(new XYChart.Data<>(category, primary));
            secondarySeries.getData().add(new XYChart.Data<>(category, secondary));
        }
    }

    /**
     * Tilts date category labels on the X axis (~45°) so dense daily or bucket labels stay readable
     * in both standard (full date range) and compressed (sparse / grouped) modes.
     */
    private void applyRotatedDateCategoryAxisLabels(KPIType kpiType,
                                                    CategoryAxis xAxis,
                                                    XYChart<String, Number> chart) {
        if (!usesDateLabels(kpiType)) {
            return;
        }
        xAxis.setTickLabelRotation(-45);
        xAxis.setTickLabelGap(6);
        StackPane.setMargin(chart, new Insets(0, 0, 28, 0));
    }

    private List<String> buildOrderedCategories(List<DataPoint> primaryData,
                                                List<DataPoint> secondaryData,
                                                KPIType kpiType,
                                                LocalDate start,
                                                LocalDate end,
                                                boolean compressedView,
                                                TimeSeriesGranularity chartGranularity) {

        if (usesDateLabels(kpiType)) {
            return buildOrderedDateCategories(
                    primaryData, secondaryData, start, end, compressedView, chartGranularity);
        }

        return buildOrderedCategoricalLabels(primaryData, secondaryData);
    }

    private List<String> buildOrderedDateCategories(List<DataPoint> primaryData,
                                                    List<DataPoint> secondaryData,
                                                    LocalDate start,
                                                    LocalDate end,
                                                    boolean compressedView,
                                                    TimeSeriesGranularity chartGranularity) {

        if (!compressedView && start != null && end != null) {
            List<String> ordered = new ArrayList<>();
            LocalDate current = start;
            while (!current.isAfter(end)) {
                ordered.add(current.toString());
                current = current.plusDays(1);
            }
            return ordered;
        }

        if (compressedView
                && chartGranularity != TimeSeriesGranularity.DAILY
                && start != null
                && end != null) {
            List<String> ordered = new ArrayList<>();
            for (AnalyticsTimeSeriesCompressor.DateBucket b :
                    AnalyticsTimeSeriesCompressor.listBuckets(start, end, chartGranularity)) {
                ordered.add(b.label().toString());
            }
            return ordered;
        }

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

    private List<String> buildOrderedCategoricalLabels(List<DataPoint> primaryData,
                                                       List<DataPoint> secondaryData) {

        LinkedHashSet<String> labels = new LinkedHashSet<>();

        if (primaryData != null) {
            for (DataPoint dp : primaryData) {
                labels.add(dp.getLabel());
            }
        }

        if (secondaryData != null) {
            for (DataPoint dp : secondaryData) {
                labels.add(dp.getLabel());
            }
        }

        return new ArrayList<>(labels);
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
                double completed = primaryValue;
                double open = secondaryValue;
                double sum = completed + open;
                if (sum > 1e-9) {
                    completed = completed / sum * 100.0;
                    open = open / sum * 100.0;
                    chart.getData().add(new PieChart.Data(
                            formatPieLabel("Completed", completed, 100.0),
                            completed
                    ));
                    chart.getData().add(new PieChart.Data(
                            formatPieLabel("In progress", open, 100.0),
                            open
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
                startDatePicker.setDayCellFactory(null);
                endDatePicker.setDayCellFactory(null);
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

            if (isCompressedView()) {
                if (!validDates.contains(startDatePicker.getValue())) {
                    startDatePicker.setValue(first);
                }
                if (!validDates.contains(endDatePicker.getValue())) {
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
            } else {
                startDatePicker.setDayCellFactory(null);
                endDatePicker.setDayCellFactory(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}