/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.dao.impl;

import com.mvcjava.sagt.javafx.config.DatabaseManager;
import com.mvcjava.sagt.javafx.dao.interfaces.AuthDAO;
import com.mvcjava.sagt.javafx.exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 *
 * @author lucas
 */
public class AuthDAOImpl implements AuthDAO {

    @Override
    public AuthRow findByEmail(String email) {
        String sql = "SELECT user_id, email, password_hash, nombre, apellido "
                + "FROM auth.login(?)";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID id = (UUID) rs.getObject("user_id");
                    if (id == null) return null;
                    
                    return new AuthRow(
                            id,
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("nombre"),
                            rs.getString("apellido")
                    );
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error al buscar usuario por email.", ex);
        }
    }

    @Override
    public void registerUser(String email, String passwordHash, String name, String lastname) {
        String sql = "SELECT auth.registrar_usuario(?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            stmt.setString(2, passwordHash);
            stmt.setString(3, name != null && !name.isBlank() ? name.trim() : null);
            stmt.setString(4, lastname != null && !lastname.isBlank() ? lastname.trim() : null);
            
            stmt.execute();
        } catch (SQLException ex) {
            if (ex.getSQLState() != null && ex.getSQLState().startsWith("23")) {
                throw new DataAccessException("El email ya está registrado.", ex);
            }
            throw new DataAccessException("Error al registrar usuario.", ex);
        }
    }

    @Override
    public void setCurrentUserConnection(Connection conn, UUID userId) {
        String sql = "SELECT auth.set_current_user(?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, userId);
            stmt.execute();
        } catch (SQLException ex) {
            //No es critico para el login en si. loguear y continuar
            System.err.println("Advertencia: no se pudo fijar current_user_id en conexión: " + ex.getMessage());
        }
    }
}
