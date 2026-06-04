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
public class SessionContext {
    private static UUID currentUserId;
    private static String currentUserName;
    private static String currentUserEmail;
    
    private SessionContext() {}
    
    public static void setCurrentUser(AuthResult result) {
        currentUserId = result.getId();
        currentUserName = result.getFullName();
        currentUserEmail = result.getEmail();
    }
    
    public static void setCurrentUser(UUID id, String name) {
        currentUserId = id;
        currentUserName = name;
    }
    
    /*@Deprecated
    public static void setCurrentUser() {
        currentUserId = UUID.fromString("a6bbb40c-76a2-4a66-8805-a42a58392122");
        currentUserName = "Ana Administradora";
    }*/
    
    public static void clear() {
        currentUserId = null;
        currentUserEmail = null;
        currentUserName = null;
    }
    
    public static UUID getCurrentUserId() {
        if (currentUserId == null) {
            throw new IllegalStateException("No hay usuario autenticado");
        }
        return currentUserId;
    }
    
    public static String getCurrentUserName() {
        return currentUserName != null ? currentUserName : "Desconocido";
    }
    
    public static String getCurrentUserEmail() {
        return currentUserEmail != null ? currentUserEmail : "";
    }
    
    public static boolean isLoggedIn() {
        return currentUserId != null;
    }
}