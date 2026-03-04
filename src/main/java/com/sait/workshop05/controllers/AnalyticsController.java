// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.controllers;

import com.sait.workshop05.analytics.*;
import com.sait.workshop05.database.AnalyticsDAO;
import com.sait.workshop05.session.UserSession;
import com.sait.workshop05.util.ErrorHandler;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalyticsController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> bakeryComboBox;
    @FXML private ComboBox<String> kpiComboBox;
    @FXML private ComboBox<String> chartTypeComboBox;
    @FXML private Label kpiValueLabel;
    @FXML private Label kpiTitleLabel;
    @FXML private StackPane chartContainer;

    private final UserSession session = UserSession.getInstance();
    private final AnalyticsDAO dao = new AnalyticsDAO();

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

        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (endDatePicker.getValue() != null &&
                    newVal != null &&
                    newVal.isAfter(endDatePicker.getValue())) {
                endDatePicker.setValue(newVal);
            }
        });

        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (startDatePicker.getValue() != null &&
                    newVal != null &&
                    newVal.isBefore(startDatePicker.getValue())) {
                startDatePicker.setValue(newVal);
            }
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
                List<String> bakeries = dao.getBakeryNames();
                bakeries.add(0, ALL_BAKERIES_ADMIN);
                bakeryComboBox.setItems(FXCollections.observableArrayList(bakeries));
                bakeryComboBox.setValue(ALL_BAKERIES_ADMIN);
            } else {
                List<Integer> scopeIds = session.getAccessibleBakeryIds();
                List<String> bakeries = dao.getBakeryNamesByIds(scopeIds);

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
                chartContainer.getChildren().clear();
                return;
            }

            List<Integer> scopeBakeryIds = session.isAdmin() ? null : session.getAccessibleBakeryIds();

            double primaryValue = handler.getPrimaryValue(start, end, bakerySelection, scopeBakeryIds);

            switch (type) {
                case TOP_PRODUCTS ->
                        kpiValueLabel.setText(String.format("%.0f Units", primaryValue));
                case COMPLETION_RATE ->
                        kpiValueLabel.setText(String.format("%.2f%%", primaryValue));
                case REVENUE_OVER_TIME,
                     REVENUE_BY_BAKERY,
                     AVERAGE_ORDER_VALUE,
                     SALES_BY_EMPLOYEE ->
                        kpiValueLabel.setText(String.format("$%.2f", primaryValue));
            }

            kpiTitleLabel.setText(handler.getTitle());

            ChartType chartType = ChartType.fromDisplayName(chartTypeComboBox.getValue());

            renderChart(
                    handler.getChartData(start, end, bakerySelection, scopeBakeryIds),
                    chartType
            );

        } catch (Exception e) {
            e.printStackTrace();
            kpiValueLabel.setText("Error");
            kpiTitleLabel.setText("Failed to load KPI");
            chartContainer.getChildren().clear();
        }
    }

    private void renderChart(List<DataPoint> data, ChartType type) {
        chartContainer.getChildren().clear();

        switch (type) {
            case LINE -> renderLineChart(data);
            case BAR -> renderBarChart(data);
            case PIE -> renderPieChart(data);
        }
    }

    private void renderLineChart(List<DataPoint> data) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);

        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (DataPoint dp : data) {
            series.getData().add(new XYChart.Data<>(dp.getLabel(), dp.getValue()));
        }

        chart.getData().add(series);
        chartContainer.getChildren().add(chart);
    }

    private void renderBarChart(List<DataPoint> data) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (DataPoint dp : data) {
            series.getData().add(new XYChart.Data<>(dp.getLabel(), dp.getValue()));
        }

        chart.getData().add(series);
        chartContainer.getChildren().add(chart);
    }

    private void renderPieChart(List<DataPoint> data) {

        PieChart chart = new PieChart();

        for (DataPoint dp : data) {
            chart.getData().add(new PieChart.Data(dp.getLabel(), dp.getValue()));
        }

        chartContainer.getChildren().add(chart);
    }

    private void configureDatePickers(String bakerySelection) {

        try {
            List<Integer> scopeBakeryIds = session.isAdmin() ? null : session.getAccessibleBakeryIds();

            List<LocalDate> validDates = dao.getAvailableOrderDates(bakerySelection, scopeBakeryIds);

            if (validDates.isEmpty()) {
                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
                return;
            }

            validDates.sort(LocalDate::compareTo);

            LocalDate first = validDates.get(0);
            LocalDate last  = validDates.get(validDates.size() - 1);

            startDatePicker.setValue(first);
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}