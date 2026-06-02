/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.dao.interfaces;

import java.sql.Connection;
import java.util.UUID;

/**
 *
 * @author lucas
 */
public interface AuthDAO {
    AuthRow findByEmail(String email);
    void registerUser(String email, String passwordHash, String name, String lastname);
    void setCurrentUserConnection(Connection conn, UUID userId);
    
    final class AuthRow {
        public final UUID id;
        public final String email;
        public final String passwordHash;
        public final String name;
        public final String lastname;
        
        public AuthRow(UUID id, String email, String passwordHash, String name, String lastname) {
            this.id = id;
            this.email = email;
            this.passwordHash = passwordHash;
            this.name = name;
            this.lastname = lastname;
        }
    }
}
