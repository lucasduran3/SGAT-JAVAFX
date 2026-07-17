package com.mvcjava.sagt.javafx.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public final class ListPaginationHelper<T> {

    private final Consumer<List<T>> pageRenderer;
    private final Button previousButton;
    private final Button nextButton;
    private final Label pageLabel;
    private final int pageSize;

    private List<T> sourceItems = Collections.emptyList();
    private int currentPage = 1;

    public ListPaginationHelper(
            Consumer<List<T>> pageRenderer,
            Button previousButton,
            Button nextButton,
            Label pageLabel,
            int pageSize) {

        this.pageRenderer = Objects.requireNonNull(pageRenderer, "El renderizador es obligatorio.");
        this.previousButton = Objects.requireNonNull(previousButton, "El boton anterior es obligatorio.");
        this.nextButton = Objects.requireNonNull(nextButton, "El boton siguiente es obligatorio.");
        this.pageLabel = Objects.requireNonNull(pageLabel, "La etiqueta de pagina es obligatoria.");

        if (pageSize < 1) {
            throw new IllegalArgumentException("El tamano de pagina debe ser mayor que cero.");
        }
        this.pageSize = pageSize;

        previousButton.setOnAction(event -> showPreviousPage());
        nextButton.setOnAction(event -> showNextPage());
        refresh();
    }

    public void setItems(List<T> items) {
        sourceItems = new ArrayList<>(Objects.requireNonNull(items, "La lista origen es obligatoria."));
        resetToFirstPage();
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

    public void showPageContaining(Predicate<T> matcher) {
        Objects.requireNonNull(matcher, "El criterio de busqueda es obligatorio.");

        for (int index = 0; index < sourceItems.size(); index++) {
            if (matcher.test(sourceItems.get(index))) {
                currentPage = (index / pageSize) + 1;
                refresh();
                return;
            }
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
        pageRenderer.accept(Collections.unmodifiableList(new ArrayList<>(sourceItems.subList(fromIndex, toIndex))));

        previousButton.setDisable(currentPage == 1);
        nextButton.setDisable(currentPage == pageCount || sourceItems.isEmpty());
        pageLabel.setText("Pagina " + currentPage + " de " + pageCount);
    }
}
