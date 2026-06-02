/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.auth;

import java.util.UUID;

/**
 *
 * @author lucas
 */
public class AuthResult {
    private final UUID id;
    private final String email;
    private final String name;
    private final String lastname;
    
    public AuthResult(UUID id, String email, String name, String lastname) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.lastname = lastname;
    }
    
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getLastname() { return lastname; }
    
    public String getFullName() {
        String full = (name + " " + lastname).trim();
        return full.isEmpty() ? email : full;
    }
}
