/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.dao.impl;

import com.mvcjava.sagt.javafx.config.DatabaseManager;
import com.mvcjava.sagt.javafx.dao.interfaces.ClientDAO;
import com.mvcjava.sagt.javafx.dao.model.Client;
import com.mvcjava.sagt.javafx.enums.ClientType;
import com.mvcjava.sagt.javafx.exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author lucas
 */
public class ClientDAOImpl implements ClientDAO{

    @Override
    public Set<Client> findAll() {
        Set<Client> clients = new HashSet<>();
        String sql = "SELECT * FROM app.clientes";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error al obtener clientes.", ex);
        }
        
        return clients;
    }

    @Override
    public Client findById(UUID id) {
        Client client = new Client();
        String sql = "SELECT * FROM app.clientes WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    client = mapResultSetToClient(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error al obtener cliente con id: " + id, ex);
        }
        
        return client;
    }

    @Override
    public void addClient(Client client) {
        String sql = "INSERT INTO app.clientes " + 
                     "( " + 
                        "cuit_cuil, "+
                        "razon_social, " +
                        "tipo, " +
                        "telefono, " +
                        "email, " +
                        "direccion, " +
                        "localidad, " +
                        "provincia, " +
                        "fecha_alta" +
                     ") " +
                     "VALUES (?, ?, ?::public.e_tipo_cliente, ?, ?, ?, ?, ? ,?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setString(1, client.getCuitCuil());
            stmt.setString(2, client.getCompanyName());
            stmt.setString(3, client.getClientType().name().toLowerCase());
            stmt.setString(4, client.getPhone());
            stmt.setString(5, client.getEmail());
            stmt.setString(6, client.getAddress());
            stmt.setString(7, client.getLocation());
            stmt.setString(8, client.getProvince());
            stmt.setDate(9, client.getEntryDate());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error al añadir cliente: " + client.getCuitCuil(), ex);
        }
    }

    @Override
    public void updateClient(UUID id, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) return;
        
        updates.values().removeIf(t -> t == null);
        
        if (updates.isEmpty()) return;
        
        StringBuilder sql = new StringBuilder("UPDATE app.clientes SET ");
        int idx = 0;
        for (Map.Entry<String, Object> e : updates.entrySet()) {
            if (idx++ > 0) sql.append(", ");
            sql.append(e.getKey()).append(" = ? ");
        }
        sql.append(" WHERE id = ?");
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int i = 1;
            for (Object p : updates.values()) {
                stmt.setObject(i++, p);
            }
            stmt.setObject(i, id);
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error al actualizar cliente: " + id, ex);
        }
    }

    @Override
    public void deleteClient(UUID id) {
        String sql = "DELETE FROM app.clientes WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error al eliminar cliente: " + id, ex);
        }
    }

    @Override
    public boolean alreadyExists(UUID id, String cuit_cuil) {
        String sql = "SELECT 1 FROM app.clientes WHERE cuit_cuil LIKE ? AND id <> ? LIMIT 1";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cuit_cuil);
            stmt.setObject(2, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error al verificar existencia de cliente: " + id + " " + cuit_cuil, ex);
        }
    }
    
    private Client mapResultSetToClient(ResultSet rs) throws SQLException{
        Client client = new Client();
        
        client.setId((UUID) rs.getObject("id"));
        client.setCuitCuil(rs.getString("cuit_cuil"));
        client.setCompanyName(rs.getString("razon_social"));
        client.setClientType(ClientType.fromString(rs.getString("tipo")));
        client.setPhone(rs.getString("telefono"));
        client.setEmail(rs.getString("email"));
        client.setAddress(rs.getString("direccion"));
        client.setLocation(rs.getString("localidad"));
        client.setProvince(rs.getString("provincia"));
        client.setEntryDate(rs.getDate("fecha_alta"));
        
        return client;
    }
    
}
