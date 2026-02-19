package com.sait.workshop05.controllers;

import com.sait.workshop05.analytics.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.util.List;

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

        bakeryComboBox.setItems(FXCollections.observableArrayList("All Bakeries"));
        bakeryComboBox.setValue("All Bakeries");

        onRefresh();
    }

    @FXML
    private void onRefresh() {

        if (kpiComboBox.getValue() == null) return;

        try {

            // Use your enum factory method properly
        	KPIType type =
        	        KPIType.fromDisplayName(kpiComboBox.getValue());

        	KPIHandler handler = type.createHandler();

            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            String bakery = bakeryComboBox.getValue();

            double primaryValue =
                    handler.getPrimaryValue(start, end, bakery);

            // Intelligent formatting
            String selectedKPI = kpiComboBox.getValue();

            if (selectedKPI.equals(KPIType.TOP_PRODUCTS.getDisplayName())) {
                kpiValueLabel.setText(String.format("%.0f Units", primaryValue));
            }
            else if (selectedKPI.equals(KPIType.COMPLETION_RATE.getDisplayName())) {
                kpiValueLabel.setText(String.format("%.2f%%", primaryValue));
            }
            else {
                // Revenue & AOV are currency
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
}
