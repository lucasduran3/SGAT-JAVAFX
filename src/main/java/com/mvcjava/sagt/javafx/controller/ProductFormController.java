/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.auth.SessionContext;
import com.mvcjava.sagt.javafx.dao.model.Category;
import com.mvcjava.sagt.javafx.dao.model.Product;
import com.mvcjava.sagt.javafx.dao.model.Supplier;
import com.mvcjava.sagt.javafx.dto.ProductWithRelations;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import com.mvcjava.sagt.javafx.viewmodel.ProductViewModel;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;

/**
 *
 * @author lucas
 */
public class ProductFormController {
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
    private TextField modelField;
    @FXML
    private TextField purchasePriceField;
    @FXML 
    private TextField salePriceField;
    @FXML
    private TextField stockField;
    @FXML 
    private TextField minStockField;
    
    //Error labels
    @FXML
    private Label errorName;
    @FXML
    private Label errorBrand;
    @FXML
    private Label errorModel;
    @FXML
    private Label errorPurchasePrice;
    @FXML
    private Label errorSalePrice;
    @FXML
    private Label errorStock;
    @FXML
    private Label errorMinStock;
    @FXML
    private Label errorSupplier;
    
    
    private Map<Category, CheckBox> checkboxMap = new HashMap<>();
    
    public static ProductViewModel showProductForm(Set<Category> avaibleCategories, List<Supplier> avaibleSuppliers, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(ProductFormController.class.getResource("/com/mvcjava/sagt/javafx/view/productForm.fxml"));
            
            ProductFormController controller = new ProductFormController();
            loader.setController(controller);
            
            DialogPane dialogPane = loader.load();
            Dialog<ProductViewModel> dialog = new Dialog();
            dialog.initOwner(owner);
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Formulario de producto");
            
            controller.setData(avaibleCategories, avaibleSuppliers);
            
            //Interceptar el evento para evitar que el dialogo se cierre
            Button btnOK = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            btnOK.addEventFilter(ActionEvent.ACTION, e -> {
                //Si la validacion falla se consume el evento   
                if(!controller.isValid()) {
                    e.consume();
                }
            });
            
            //Se llama si el evento no fue consumido
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return controller.createViewModel();
                }
                return null;
            });
            
            return dialog.showAndWait().orElse(null);
            
        } catch (IOException ex) {
            ex.printStackTrace();
            AlertUtils.showError("Error al abrir formulario de producto: " + ex.getMessage());
            return null;
        }
    }
    
    private void setData(Set<Category> avaibleCategories, List<Supplier> avaibleSuppliers) {
        checkboxMap.clear();
        categoriesContainer.getChildren().clear();
        
        //Crear checbox de categorias
        for (Category category : avaibleCategories) {
            CheckBox checkbox = new CheckBox(category.toString());
            categoriesContainer.getChildren().add(checkbox);
            checkboxMap.put(category, checkbox); //Quizas se puede usar setUserData en vez de crear un mapa
        }
        
        //Crear items de comboBox proveedores
        supplierComboBox.getItems().setAll(avaibleSuppliers);
        clearErrors();
    }
    
    private ProductViewModel createViewModel() {
        UUID id = UUID.randomUUID();
        String name = nameField.getText();
        String brand = brandField.getText();
        String model = modelField.getText();
        float purchasePrice = Float.parseFloat(purchasePriceField.getText());
        float salePrice = Float.parseFloat(salePriceField.getText());
        int stock = Integer.parseInt(stockField.getText());
        int minStock = Integer.parseInt(minStockField.getText());
        Supplier supplier = supplierComboBox.getValue();
        
        Set<Category> selectedCategories = new HashSet<>();
        checkboxMap.forEach((k, v) -> {
            if (v.isSelected()) {
                selectedCategories.add(k);
            }
        });
        
        Product product = new Product(
                id,
                name,
                brand,
                model, 
                purchasePrice, 
                salePrice, 
                stock, 
                minStock, 
                supplier.getId(),
                SessionContext.getCurrentUserId()
        );
        
        ProductWithRelations dto = new ProductWithRelations
        (
                product, 
                SessionContext.getCurrentUserName(),
                selectedCategories
        );
        
        return new ProductViewModel(dto, supplier);
    }
    
    private void clearErrors() {
        errorName.setText("");
        errorBrand.setText("");
        errorModel.setText("");
        errorPurchasePrice.setText("");
        errorSalePrice.setText("");
        errorStock.setText("");
        errorMinStock.setText("");
        errorSupplier.setText("");
    }
    
    private boolean isValid() {
        clearErrors();
        boolean isValid = true;
        
        //Validaciones de texto
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorName.setText("El nombre es obligatorio");
            isValid = false;
        } else if (nameField.getText().length() < 2) {
            errorName.setText("Mínimo 2 caracteres.");
            isValid = false;
        }
        
        if (brandField.getText() == null || brandField.getText().trim().isEmpty()) {
            errorBrand.setText("La marca es obligatria");
            isValid = false;
        }
        
        if (modelField.getText() == null || modelField.getText().trim().isEmpty()) {
            errorModel.setText("El modelo es obligatorio");
            isValid = false;
        }
        
        //Validaciones numericas
        float purchasePrice = -1;
        try {
            purchasePrice = Float.parseFloat(purchasePriceField.getText());
            if (purchasePrice < 0) {
                errorPurchasePrice.setText("No puede ser negativo");
                isValid = false;
            }
        } catch (NumberFormatException ex) {
            errorPurchasePrice.setText("Debe ser un número válido");
            isValid = false;
        }
        
        float salePrice = -1;
        try {
            salePrice = Float.parseFloat(salePriceField.getText());
            if (salePrice < 0) {
                errorSalePrice.setText("No puede ser negativo");
                isValid = false;
            }
        } catch (NumberFormatException ex) {
            errorSalePrice.setText("Debe ser un número válido");
            isValid = false;
        }
        
        if(purchasePrice >= 0 && salePrice >= 0 && purchasePrice > salePrice) {
            errorSalePrice.setText("El precio de venta debe ser mayor al de compra");
            isValid = false;
        }
        
        try {
            int stock = Integer.parseInt(stockField.getText());
            if (stock < 0) {
                errorStock.setText("No puede ser negativo");
                isValid = false;
            }
        } catch (NumberFormatException ex) {
            errorStock.setText("Debe ser un número válido");
        }
        
        try {
            int minStock = Integer.parseInt(minStockField.getText());
            if (minStock < 0) {
                errorMinStock.setText("No puede ser negativo");
                isValid = false;
            }
        } catch (NumberFormatException ex) {
            errorMinStock.setText("Debe ser un número válido");
        }
        
        //Validacion de supplier
        if (supplierComboBox.getValue() == null) {
            errorSupplier.setText("Seleccione un proveedor");
            isValid = false;
        }
        
        return isValid;
    }
}
