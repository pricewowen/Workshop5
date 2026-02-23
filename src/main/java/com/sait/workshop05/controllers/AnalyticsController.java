// η℩.cαηtor ↈ (and his AI, ⌈𝗆𝖾𝗍𝖺𝖼𝗈𝖽𝖺⌋ ⊛)

package com.sait.workshop05.controllers;

import com.sait.workshop05.analytics.*;
import com.sait.workshop05.database.AnalyticsDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class AnalyticsController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> bakeryComboBox;
    @FXML private ComboBox<String> kpiComboBox;
    @FXML private ComboBox<String> chartTypeComboBox;
    @FXML private Label kpiValueLabel;
    @FXML private Label kpiTitleLabel;
    @FXML private StackPane chartContainer;

    @FXML
    public void initialize() {

        kpiComboBox.setItems(FXCollections.observableArrayList(
                KPIType.REVENUE_OVER_TIME.getDisplayName(),
                KPIType.REVENUE_BY_BAKERY.getDisplayName(),
                KPIType.AVERAGE_ORDER_VALUE.getDisplayName(),
                KPIType.COMPLETION_RATE.getDisplayName(),
                KPIType.TOP_PRODUCTS.getDisplayName()
        ));

        kpiComboBox.setValue(KPIType.REVENUE_OVER_TIME.getDisplayName());

        chartTypeComboBox.setItems(FXCollections.observableArrayList(
                ChartType.LINE.getDisplayName(),
                ChartType.BAR.getDisplayName(),
                ChartType.PIE.getDisplayName()
        ));

        chartTypeComboBox.setValue(ChartType.LINE.getDisplayName());

        loadBakeryNames();

        // Initial date config
        configureDatePickers("All Bakeries");

        // When bakery changes → update valid dates
        bakeryComboBox.setOnAction(e -> {
            String selected = bakeryComboBox.getValue();
            configureDatePickers(selected);
            onRefresh();
        });

        // When KPI changes → enforce domain logic
        kpiComboBox.setOnAction(e -> {

            KPIType type =
                    KPIType.fromDisplayName(kpiComboBox.getValue());

            if (type == KPIType.REVENUE_BY_BAKERY) {

                bakeryComboBox.setValue("All Bakeries");
                bakeryComboBox.setDisable(true);
                configureDatePickers("All Bakeries");

            } else {

                bakeryComboBox.setDisable(false);
                configureDatePickers(bakeryComboBox.getValue());
            }

            onRefresh();
        });

        // Enforce start <= end
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

    @FXML
    private void onRefresh() {

        if (kpiComboBox.getValue() == null) return;

        try {

            KPIType type =
                    KPIType.fromDisplayName(kpiComboBox.getValue());

            KPIHandler handler = type.createHandler();

            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            String bakery = bakeryComboBox.getValue();

            // Defensive date validation
            if (start != null && end != null && start.isAfter(end)) {
                kpiValueLabel.setText("Invalid Date Range");
                kpiTitleLabel.setText("");
                chartContainer.getChildren().clear();
                return;
            }

            double primaryValue =
                    handler.getPrimaryValue(start, end, bakery);

            // Format based on KPI type
            switch (type) {

                case TOP_PRODUCTS -> 
                        kpiValueLabel.setText(String.format("%.0f Units", primaryValue));

                case COMPLETION_RATE ->
                        kpiValueLabel.setText(String.format("%.2f%%", primaryValue));

                case REVENUE_OVER_TIME,
                     REVENUE_BY_BAKERY,
                     AVERAGE_ORDER_VALUE ->
                        kpiValueLabel.setText(String.format("$%.2f", primaryValue));
            }

            kpiTitleLabel.setText(handler.getTitle());

            ChartType chartType =
                    ChartType.fromDisplayName(chartTypeComboBox.getValue());

            renderChart(
                    handler.getChartData(start, end, bakery),
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
        LineChart<String, Number> chart =
                new LineChart<>(xAxis, yAxis);
        
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();

        for (DataPoint dp : data) {
            series.getData().add(
                    new XYChart.Data<>(dp.getLabel(), dp.getValue()));
        }

        chart.getData().add(series);
        chartContainer.getChildren().add(chart);
    }

    private void renderBarChart(List<DataPoint> data) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart =
                new BarChart<>(xAxis, yAxis);
        
        chart.setLegendVisible(false);

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();

        for (DataPoint dp : data) {
            series.getData().add(
                    new XYChart.Data<>(dp.getLabel(), dp.getValue()));
        }

        chart.getData().add(series);
        chartContainer.getChildren().add(chart);
    }

    private void renderPieChart(List<DataPoint> data) {

        PieChart chart = new PieChart();

        for (DataPoint dp : data) {
            chart.getData().add(
                    new PieChart.Data(dp.getLabel(), dp.getValue()));
        }

        chartContainer.getChildren().add(chart);
    }
    
    private void loadBakeryNames() {

        try {
            AnalyticsDAO dao = new AnalyticsDAO();

            List<String> bakeries = dao.getBakeryNames();

            bakeries.add(0, "All Bakeries");

            bakeryComboBox.setItems(
                    FXCollections.observableArrayList(bakeries)
            );

            bakeryComboBox.setValue("All Bakeries");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void configureDatePickers(String bakery) {

        try {

            AnalyticsDAO dao = new AnalyticsDAO();

            List<LocalDate> validDates =
                    dao.getAvailableOrderDates(bakery);

            if (validDates.isEmpty()) {

                startDatePicker.setValue(null);
                endDatePicker.setValue(null);
                return;
            }

            // Sort just to be safe
            validDates.sort(LocalDate::compareTo);

            LocalDate first = validDates.get(0);
            LocalDate last  = validDates.get(validDates.size() - 1);

            // Auto-set range
            startDatePicker.setValue(first);
            endDatePicker.setValue(last);

            // Convert to Set for faster lookup
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
