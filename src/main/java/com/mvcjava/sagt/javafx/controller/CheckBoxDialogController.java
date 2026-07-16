package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.util.AlertUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class CheckBoxDialogController<T> {

    @FXML private VBox checkBoxContainer;
    @FXML private Label instructionLabel;
    @FXML private Label selectionLabel;
    @FXML private TextField searchField;
    
    private Function<T, String> nameExtractor;

    private Map<T, CheckBox> checkBoxMap = new LinkedHashMap<>();

    public static <T> List<T> showDialog(
            Window owner,
            String windowTitle,
            String headerText,
            String instructionText,
            List<T> availableItems,
            List<T> currentItems,
            Function<T, String> nameExtractor
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(CheckBoxDialogController.class.getResource(
                    "/com/mvcjava/sagt/javafx/view/checkBoxDialog.fxml"));
            
            CheckBoxDialogController<T> controller = new CheckBoxDialogController<>();
            loader.setController(controller);

            DialogPane dialogPane = loader.load();
            
            Dialog<List<T>> dialog = new Dialog<>();
            dialog.setTitle(windowTitle);
            dialog.initOwner(owner);
            dialog.setDialogPane(dialogPane);
            dialog.setHeaderText(headerText);

            controller.nameExtractor = nameExtractor;
            controller.populate(availableItems, currentItems, instructionText);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return controller.getSelectedItems();
                }
                return null; // Si es Cancel o cerrar ventana
            });

            return dialog.showAndWait().orElse(null);

        } catch (IOException ex) {
            ex.printStackTrace();
            AlertUtils.showError("Error al abrir diálogo: " + ex.getMessage());
            return null;
        }
    }
    
    @FXML
    public void initialize() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterRows(newText == null ? "" : newText.trim().toLowerCase());
        });
    }
    
    private void populate(List<T> items, List<T> currentItems, String instructionText) {
        checkBoxContainer.getChildren().clear();
        checkBoxMap.clear();
        
        for (T item : items) {
            HBox row = buildRow(item, currentItems);
            checkBoxContainer.getChildren().add(row);
        }
        
        instructionLabel.setText(instructionText);
        
        updateSelectionLabel(currentItems.size());
    }
    
    private HBox buildRow(T item, List<T> currentItems) {
        String name = nameExtractor.apply(item);
        
        CheckBox check = new CheckBox();
        checkBoxMap.put(item, check);
        check.setMaxWidth(40);
        check.setMaxHeight(40);
        
        if (!currentItems.isEmpty() && currentItems.contains(item)) {
            check.setSelected(true);
            updateSelectionLabel(currentItems.size());
        }
        
        Label nameLabel = new Label(name);
        nameLabel.setPrefWidth(250);
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-text-fill: #2d3436;");
        
        HBox row = new HBox(10, check, nameLabel);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setUserData(item);
        applyRowStyle(row, false);
        
        check.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                currentItems.add((T)row.getUserData());
            } else {
                currentItems.remove((T)row.getUserData());
            }
            updateSelectionLabel(currentItems.size());
        });
        
        row.setOnMouseClicked(e -> check.setSelected(true));
        row.setOnMouseEntered(e -> applyRowStyle(row, true));
        row.setOnMouseExited(e -> applyRowStyle(row, false));
        
        return row;
    }
    
    private void filterRows(String query) {
        checkBoxContainer.getChildren().forEach(node -> {
            if (!(node instanceof HBox)) return;
            
            HBox row = (HBox) node;
            
            Object data = row.getUserData();
            boolean matches = query.isEmpty() ||
                    nameExtractor.apply((T) data).toLowerCase().contains(query);
            
            row.setVisible(matches);
            row.setManaged(matches);
        });
    }
    
    private void updateSelectionLabel(int count) {
        selectionLabel.setText("Seleccionados: " + Integer.toString(count));
        selectionLabel.setStyle(
            "-fx-text-fill: #1E88E5; -fx-font-style: normal; -fx-font-weight: bold;");
    }
    
    private List<T> getSelectedItems() {
        List<T> selected = new ArrayList<>();
        checkBoxMap.forEach((item, checkbox) -> {
            if (checkbox.isSelected()) {
                selected.add(item);
            }
        });
        return selected;
    }
    
    private static void applyRowStyle(HBox row, boolean hover) {
        String base = "-fx-border-color: transparent transparent #E9ECEF transparent; "
                    + "-fx-border-width: 1;";
        row.setStyle(hover
            ? "-fx-background-color: #E3F2FD; " + base + " -fx-cursor: hand;"
            : base);
    }
}