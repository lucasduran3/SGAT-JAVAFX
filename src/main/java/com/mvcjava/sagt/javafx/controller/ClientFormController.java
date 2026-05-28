/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.auth.SessionContext;
import com.mvcjava.sagt.javafx.dao.model.Client;
import com.mvcjava.sagt.javafx.enums.ClientType;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import com.mvcjava.sagt.javafx.viewmodel.ClientViewModel;
import java.io.IOException;
import java.util.UUID;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;

/**
 *
 * @author lucas
 */
public class ClientFormController {
    //Input fields
    @FXML
    private TextField cuitField;
    
    @FXML
    private TextField companyNameField;
    
    @FXML
    private ComboBox<ClientType> typeComboBox;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField locationField;
    
    @FXML
    private TextField provinceField;
    
    @FXML
    private TextField addressField;
    
    //Error Labels
    @FXML
    private Label errorCuit;
    
    @FXML
    private Label errorCompanyName;
    
    @FXML
    private Label errorType;
    
    @FXML
    private Label errorPhone;
    
    @FXML
    private Label errorEmail;
    
    @FXML
    private Label errorLocation;
    
    @FXML
    private Label errorProvince;
    
    @FXML
    private Label errorAddress;
    
    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll(ClientType.values());
    }
    
    public static ClientViewModel showClientForm(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(ProductFormController.class.getResource("/com/mvcjava/sagt/javafx/view/clientForm.fxml"));
            
            ClientFormController controller = new ClientFormController();
            loader.setController(controller);
            
            DialogPane dialogPane = loader.load();
            Dialog<ClientViewModel> dialog = new Dialog();
            dialog.initOwner(owner);
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Formulario de cliente");
            
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
            AlertUtils.showError("Error al abrir formulario de cliente: " + ex.getMessage());
            return null;
        }
    }
    
    private ClientViewModel createViewModel() {
        UUID id = UUID.randomUUID();
        String cuit = cuitField.getText();
        String companyName = companyNameField.getText();
        ClientType type = typeComboBox.getValue();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String location = locationField.getText();
        String province = provinceField.getText();
        String address = addressField.getText();
        
        //Temporal
        SessionContext.setCurrentUser();
        
        Client client = new Client(id, cuit, companyName, type, phone, email, location, province, address);
        
        return new ClientViewModel(client);
    }
    
    private void clearErrors() {
        errorCuit.setText("");
        errorCompanyName.setText("");
        errorType.setText("");
        errorPhone.setText("");
        errorEmail.setText("");
        errorLocation.setText("");
        errorProvince.setText("");
        errorAddress.setText("");
    }
    
    private boolean isValid() {
        clearErrors();
        boolean isValid = true;
        
        try {
            String cuit = cuitField.getText();
            BasicStringValidator.validate(cuit, 11, 11, "cuit/cuil");
            if (!BasicStringValidator.isValidCuit(cuit)) throw new IllegalArgumentException("Formato de cuit inválido.");
        } catch (IllegalArgumentException ex) {
            errorCuit.setText(ex.getMessage());
            isValid = false;
        }
        
        try {
            String name = companyNameField.getText();
            BasicStringValidator.validate(name, 1, 100, "razón social");
        } catch (IllegalArgumentException ex) {
            isValid = false;
            errorCompanyName.setText(ex.getMessage());
        }
        
        if (typeComboBox.getValue() == null) {
            isValid = false;
            errorType.setText("Seleccione un tipo");
        }
        
        try {
            String phone = phoneField.getText();
            BasicStringValidator.validate(phone, 8, 20, "teléfono");
            if (!BasicStringValidator.isValidPhone(phone)) {
                throw new IllegalArgumentException("Formato de teléfono inválido");
            }
        } catch (IllegalArgumentException ex) {
            isValid = false;
            errorPhone.setText(ex.getMessage());
        }
        
        try {
            String email = emailField.getText();
            BasicStringValidator.validate(email, 4, 255, "email");
            if (!BasicStringValidator.isValidEmail(email)) {
                throw new IllegalArgumentException("Formato de email inválido");
            }
        } catch (IllegalArgumentException ex) {
            isValid = false;
            errorEmail.setText(ex.getMessage());
        }
        
        try {
            String location = locationField.getText();
            BasicStringValidator.validate(location, 3, 50, "localidad");
        } catch (IllegalArgumentException ex) {
            isValid = false;
            errorLocation.setText(ex.getMessage());
        }
        
        try {
            String province = provinceField.getText();
            BasicStringValidator.validate(province, 3, 50, "provincia");
        } catch (IllegalArgumentException ex) {
            isValid = false;
            errorProvince.setText(ex.getMessage());
        }
        
        try {
            String address = addressField.getText();
            BasicStringValidator.validate(address, 3, 100, "dirección");
        } catch (IllegalArgumentException ex) {
            isValid = false;
            errorAddress.setText(ex.getMessage());
        }
        
        return isValid;
    }
}
