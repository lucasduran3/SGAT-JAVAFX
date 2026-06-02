/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.service.impl;

import com.mvcjava.sagt.javafx.auth.AuthResult;
import com.mvcjava.sagt.javafx.dao.impl.AuthDAOImpl;
import com.mvcjava.sagt.javafx.dao.interfaces.AuthDAO;
import com.mvcjava.sagt.javafx.exception.BusinessException;
import com.mvcjava.sagt.javafx.exception.DataAccessException;
import com.mvcjava.sagt.javafx.service.interfaces.AuthService;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author lucas
 */
public class AuthServiceImpl implements AuthService {
    
    private static final int BCRYPT_LOG_ROUNDS = 12;
    private static final int MIN_PASSWORD_LEN = 8;
    private static final int MAX_PASSWORD_LEN = 72;
    
    private final AuthDAO authDAO;
    
    public AuthServiceImpl() {
        this.authDAO = new AuthDAOImpl();
    }

    @Override
    public AuthResult login(String email, String password) throws BusinessException {
        if (email == null || email.isBlank()) {
            throw new BusinessException("El email es obligatorio.");
        }
        if (password == null || password.isEmpty()) {
            throw new BusinessException("La contraseña es obligatoria.");
        }
        
        AuthDAO.AuthRow row = authDAO.findByEmail(email);   
        
        if (row == null || !BCrypt.checkpw(password, row.passwordHash)) {
            throw new BusinessException("Email o contraseña incorrectos.");
        }
        
        return new AuthResult(row.id, row.email, row.name, row.lastname);
    }
    

    @Override
    public void register(String email, String password, String name, String lastname) throws BusinessException {
        // validacion email
        if (email == null || email.isBlank()) {
            throw new BusinessException("El email es obligatorio.");
        }
        if (!BasicStringValidator.isValidEmail(email.trim())) {
            throw new BusinessException("El formato del email no es válido.");
        }
        
        //validacion contraseña
        if (password == null || password.length() < MIN_PASSWORD_LEN) {
            throw new BusinessException("La contraseña debe tener al menos " + MIN_PASSWORD_LEN + " carácteres.");
        }
        if (password.length() > MAX_PASSWORD_LEN) {
            throw new BusinessException("La contraseña no debe superar los " + MAX_PASSWORD_LEN + " carácteres.");
        }
        
        //validacion nombre y apellido
        if (name != null || !name.isBlank()) {
            try {
                BasicStringValidator.validate(name.trim(), 2, 100, "nombre");
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(ex.getMessage());
            }
        }
        if (lastname != null || !lastname.isBlank()) {
            try {
                BasicStringValidator.validate(lastname.trim(), 2, 100, "apellido");
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(ex.getMessage());
            }
        }
        
        //hash contraseña con bcrypt
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
        
        try {
            authDAO.registerUser(email.trim().toLowerCase(), hash, 
                    name != null ? name.trim() : null,
                    lastname != null ? lastname.trim() : null);
        } catch (DataAccessException ex) {
            String msg = ex.getMessage();
            if (msg != null && msg.contains("ya está registrado")) {
                throw new BusinessException("El email: " + email + " ya está registrado.");
            }
            throw new BusinessException("No se pudo completar el registro: " + msg);
        }
    }
    
}
