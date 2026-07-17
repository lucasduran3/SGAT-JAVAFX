package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.dao.model.Product;
import com.mvcjava.sagt.javafx.dto.HeaderSaleWithClient;
import com.mvcjava.sagt.javafx.service.impl.ProductServiceImpl;
import com.mvcjava.sagt.javafx.service.impl.SaleServiceImpl;
import com.mvcjava.sagt.javafx.service.interfaces.ProductService;
import com.mvcjava.sagt.javafx.service.interfaces.SaleService;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DashboardController {

    private static final String LAST_7_DAYS = "Últimos 7 días";
    private static final String LAST_30_DAYS = "Últimos 30 días";
    private static final String CURRENT_MONTH = "Mes actual";
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEE d", new Locale("es", "AR"));
    private final NumberFormat integerFormat = NumberFormat.getNumberInstance(new Locale("es", "AR"));
    private final ProductService productService = new ProductServiceImpl();
    private final SaleService saleService = new SaleServiceImpl();

    @FXML private ComboBox<String> periodComboBox;
    @FXML private Label dailyRevenueLabel;
    @FXML private Label salesCountLabel;
    @FXML private Label inventoryValueLabel;
    @FXML private Label productCountLabel;
    @FXML private Label criticalStockCountLabel;
    @FXML private Label chartDescriptionLabel;
    @FXML private Label statusLabel;
    @FXML private LineChart<String, Number> salesChart;
    @FXML private CategoryAxis chartXAxis;
    @FXML private NumberAxis chartYAxis;
    @FXML private TableView<Product> criticalStockTable;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Integer> minimumStockColumn;

    private List<HeaderSaleWithClient> sales = new ArrayList<>();

    @FXML
    public void initialize() {
        integerFormat.setMaximumFractionDigits(0);
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        minimumStockColumn.setCellValueFactory(new PropertyValueFactory<>("minStock"));
        periodComboBox.setItems(FXCollections.observableArrayList(LAST_7_DAYS, LAST_30_DAYS, CURRENT_MONTH));
        periodComboBox.setValue(LAST_7_DAYS);
        periodComboBox.valueProperty().addListener((observable, previous, selected) -> refreshChart());
        loadDashboard();
    }

    private void loadDashboard() {
        try {
            List<Product> products = productService.getAll();
            sales = saleService.getAllHeaders();
            updateInventory(products);
            updateTodaySales();
            refreshChart();
        } catch (RuntimeException ex) {
            statusLabel.setText("No se pudo cargar el resumen. Verificá la conexión con la base de datos.");
            statusLabel.setManaged(true);
            statusLabel.setVisible(true);
        }
    }

    private void updateInventory(List<Product> products) {
        double inventoryValue = products.stream()
                .mapToDouble(product -> product.getPurchasePrice() * product.getStock())
                .sum();
        List<Product> criticalProducts = products.stream()
                .filter(product -> product.getStock() <= product.getMinStock())
                .sorted(Comparator.comparingInt(product -> product.getStock() - product.getMinStock()))
                .collect(Collectors.toList());

        inventoryValueLabel.setText(currency(inventoryValue));
        productCountLabel.setText(products.size() + (products.size() == 1 ? " producto en total" : " productos en total"));
        criticalStockCountLabel.setText(criticalProducts.size() + (criticalProducts.size() == 1 ? " producto crítico" : " productos críticos"));
        criticalStockTable.setItems(FXCollections.observableArrayList(criticalProducts));
    }

    private void updateTodaySales() {
        LocalDate today = LocalDate.now();
        List<HeaderSaleWithClient> todaySales = sales.stream()
                .filter(sale -> sale.getHeader().getDate().toLocalDate().equals(today))
                .collect(Collectors.toList());
        double total = todaySales.stream().mapToDouble(sale -> sale.getHeader().getTotal()).sum();
        dailyRevenueLabel.setText(currency(total));
        salesCountLabel.setText(todaySales.size() + (todaySales.size() == 1 ? " venta registrada" : " ventas registradas"));
    }

    private void refreshChart() {
        if (periodComboBox.getValue() == null) return;
        LocalDate end = LocalDate.now();
        LocalDate start;
        if (LAST_30_DAYS.equals(periodComboBox.getValue())) {
            start = end.minusDays(29);
        } else if (CURRENT_MONTH.equals(periodComboBox.getValue())) {
            start = end.withDayOfMonth(1);
        } else {
            start = end.minusDays(6);
        }

        Map<LocalDate, Double> totals = new LinkedHashMap<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) totals.put(day, 0d);
        for (HeaderSaleWithClient sale : sales) {
            LocalDate date = sale.getHeader().getDate().toLocalDate();
            if (!date.isBefore(start) && !date.isAfter(end)) {
                totals.merge(date, (double) sale.getHeader().getTotal(), Double::sum);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        totals.forEach((date, total) -> series.getData().add(new XYChart.Data<>(DAY_FORMAT.format(date), total)));
        salesChart.getData().setAll(series);
        chartYAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
            @Override public String toString(Number value) { return integerFormat.format(value.doubleValue()); }
            @Override public Number fromString(String value) { return 0; }
        });
        chartDescriptionLabel.setText("Ventas diarias · " + periodComboBox.getValue().toLowerCase());
    }

    private String currency(double value) {
        return "ARS " + integerFormat.format(value);
    }
}
