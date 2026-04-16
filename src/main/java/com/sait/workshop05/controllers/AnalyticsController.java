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

import com.sait.workshop05.api.OrderApi;
import com.sait.workshop05.models.Order;
import java.time.LocalDateTime;

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

    private List<LocalDate> allValidDates = new ArrayList<>();

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

        if (kpiType == KPIType.TOP_PRODUCTS) {
            yAxis.setAutoRanging(false);

            double maxValue = 0.0;
            for (DataPoint dp : primaryData) {
                maxValue = Math.max(maxValue, dp.getValue());
            }
            for (DataPoint dp : secondaryData) {
                maxValue = Math.max(maxValue, dp.getValue());
            }

            int maxInt = Math.max(1, (int) Math.ceil(maxValue));

            double tickUnit;
            if (maxInt <= 8) {
                tickUnit = 1;
            } else if (maxInt <= 16) {
                tickUnit = 2;
            } else {
                tickUnit = 5;
            }

            double upperBound = Math.ceil(maxInt / tickUnit) * tickUnit;

            yAxis.setLowerBound(0);
            yAxis.setUpperBound(upperBound);
            yAxis.setTickUnit(tickUnit);
            yAxis.setMinorTickCount(0);
            yAxis.setForceZeroInRange(true);
            yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "", ""));
        }

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> recognizedSeries = new XYChart.Series<>();
        recognizedSeries.setName("Recognized");

        XYChart.Series<String, Number> inProgressSeries = new XYChart.Series<>();
        inProgressSeries.setName("In Progress");

        if (usesDateLabels(kpiType)) {
            List<LocalDate> orderedDates = isCompressedView()
                    ? buildCompressedDateList(startDatePicker.getValue(), endDatePicker.getValue())
                    : buildOrderedDateList(primaryData, secondaryData, startDatePicker.getValue(), endDatePicker.getValue(), false);

            List<String> orderedCategories = buildDisplayDateCategories(orderedDates);

            xAxis.setAutoRanging(false);
            xAxis.setCategories(FXCollections.observableArrayList(orderedCategories));

            maybeRotateAxisLabels(xAxis, orderedCategories, kpiType);

            Map<String, Double> primaryDateMap = toIsoDateValueMap(primaryData);
            Map<String, Double> secondaryDateMap = toIsoDateValueMap(secondaryData);

            for (int i = 0; i < orderedDates.size(); i++) {
                LocalDate date = orderedDates.get(i);
                String category = orderedCategories.get(i);
                String iso = date.toString();

                recognizedSeries.getData().add(
                        new XYChart.Data<>(category, primaryDateMap.getOrDefault(iso, 0.0))
                );

                inProgressSeries.getData().add(
                        new XYChart.Data<>(category, secondaryDateMap.getOrDefault(iso, 0.0))
                );
            }
        } else {
            List<String> orderedCategories = buildOrderedCategoricalLabels(primaryData, secondaryData);

            xAxis.setAutoRanging(false);
            xAxis.setCategories(FXCollections.observableArrayList(orderedCategories));

            maybeRotateAxisLabels(xAxis, orderedCategories, kpiType);

            Map<String, Double> primaryMap = toValueMap(primaryData);
            Map<String, Double> secondaryMap = toValueMap(secondaryData);

            for (String category : orderedCategories) {
                recognizedSeries.getData().add(
                        new XYChart.Data<>(category, primaryMap.getOrDefault(category, 0.0))
                );

                inProgressSeries.getData().add(
                        new XYChart.Data<>(category, secondaryMap.getOrDefault(category, 0.0))
                );
            }
        }

        chart.getData().add(recognizedSeries);
        chart.getData().add(inProgressSeries);
        chart.setLegendVisible(true);

        chartContainer.getChildren().add(chart);
    }

    private void renderBarChart(List<DataPoint> primaryData,
                                List<DataPoint> secondaryData,
                                KPIType kpiType) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        if (kpiType == KPIType.TOP_PRODUCTS) {
            yAxis.setAutoRanging(false);

            double maxValue = 0.0;
            for (DataPoint dp : primaryData) {
                maxValue = Math.max(maxValue, dp.getValue());
            }
            for (DataPoint dp : secondaryData) {
                maxValue = Math.max(maxValue, dp.getValue());
            }

            int maxInt = Math.max(1, (int) Math.ceil(maxValue));

            double tickUnit;
            if (maxInt <= 8) {
                tickUnit = 1;
            } else if (maxInt <= 16) {
                tickUnit = 2;
            } else {
                tickUnit = 5;
            }

            double upperBound = Math.ceil(maxInt / tickUnit) * tickUnit;

            yAxis.setLowerBound(0);
            yAxis.setUpperBound(upperBound);
            yAxis.setTickUnit(tickUnit);
            yAxis.setMinorTickCount(0);
            yAxis.setForceZeroInRange(true);
            yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "", ""));
        }

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> recognizedSeries = new XYChart.Series<>();
        recognizedSeries.setName("Recognized");

        XYChart.Series<String, Number> inProgressSeries = new XYChart.Series<>();
        inProgressSeries.setName("In Progress");

        if (usesDateLabels(kpiType)) {
            List<LocalDate> orderedDates = isCompressedView()
                    ? buildCompressedDateList(startDatePicker.getValue(), endDatePicker.getValue())
                    : buildOrderedDateList(primaryData, secondaryData, startDatePicker.getValue(), endDatePicker.getValue(), false);

            List<String> orderedCategories = buildDisplayDateCategories(orderedDates);

            xAxis.setAutoRanging(false);
            xAxis.setCategories(FXCollections.observableArrayList(orderedCategories));

            maybeRotateAxisLabels(xAxis, orderedCategories, kpiType);

            Map<String, Double> primaryDateMap = toIsoDateValueMap(primaryData);
            Map<String, Double> secondaryDateMap = toIsoDateValueMap(secondaryData);

            for (int i = 0; i < orderedDates.size(); i++) {
                LocalDate date = orderedDates.get(i);
                String category = orderedCategories.get(i);
                String iso = date.toString();

                recognizedSeries.getData().add(
                        new XYChart.Data<>(category, primaryDateMap.getOrDefault(iso, 0.0))
                );

                inProgressSeries.getData().add(
                        new XYChart.Data<>(category, secondaryDateMap.getOrDefault(iso, 0.0))
                );
            }
        } else {
            List<String> orderedCategories = buildOrderedCategoricalLabels(primaryData, secondaryData);

            xAxis.setAutoRanging(false);
            xAxis.setCategories(FXCollections.observableArrayList(orderedCategories));

            maybeRotateAxisLabels(xAxis, orderedCategories, kpiType);

            Map<String, Double> primaryMap = toValueMap(primaryData);
            Map<String, Double> secondaryMap = toValueMap(secondaryData);

            for (String category : orderedCategories) {
                recognizedSeries.getData().add(
                        new XYChart.Data<>(category, primaryMap.getOrDefault(category, 0.0))
                );

                inProgressSeries.getData().add(
                        new XYChart.Data<>(category, secondaryMap.getOrDefault(category, 0.0))
                );
            }
        }

        chart.getData().add(recognizedSeries);
        chart.getData().add(inProgressSeries);
        chart.setLegendVisible(true);

        chartContainer.getChildren().add(chart);
    }

    private void maybeRotateAxisLabels(CategoryAxis xAxis, List<String> categories, KPIType kpiType) {
        if (kpiType == KPIType.REVENUE_BY_BAKERY) {
            xAxis.setTickLabelRotation(0);
            xAxis.setStyle("-fx-tick-label-rotation: 0;");
            return;
        }

        if (categories == null || categories.isEmpty()) {
            xAxis.setTickLabelRotation(0);
            xAxis.setStyle("-fx-tick-label-rotation: 0;");
            return;
        }

        int longestLabelLength = categories.stream()
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .max()
                .orElse(0);

        boolean shouldRotate = categories.size() > 10 || longestLabelLength > 10;

        if (shouldRotate) {
            xAxis.setTickLabelRotation(45);
            xAxis.setStyle("-fx-tick-label-rotation: 45;");
        } else {
            xAxis.setTickLabelRotation(0);
            xAxis.setStyle("-fx-tick-label-rotation: 0;");
        }
    }

    private String formatDateLabel(LocalDate date) {
        return date.getMonthValue() + "/" + date.getDayOfMonth();
    }

    private List<LocalDate> buildOrderedDateList(List<DataPoint> primaryData,
                                                 List<DataPoint> secondaryData,
                                                 LocalDate start,
                                                 LocalDate end,
                                                 boolean compressedView) {

        List<LocalDate> orderedDates = new ArrayList<>();

        if (!compressedView && start != null && end != null) {
            LocalDate current = start;
            while (!current.isAfter(end)) {
                orderedDates.add(current);
                current = current.plusDays(1);
            }
        } else {
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

            orderedDates.addAll(dates);
        }

        return orderedDates;
    }

    private List<LocalDate> buildCompressedDateList(LocalDate start, LocalDate end) {
        List<LocalDate> orderedDates = new ArrayList<>();

        if (start == null || end == null) {
            return orderedDates;
        }

        for (LocalDate date : allValidDates) {
            if (!date.isBefore(start) && !date.isAfter(end)) {
                orderedDates.add(date);
            }
        }

        return orderedDates;
    }

    private List<String> buildDisplayDateCategories(List<LocalDate> orderedDates) {
        int labelStride = 1;
        int count = orderedDates.size();

        if (count >= 29) {
            labelStride = (int) Math.ceil(count / 28.0);
        }

        List<String> ordered = new ArrayList<>();
        for (int i = 0; i < orderedDates.size(); i++) {
            LocalDate date = orderedDates.get(i);

            boolean isLast = i == orderedDates.size() - 1;
            boolean showByStride = (labelStride == 1) || (i % labelStride == 0);
            boolean showLastSafely = isLast && ((orderedDates.size() - 1) % labelStride != labelStride - 1);

            if (showByStride || showLastSafely) {
                ordered.add(formatDateLabel(date));
            } else {
                ordered.add(hiddenDateCategoryKey(i));
            }
        }

        return ordered;
    }

    private String hiddenDateCategoryKey(int index) {
        return "\u200B".repeat(index + 1);
    }

    private List<String> buildOrderedCategories(List<DataPoint> primaryData,
                                                List<DataPoint> secondaryData,
                                                KPIType kpiType,
                                                LocalDate start,
                                                LocalDate end,
                                                boolean compressedView) {

        if (usesDateLabels(kpiType)) {
            List<LocalDate> orderedDates = compressedView
                    ? buildCompressedDateList(start, end)
                    : buildOrderedDateList(primaryData, secondaryData, start, end, false);
            return buildDisplayDateCategories(orderedDates);
        }

        return buildOrderedCategoricalLabels(primaryData, secondaryData);
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

    private Map<String, Double> toIsoDateValueMap(List<DataPoint> data) {
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

            List<LocalDate> validDates = loadValidOrderDates(bakerySelection);

            if (validDates.isEmpty()) {
                allValidDates = new ArrayList<>();
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
                startDatePicker.setDayCellFactory(null);
                endDatePicker.setDayCellFactory(null);
                return;
            }

            validDates.sort(LocalDate::compareTo);
            allValidDates = new ArrayList<>(validDates);

            LocalDate first = validDates.get(0);
            LocalDate last  = validDates.get(validDates.size() - 1);

            if (isCompressedView()) {
                if (startDatePicker.getValue() == null || !validDates.contains(startDatePicker.getValue())) {
                    startDatePicker.setValue(first);
                }

                endDatePicker.setValue(last);

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
                if (startDatePicker.getValue() == null || !validDates.contains(startDatePicker.getValue())) {
                    startDatePicker.setValue(first);
                }
                if (endDatePicker.getValue() == null || !validDates.contains(endDatePicker.getValue())) {
                    endDatePicker.setValue(last);
                }

                startDatePicker.setDayCellFactory(null);
                endDatePicker.setDayCellFactory(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LocalDate> loadValidOrderDates(String bakerySelection) throws Exception {
        List<Order> orders = OrderApi.listOrders();
        Set<LocalDate> dates = new TreeSet<>();

        for (Order order : orders) {
            if (order == null || order.getOrderStatus() == null) {
                continue;
            }

            String status = order.getOrderStatus().trim().toLowerCase(Locale.ROOT);
            if ("cancelled".equals(status)) {
                continue;
            }

            if (!bakeryMatches(order, bakerySelection)) {
                continue;
            }

            LocalDate date = analyticsDate(order);
            if (date != null) {
                dates.add(date);
            }
        }

        return new ArrayList<>(dates);
    }

    private boolean bakeryMatches(Order order, String bakerySelection) {
        if (bakerySelection == null
                || bakerySelection.isBlank()
                || ALL_BAKERIES_ADMIN.equals(bakerySelection)
                || ALL_MY_BAKERIES.equals(bakerySelection)) {
            return true;
        }

        String bakery = order.getBakeryDisplay();
        return bakery != null && bakery.equals(bakerySelection);
    }

    private LocalDate analyticsDate(Order order) {
        LocalDateTime placed = order.getOrderPlacedDateTime();
        if (placed != null) {
            return placed.toLocalDate();
        }

        LocalDateTime scheduled = order.getOrderScheduledDateTime();
        if (scheduled != null) {
            return scheduled.toLocalDate();
        }

        LocalDateTime delivered = order.getOrderDeliveredDateTime();
        if (delivered != null) {
            return delivered.toLocalDate();
        }

        return null;
    }
}