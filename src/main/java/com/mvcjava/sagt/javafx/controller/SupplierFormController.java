/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.dao.model.Supplier;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import com.mvcjava.sagt.javafx.viewmodel.SupplierViewModel;
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

public class SupplierFormController {
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TextField webField;
    @FXML private TextField locationField;
    @FXML private TextField provinceField;

    @FXML private Label errorName;
    @FXML private Label errorPhone;
    @FXML private Label errorEmail;
    @FXML private Label errorAddress;
    @FXML private Label errorWeb;
    @FXML private Label errorLocation;
    @FXML private Label errorProvince;

    public static SupplierViewModel showSupplierForm(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SupplierFormController.class.getResource("/com/mvcjava/sagt/javafx/view/supplierForm.fxml"));

            SupplierFormController controller = new SupplierFormController();
            loader.setController(controller);

            DialogPane dialogPane = loader.load();
            Dialog<SupplierViewModel> dialog = new Dialog<>();
            dialog.initOwner(owner);
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Formulario de proveedor");

            // Interceptar OK para evitar cierre si la validación falla
            Button btnOK = (Button) dialogPane.lookupButton(ButtonType.OK);
            btnOK.addEventFilter(ActionEvent.ACTION, e -> {
                if (!controller.isValid()) {
                    e.consume();
                }
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return controller.buildViewModel();
                }
                return null;
            });

            return dialog.showAndWait().orElse(null);

        } catch (IOException ex) {
            ex.printStackTrace();
            AlertUtils.showError("Error al abrir formulario de proveedor: " + ex.getMessage());
            return null;
        }
    }

    private SupplierViewModel buildViewModel() {
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName(nameField.getText());
        supplier.setPhone(phoneField.getText());
        supplier.setEmail(emailField.getText());
        supplier.setAddress(addressField.getText());
        supplier.setWeb(webField.getText());
        supplier.setLocation(locationField.getText());
        supplier.setProvince(provinceField.getText());
        return new SupplierViewModel(supplier);
    }

    private void clearErrors() {
        errorName.setText("");
        errorPhone.setText("");
        errorEmail.setText("");
        errorAddress.setText("");
        errorWeb.setText("");
        errorLocation.setText("");
        errorProvince.setText("");
    }

    private boolean isValid() {
        clearErrors();
        boolean valid = true;

        // Nombre
        try {
            BasicStringValidator.validate(nameField.getText(), 3, 30, "nombre");
        } catch (IllegalArgumentException ex) {
            errorName.setText(ex.getMessage());
            valid = false;
        }

        // Teléfono
        try {
            BasicStringValidator.validate(phoneField.getText(), 8, 20, "teléfono");
            if (!BasicStringValidator.isValidPhone(phoneField.getText())) {
                throw new IllegalArgumentException("Formato de teléfono inválido.");
            }
        } catch (IllegalArgumentException ex) {
            errorPhone.setText(ex.getMessage());
            valid = false;
        }

        // Email
        try {
            BasicStringValidator.validate(emailField.getText(), 4, 255, "email");
            if (!BasicStringValidator.isValidEmail(emailField.getText())) {
                throw new IllegalArgumentException("Formato de email inválido.");
            }
        } catch (IllegalArgumentException ex) {
            errorEmail.setText(ex.getMessage());
            valid = false;
        }

        // Dirección
        try {
            BasicStringValidator.validate(addressField.getText(), 3, 100, "dirección");
        } catch (IllegalArgumentException ex) {
            errorAddress.setText(ex.getMessage());
            valid = false;
        }

        // Web
        try {
            BasicStringValidator.validate(webField.getText(), 4, 255, "web");
        } catch (IllegalArgumentException ex) {
            errorWeb.setText(ex.getMessage());
            valid = false;
        }

        // Localidad
        try {
            BasicStringValidator.validate(locationField.getText(), 3, 50, "localidad");
        } catch (IllegalArgumentException ex) {
            errorLocation.setText(ex.getMessage());
            valid = false;
        }

        // Provincia
        try {
            BasicStringValidator.validate(provinceField.getText(), 3, 50, "provincia");
        } catch (IllegalArgumentException ex) {
            errorProvince.setText(ex.getMessage());
            valid = false;
        }

        return valid;
    }
}
