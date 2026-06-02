/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mvcjava.sagt.javafx.service.interfaces;

import com.mvcjava.sagt.javafx.auth.AuthResult;
import com.mvcjava.sagt.javafx.exception.BusinessException;

/**
 *
 * @author lucas
 */
public interface AuthService {
    AuthResult login(String email, String password) throws BusinessException;
    void register(String email, String password, String name, String lastname) throws BusinessException;
}
