/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.dao.impl;

import com.mvcjava.sagt.javafx.config.DatabaseManager;
import com.mvcjava.sagt.javafx.dao.interfaces.SupplierDAO;
import com.mvcjava.sagt.javafx.dao.model.Supplier;
import com.mvcjava.sagt.javafx.exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author lucas
 */
public class SupplierDAOImpl implements SupplierDAO{

    @Override
    public void addSupplier(Supplier supplier) {
        String sql = "INSERT INTO app.proveedores "
                + "(nombre, "
                + "telefono, "
                + "email, "
                + "direccion, "
                + "web, "
                + "localidad, "
                + "provincia)"
                + "VALUES(?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getPhone());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getAddress());
            stmt.setString(5, supplier.getWeb());
            stmt.setString(6, supplier.getLocation());
            stmt.setString(7, supplier.getProvince());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error al crear proveedor.", ex);
        }
    }

    @Override
    public void deleteSupplier(UUID id) {
        String sql = "DELETE FROM app.proveedores WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error al eliminar proveedor con id: " + id, ex);
        }
    }

    @Override
    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<Supplier>();
        String sql = "SELECT * FROM app.proveedores";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Supplier supplier = mapResultSetToSupplier(rs);
                suppliers.add(supplier);
            }
            
        } catch (SQLException ex) {
            throw new DataAccessException("Error al obtener proveedores.", ex);
        }
        
        return suppliers;
    }

    @Override
    public Supplier getSupplier(UUID id) {
        Supplier supplier = null;
        String sql = "SELECT * FROM app.proveedores WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    supplier = mapResultSetToSupplier(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error al obtener proveedor con id: " + id , ex);
        }
        
        return supplier;
    }

    @Override
    public void updateSupplier(UUID id, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        
        //Limpiar valores nulos del mapa (no se admiten nulos)
        updates.values().removeIf(t -> t == null);
        
        if (updates.isEmpty()) return;
        
        StringBuilder sql = new StringBuilder("UPDATE app.proveedores SET ");
        int idx = 0;
        for (Map.Entry<String, Object> e : updates.entrySet()) {
            if (idx++ > 0) sql.append(", ");
            sql.append(e.getKey()).append(" = ? ");
        }
        sql.append(" WHERE id = ?");
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString()))
        {
            int i = 1;
            for (Object p : updates.values()) {
                stmt.setObject(i++, p);
            }
            stmt.setObject(i, id);
            
            stmt.executeUpdate();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new DataAccessException("Error al actualizar el proveedor con id: " + id.toString(), ex);
        }
    }

    @Override
    public boolean alreadyExists(String name, String phone) {
        String sql = "SELECT 1 FROM app.proveedores WHERE nombre LIKE ? AND telefono = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error al comprobar existencia del proveedor: " + name + " " + phone, ex);
        }
    }
    
    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        
        supplier.setId((UUID)rs.getObject("id"));
        supplier.setName(rs.getString("nombre"));
        supplier.setPhone(rs.getString("telefono"));
        supplier.setEmail(rs.getString("email"));
        supplier.setWeb(rs.getString("web"));
        supplier.setAddress(rs.getString("direccion"));
        supplier.setLocation(rs.getString("localidad"));
        supplier.setProvince(rs.getString("provincia"));
        
        return supplier;
    }
    
}
