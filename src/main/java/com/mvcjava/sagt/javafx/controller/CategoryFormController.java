/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.dao.model.Category;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import com.mvcjava.sagt.javafx.viewmodel.CategoryViewModel;
import java.io.IOException;
import java.util.UUID;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;

/**
 *
 * @author lucas
 */
public class CategoryFormController {
    @FXML
    private TextField nameField;
    @FXML
    private Label errorName;
    
    @FXML
    public void initialize() {
        
    }
    
    public static CategoryViewModel showForm(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(CategoryFormController.class.getResource("/com/mvcjava/sagt/javafx/view/categoryForm.fxml"));
            
            CategoryFormController controller = new CategoryFormController();
            loader.setController(controller);
            
            DialogPane dialogPane = loader.load();
            Dialog<CategoryViewModel> dialog = new Dialog();
            dialog.initOwner(owner);
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Formulario de categoria");
            
            Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            btnOk.addEventFilter(ActionEvent.ACTION, e -> {
                if (!controller.isValid()) {
                    e.consume();
                }
            });
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return controller.createViewModel();
                }
                return null;
            });
            
            return dialog.showAndWait().orElse(null);
            
        } catch (IOException ex) {
            ex.printStackTrace();
            AlertUtils.showError("Error al abrir formulario de categoria: " + ex.getMessage());
            return null;
        }
    }
    
    private CategoryViewModel createViewModel() {
        UUID id = UUID.randomUUID();
        String name = nameField.getText();
        
        Category category = new Category(id, name);
        return new CategoryViewModel(category);
    }
    
    private boolean isValid() {
        clearErrors();
        boolean isValid = true;
        
        try {
            String name = nameField.getText();
            BasicStringValidator.validate(name, 3, 30, "nombre");
        } catch (IllegalArgumentException ex) {
            isValid = false;
            errorName.setText(ex.getMessage());
        }
        
        return isValid;
    }
    
    private void clearErrors() {
        errorName.setText("");
    }
}
