package com.mvcjava.sagt.javafx.util;

import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public final class TablePaginationHelper<T> {

    private final ObservableList<T> sourceItems; 
    private final ObservableList<T> pageItems = FXCollections.observableArrayList(); 
    private final Button previousButton;
    private final Button nextButton;
    private final Label pageLabel;
    private final Label summaryLabel;
    private final int pageSize;

    private int currentPage = 1;

    public TablePaginationHelper(
            ObservableList<T> sourceItems,
            TableView<T> tableView,
            Button previousButton,
            Button nextButton,
            Label pageLabel,
            Label summaryLabel,
            int pageSize) {

        this.sourceItems = Objects.requireNonNull(sourceItems, "La lista origen es obligatoria.");
        Objects.requireNonNull(tableView, "La tabla es obligatoria.");
        this.previousButton = Objects.requireNonNull(previousButton, "El botón anterior es obligatorio.");
        this.nextButton = Objects.requireNonNull(nextButton, "El botón siguiente es obligatorio.");
        this.pageLabel = Objects.requireNonNull(pageLabel, "La etiqueta de página es obligatoria.");
        this.summaryLabel = Objects.requireNonNull(summaryLabel, "La etiqueta de resumen es obligatoria.");

        if (pageSize < 1) {
            throw new IllegalArgumentException("El tamaño de página debe ser mayor que cero.");
        }
        this.pageSize = pageSize;

        tableView.setItems(pageItems);
        previousButton.setOnAction(event -> showPreviousPage());
        nextButton.setOnAction(event -> showNextPage());
        sourceItems.addListener((ListChangeListener<T>) change -> refresh());

        refresh();
    }

    public void resetToFirstPage() {
        currentPage = 1;
        refresh();
    }

    public void showPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            refresh();
        }
    }

    public void showNextPage() {
        if (currentPage < getPageCount()) {
            currentPage++;
            refresh();
        }
    }

    private int getPageCount() {
        return Math.max(1, (int) Math.ceil((double) sourceItems.size() / pageSize));
    }

    private void refresh() {
        int pageCount = getPageCount();
        currentPage = Math.min(currentPage, pageCount);

        int fromIndex = Math.min((currentPage - 1) * pageSize, sourceItems.size());
        int toIndex = Math.min(fromIndex + pageSize, sourceItems.size());
        pageItems.setAll(sourceItems.subList(fromIndex, toIndex));

        previousButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == pageCount || sourceItems.isEmpty());
        pageLabel.setText("Página " + currentPage + " de " + pageCount);
        summaryLabel.setText(createSummary(fromIndex, toIndex));
    }

    private String createSummary(int fromIndex, int toIndex) {
        if (sourceItems.isEmpty()) {
            return "Sin resultados";
        }
        return "Mostrando " + (fromIndex + 1) + "-" + toIndex + " de " + sourceItems.size();
    }
}
