/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.filter.FilterGroup;
import com.mvcjava.sagt.javafx.filter.FilterState;
import com.mvcjava.sagt.javafx.filter.FilterCriteria;
import java.io.IOException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/**
 *
 * @author lucas
 */
public class FilterPanelController<T> {
    @FXML private VBox groupsContainer;
    @FXML private Label activeCountLabel;
    @FXML private ScrollPane scrollPane;
    
    private Node root;
    private FilterState<T> filterState;
    private List<FilterGroup<T>> groups;
    
    //static factory
    public static <T> FilterPanelController<T> create(
            List<FilterGroup<T>> groups,
            FilterState<T> filterState
    ) throws IOException {
        
        FXMLLoader loader = new FXMLLoader(FilterPanelController.class.getResource(
        "/com/mvcjava/sagt/javafx/view/filterPanel.fxml"));
        
        FilterPanelController<T> controller = new FilterPanelController<>();
        loader.setController(controller);
        controller.root = loader.load();
        controller.filterState = filterState;
        controller.groups = groups;
        
        controller.buildUI();
        return controller;
    }
    
    public Node getRoot() {return root;}
    
    public void clearAll() {
        filterState.clearAll();
        buildUI();
        refreshBadge();
    }
    
    private void buildUI() {
        groupsContainer.getChildren().clear();
        
        for (FilterGroup<T> group: groups) {
            groupsContainer.getChildren().add(buildGroupSelection(group));
        }
        
        refreshBadge();
    }
    
    private Node buildGroupSelection(FilterGroup<T> group) {
        VBox section = new VBox(6);
        section.getStyleClass().add("filter-section");
        
        Label title = new Label(group.getTitle());
        title.getStyleClass().add("filter-section-title");
        section.getChildren().add(title);
        
        if (group.isSingle()) {
            buildSingleSelect(section, group);
        } else {
            buildMultiSelect(section, group);
        }
        
        return section;
    }
    
    private void buildSingleSelect(VBox container, FilterGroup<T> group) {
        ToggleGroup toggleGroup = new ToggleGroup();
        
        RadioButton allRadio = new RadioButton("Todos");
        allRadio.getStyleClass().add("filter-radio");
        allRadio.setToggleGroup(toggleGroup);
        allRadio.setSelected(!hasActiveInGroup(group));
 
        allRadio.setOnAction(e -> {
            clearGroup(group);
            refreshBadge();
        });
        container.getChildren().add(allRadio);
 
        for (FilterCriteria<T> criterion : group.getCriteria()) {
            RadioButton radio = new RadioButton(criterion.getDisplayName());
            radio.getStyleClass().add("filter-radio");
            radio.setToggleGroup(toggleGroup);
            radio.setSelected(filterState.isActive(group, criterion));
 
            radio.setOnAction(e -> {
                if (radio.isSelected()) {
                    filterState.setCriterion(group, criterion);
                } else {
                    filterState.clearCriterion(group, criterion);
                }
                refreshBadge();
            });
 
            container.getChildren().add(radio);
        }
    }
    
    private void buildMultiSelect(VBox container, FilterGroup<T> group) {
        for (FilterCriteria<T> criterion : group.getCriteria()) {
            CheckBox checkbox = new CheckBox(criterion.getDisplayName());
            checkbox.getStyleClass().add("filter-checkbox");
            checkbox.setSelected(filterState.isActive(group, criterion));
 
            checkbox.setOnAction(e -> {
                if (checkbox.isSelected()) {
                    filterState.setCriterion(group, criterion);
                } else {
                    filterState.clearCriterion(group, criterion);
                }
                refreshBadge();
            });
 
            container.getChildren().add(checkbox);
        }
    }
    
    private boolean hasActiveInGroup(FilterGroup<T> group) {
        return group.getCriteria().stream()
                .anyMatch(c -> filterState.isActive(group, c));
    }
 
    /** Clears all active criteria that belong to this group. */
    private void clearGroup(FilterGroup<T> group) {
        group.getCriteria().forEach(c -> filterState.clearCriterion(group, c));
    }
 
    /** Updates the "N filtros activos" badge. */
    private void refreshBadge() {
        long count = filterState.getActiveCriteria().values().stream()
                .mapToLong(m -> m.size())
                .sum();
 
        if (count == 0) {
            activeCountLabel.setText("Sin filtros activos");
            activeCountLabel.getStyleClass().removeAll("filter-badge-active");
        } else {
            activeCountLabel.setText(count + (count == 1 ? " filtro activo" : " filtros activos"));
            if (!activeCountLabel.getStyleClass().contains("filter-badge-active")) {
                activeCountLabel.getStyleClass().add("filter-badge-active");
            }
        }
    }
}
