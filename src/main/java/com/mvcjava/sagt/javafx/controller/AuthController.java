/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.App;
import com.mvcjava.sagt.javafx.async.AuthAsyncService;
import com.mvcjava.sagt.javafx.auth.AuthResult;
import com.mvcjava.sagt.javafx.auth.SessionContext;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

/**
 *
 * @author lucas
 */
public class AuthController {
    @FXML private ToggleButton loginTab;
    @FXML private ToggleButton registerTab;
    
    @FXML private VBox loginPanel;
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginEmailError;
    @FXML private Label loginPasswordError;
    @FXML private Label loginGeneralError;
    @FXML private Button loginBtn;
    @FXML private ProgressIndicator loginProgress;
    
    @FXML private VBox registerPanel;
    @FXML private TextField regName;
    @FXML private TextField regLastname;
    @FXML private TextField regEmail;
    @FXML private TextField regPassword;
    @FXML private TextField regPasswordConfirm;
    @FXML private Label regNameError;
    @FXML private Label regLastnameError;
    @FXML private Label regEmailError;
    @FXML private Label regPasswordError;
    @FXML private Label regPasswordConfirmError;
    @FXML private Label regGeneralError;
    @FXML private Button registerBtn;
    @FXML private ProgressIndicator registerProgress;
    
    private AuthAsyncService authService;
    
    @FXML
    public void initialize() {
        authService = new AuthAsyncService();
        
        loginPassword.setOnAction(e -> handleLogin());
        loginEmail.setOnAction(e -> loginPassword.requestFocus());
        
        regPasswordConfirm.setOnAction(e -> handleRegister());
        
        clearAllErrors();
    }
    
    @FXML 
    public void handleTabChange() { 
        boolean showLogin = loginTab.isSelected();
        
        loginPanel.setVisible(showLogin);
        loginPanel.setManaged(showLogin);
        
        registerPanel.setVisible(!showLogin);
        registerPanel.setManaged(!showLogin);
        
        loginTab.setSelected(showLogin);
        registerTab.setSelected(!showLogin);
        
        clearAllErrors();
    }
    
    @FXML
    public void handleLogin() {
        clearLoginErrors();
        
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText();
        
        boolean valid = true;
        
        if (email.isBlank()) {
            loginEmailError.setText("El email es obligatorio.");
            valid = false;
        } else if (!BasicStringValidator.isValidEmail(email)) {
            loginEmailError.setText("Formato de email inválido.");
            valid = false;
        }
        if (password.isEmpty()) {
            loginPasswordError.setText("La contraseña es obligatoria.");
            valid = false;
        }
        if (!valid) return;
        
        setLoginLoading(true);
        
        if (authService.isRunning()) {
            authService.cancel();
        }
        authService.reset();
        authService.configureLogin(email, password);
        
        authService.setOnSucceeded(e -> {
            setLoginLoading(false);
            AuthResult result = authService.getValue();
            onAuthSuccess(result);
        });
        
        authService.setOnFailed(e -> {
            setLoginLoading(false);
            Throwable ex = authService.getException();
            loginGeneralError.setText(ex != null ? ex.getMessage() : "Error desconocido al iniciar sesión.");
        });
        
        authService.start();
    }
    
    @FXML
    public void handleRegister() {
        System.out.println("Handle register btn pressed");
        clearRegisterErrors();
        
        String name = regName.getText().trim();
        String lastname = regLastname.getText().trim();
        String email = regEmail.getText().trim();
        String password = regPassword.getText();
        String passConf = regPasswordConfirm.getText();
        
        boolean valid = true;
        
        if (!name.isBlank() && lastname.length() < 2) {
            regNameError.setText("Mínimo 2 carácteres.");
            valid = false;
        }
        if (!lastname.isBlank() && lastname.length() < 2) {
            regLastnameError.setText("Mínimo 2 carácteres.");
            valid = false;
        }
        
        if (email.isBlank()) {
            regEmailError.setText("El email es obligatorio.");
            valid = false;
        } else if (!BasicStringValidator.isValidEmail(email)) {
            regEmailError.setText("Formato de email inválido.");
            valid = false;
        }
        
        if (password.length() < 8) {
            regPasswordError.setText("Mínimo 8 carácteres.");
            valid = false;
        }
        
        if (!password.equals(passConf)) {
            regPasswordConfirmError.setText("Las contraseñas no coinciden.");
            valid = false;
        }
        
        if(!valid) return;
        
        setRegisterLoading(true);
        
        if(authService.isRunning()) {
            authService.cancel();
        }
        authService.reset();
        System.out.println("Información desde controller: " + name + "-" + lastname);
        authService.configureRegister(email, password, name, lastname);
        
        authService.setOnSucceeded(e -> {
            setRegisterLoading(false);
            AuthResult result = authService.getValue();
            onAuthSuccess(result);
        });
        
        authService.setOnFailed(e -> {
            setRegisterLoading(false);
            Throwable ex = authService.getException();
            regGeneralError.setText(ex != null ? ex.getMessage() : "Error desconocido al registrarse.");
        });
        
        authService.start();
    }
    
    private void onAuthSuccess(AuthResult result) {
        SessionContext.setCurrentUser(result);
        
        try {
            System.out.println("Registro exitoso, dirigiendo a main");
            App.showMainView();
        } catch (Exception ex) {
            ex.printStackTrace();
            loginGeneralError.setText("Error al cargar la aplicación: " + ex.getMessage());
        }
    }
    
    //helpers
    private void setLoginLoading(boolean loading) {
        loginBtn.setDisable(loading);
        loginProgress.setVisible(loading);
        loginProgress.setManaged(loading);
    }
    
    private void setRegisterLoading(boolean loading) {
        registerBtn.setDisable(loading);
        registerProgress.setVisible(loading);
        registerProgress.setManaged(loading);
    }
    
    private void clearLoginErrors() {
        loginEmailError.setText("");
        loginPasswordError.setText("");
        loginGeneralError.setText("");
    }
    
    private void clearRegisterErrors() {
        regNameError.setText("");
        regLastnameError.setText("");
        regEmailError.setText("");
        regPasswordError.setText("");
        regPasswordConfirmError.setText("");
        regGeneralError.setText("");
    }
 
    private void clearAllErrors() {
        clearLoginErrors();
        clearRegisterErrors();
    }
}
