/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.filter;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TextField;

/**
 *
 * @author lucas
 */
public final class FilterableTableHelper<T> {
    private final FilteredList<T> filteredList;
    private final FilterState<T> filterState;
    private final BiPredicate<T, String> textMatchFn;
    
    private Predicate<T> lastPanelPredicate = null;
    private String lastSearchText = "";
    
    public FilterableTableHelper(
            ObservableList<T> sourceList, 
            TextField searchField, 
            FilterState<T> filterState, 
            BiPredicate<T, String> textMatchFn) {
        
        this.filterState = filterState;
        this.textMatchFn = textMatchFn;
        this.filteredList = new FilteredList<>(sourceList, vm -> true);
        
        wireSearchField(searchField);
        wirePanelState();
    }
    
    public FilteredList<T> getFilteredList() {
        return filteredList;
    }
    
    public void setAll(List<T> newItems) {
        ObservableList<T> source = (ObservableList<T>) filteredList.getSource();
        source.setAll(newItems);
    }
    
    private void wireSearchField(TextField searchField) {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            lastSearchText = newVal == null ? "" : newVal.trim().toLowerCase();
            recompute();
        });
    }
    
    private void wirePanelState() {
        filterState.combinedPredicateProperty().addListener((obs, oldVal, newVal) -> {
            lastPanelPredicate = newVal;
            recompute();
        });
    }
    
    private void recompute() {
        filteredList.setPredicate(vm -> {
            if (!lastSearchText.isEmpty() && !textMatchFn.test(vm, lastSearchText)) {
                return false;
            }
            
            if (lastPanelPredicate != null && !lastPanelPredicate.test(vm)) {
                return false;
            }
            return true;
        });
    }
}
