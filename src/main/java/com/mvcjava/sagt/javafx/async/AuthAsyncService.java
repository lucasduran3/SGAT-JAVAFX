/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.async;

import com.mvcjava.sagt.javafx.auth.AuthResult;
import com.mvcjava.sagt.javafx.service.impl.AuthServiceImpl;
import com.mvcjava.sagt.javafx.service.interfaces.AuthService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author lucas
 */
public class AuthAsyncService extends Service<AuthResult> {
    public enum Mode { LOGIN, REGISTER }
    
    private final AuthService authService;
    
    private Mode mode;
    private String email;
    private String password;
    private String name;
    private String lastname;
    
    public AuthAsyncService() {
        this.authService = new AuthServiceImpl();
    }
    
    public void configureLogin(String email, String password) {
        this.mode = Mode.LOGIN;
        this.email = email;
        this.password = password;
    }
    
    public void configureRegister(String email, String password, String name, String lastname) {
        this.mode = Mode.REGISTER;
        this.email = email;
        this.password = password;
        this.name = name;
        this.lastname = lastname;
    }
    
    @Override
    protected Task<AuthResult> createTask() {
        final Mode taskMode = this.mode;
        final String taskEmail = this.email;
        final String taskPassword = this.password;
        final String taskName = this.name;
        final String taskLastname = this.lastname;
        
        return new Task<>() {
            @Override
            protected AuthResult call() throws Exception {
                switch (taskMode) {
                    case LOGIN: 
                        return authService.login(taskEmail, taskPassword);
                    case REGISTER:
                        authService.register(taskEmail, taskPassword, taskName, taskLastname);
                        return authService.login(taskEmail, taskPassword);//login automatico despues de registro
                    default:
                        return null;
                }
            };
        };
    }
}


