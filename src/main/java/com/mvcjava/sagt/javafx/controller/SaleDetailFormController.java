/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.dao.model.Product;
import com.mvcjava.sagt.javafx.dto.SaleDetailFormData;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;

/**
 *
 * @author lucas
 */
public class SaleDetailFormController {
    @FXML
    private Hyperlink productLink;
    @FXML
    private TextField unitPriceField;
    @FXML
    private TextField ammountField;
    @FXML
    private TextField subtotalField;
    
    @FXML
    private Label errorProduct;
    @FXML
    private Label errorUnitPrice;
    @FXML
    private Label errorAmmount;
    
    private List<Product> avaibleProducts;
    private Window owner;
    
    
    public static SaleDetailFormData showForm(Window owner, List<Product> products, String billNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(SaleDetailFormController.class.getResource("/com/mvcjava/sagt/javafx/view/saleDetailForm.fxml"));
            
            SaleDetailFormController controller = new SaleDetailFormController();
            loader.setController(controller);
            
            DialogPane dialogPane = loader.load();
            Dialog<SaleDetailFormData> dialog = new Dialog();
            dialog.setTitle("Agregar item - Venta " + billNumber);
            dialog.initOwner(owner);
            dialog.setDialogPane(dialogPane);
            
            controller.setData(products, owner);
            
            Button btnOK = (Button) dialogPane.lookupButton(ButtonType.OK);
            btnOK.addEventFilter(ActionEvent.ACTION, e -> {
                if (!controller.isValid()) {
                    e.consume();
                }
            });
            
            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    return controller.buildResult();
                }
                return null;
            });
            
            return dialog.showAndWait().orElse(null);
        } catch (IOException ex) {
            ex.printStackTrace();
            AlertUtils.showError("Error al abrir el formulario de ítem: " + ex.getMessage());
            return null;
        }
    }
    
    @FXML
    public void initialize() {
        this.avaibleProducts = new ArrayList<>();
        
        productLink.setOnAction(e -> {
            openProductSelectDialog();
        });
        
        ammountField.textProperty().addListener((obs, oldVal, newVal) -> recalcSubtotal());
        
        clearErrors();
    }
    
    private void openProductSelectDialog() {
        if (avaibleProducts.isEmpty()) {
            AlertUtils.showError("No hay productos disponibles para mostrar.");
            return;
        }
        
        UUID currentProductId = null;
        
        Product chosen = RadioDialogController.showDialog(
                owner,
                "Seleccionar Producto",
                avaibleProducts,
                Product::getName,
                Product::getId,
                currentProductId
        );
        
        if (chosen == null) return;
        
        productLink.setUserData(chosen);
        productLink.setText(chosen.getName() + " - " + chosen.getBrand() + " - " + chosen.getModel());
        
        unitPriceField.setText(String.format("%.2f", chosen.getSalePrice()));
        recalcSubtotal();
    }
    
    private void setData(List<Product> products, Window owner) {
        this.avaibleProducts.addAll(products);
        this.owner = owner;
    }
    
    private void recalcSubtotal() {
        try {
            float price = Float.parseFloat(unitPriceField.getText()
                    .replace(".", "")
                    .replace(",", ".")
                    .trim());
            int ammount = Integer.parseInt(ammountField.getText().trim());
            subtotalField.setText(String.format("%.2f", price * ammount));
        } catch (NumberFormatException ex ) {
            subtotalField.clear();
        }
    }
    
    private void clearErrors() {
        errorProduct.setText("");
        errorUnitPrice.setText("");
        errorAmmount.setText("");
    }
    
    private boolean isValid() {
        clearErrors();
        boolean valid = true;
        
        if (productLink.getUserData() == null || !(productLink.getUserData() instanceof Product)) {
            errorProduct.setText("Seleccione un producto válido.");
            valid = false;
        }
        
        float unitPrice = -1;
        try {
            String value = unitPriceField.getText().replace(".", "").replace(",", "");
            unitPrice = Float.parseFloat(value.trim());
            if (unitPrice <= 0) {
                errorUnitPrice.setText("El precio debe ser mayor a 0.");
                valid = false;
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            errorUnitPrice.setText("Precio inválido.");
            valid = false;
        }
        
        try {
            int ammount = Integer.parseInt(ammountField.getText().trim());
            if (ammount <= 0) {
                errorAmmount.setText("La cantidad debe ser mayor a 0.");
                valid = false;
            }
        } catch (NumberFormatException ex) {
            errorAmmount.setText("Ingrese un número entero válido.");
            valid = false;
        }
        
        return valid;
    }
    
    private SaleDetailFormData buildResult() {
        Product product = (Product)productLink.getUserData();
        float price = Float.parseFloat(unitPriceField.getText().replace(".", "").replace(",", ".").trim());
        int ammount = Integer.parseInt(ammountField.getText().trim());
        float subtotal = price * ammount;
        
        return new SaleDetailFormData(product, price, ammount, subtotal);
    }
}
