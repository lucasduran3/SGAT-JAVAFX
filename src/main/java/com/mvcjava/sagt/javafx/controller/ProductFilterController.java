/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.dao.model.Category;
import com.mvcjava.sagt.javafx.dao.model.Supplier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 *
 * @author lucas
 */
public class ProductFilterController {
    @FXML 
    private VBox categoriesContainer;

    //Input fields
    @FXML
    private ComboBox<Supplier> supplierComboBox;
    @FXML
    private TextField nameField;
    @FXML
    private TextField brandField;
    @FXML
    private TextField minPurchasePriceField;
    @FXML
    private TextField maxPurchasePriceField;
    @FXML 
    private TextField minSalePriceField;
    @FXML 
    private TextField maxSalePriceField;
    @FXML
    private CheckBox stockField;

    //Error labels
    @FXML
    private Label errorPurchasePrice;
    @FXML
    private Label errorSalePrice;
    
    private Map<Category, CheckBox> checkBoxMap = new HashMap<>();
    
    private void setData(Set<Category> avaibleCategories, List<Supplier> avaibleSuppliers) {
        checkBoxMap.clear();
        categoriesContainer.getChildren().clear();
        
        for (Category category : avaibleCategories) {
            CheckBox checkbox = new CheckBox(category.toString());
            categoriesContainer.getChildren().add(checkbox);
            checkBoxMap.put(category, checkbox);
        }
        
        //Agregar categoria sin definir que seria null
        
        supplierComboBox.getItems().addAll(avaibleSuppliers);
        clearErrors();
    }
    
    private void clearErrors() {
        errorPurchasePrice.setText("");
        errorSalePrice.setText("");
    }
    
    private boolean isValid() {
        return true;
    }
    
}
