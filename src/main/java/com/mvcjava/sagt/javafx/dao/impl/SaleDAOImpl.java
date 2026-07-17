/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.dao.impl;

import com.mvcjava.sagt.javafx.config.DatabaseManager;
import com.mvcjava.sagt.javafx.dao.interfaces.SaleDAO;
import com.mvcjava.sagt.javafx.dao.model.SaleHeader;
import com.mvcjava.sagt.javafx.dao.model.SaleDetail;
import com.mvcjava.sagt.javafx.dto.HeaderSaleWithClient;
import com.mvcjava.sagt.javafx.dto.DetailSaleWithProduct;
import com.mvcjava.sagt.javafx.enums.PaymentMethod;
import com.mvcjava.sagt.javafx.exception.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SaleDAOImpl implements SaleDAO {

    @Override
    public List<HeaderSaleWithClient> findAllHeaders() {
        List<HeaderSaleWithClient> result = new ArrayList<>();

        String sql = "SELECT "
                + "vc.id, "
                + "vc.numero_factura, "
                + "vc.fecha, "
                + "vc.id_cliente, "
                + "vc.total, "
                + "vc.metodo_pago::text AS metodo_pago, "
                + "vc.cargado_por, "
                + "c.razon_social AS cliente_razon_social "
                + "FROM app.ventas_cabecera vc "
                + "LEFT JOIN app.clientes c ON vc.id_cliente = c.id "
                + "ORDER BY vc.fecha DESC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                SaleHeader header = mapResultSetToHeader(rs);
                String clientName = rs.getString("cliente_razon_social");
                result.add(new HeaderSaleWithClient(header, clientName));
            }

        } catch (SQLException ex) {
            throw new DataAccessException("Error al obtener cabeceras de venta.", ex);
        }

        return result;
    }


    @Override
    public List<DetailSaleWithProduct> findDetailBySaleId(UUID idVenta) {
        List<DetailSaleWithProduct> result = new ArrayList<>();

        String sql = "SELECT "
                + "vd.id, "
                + "vd.id_venta, "
                + "vd.id_producto, "
                + "vd.precio_unitario, "
                + "vd.subtotal, "
                + "vd.cantidad, "
                + "p.nombre AS producto_nombre "
                + "FROM app.ventas_detalle vd "
                + "LEFT JOIN app.productos p ON vd.id_producto = p.id "
                + "WHERE vd.id_venta = ? "
                + "ORDER BY p.nombre";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idVenta);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SaleDetail detail = mapResultSetToDetail(rs);
                    String productName = rs.getString("producto_nombre");
                    result.add(new DetailSaleWithProduct(detail, productName));
                }
            }

        } catch (SQLException ex) {
            throw new DataAccessException (
                    "Error al obtener detalle de venta con id: " + idVenta, ex);
        }

        return result;
    }

    @Override
    public boolean billNumberExists(UUID excludeId, String billNumber) {
        String sql = "SELECT 1 FROM app.ventas_cabecera WHERE numero_factura = ? AND id <> ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, billNumber.trim().toLowerCase());
            stmt.setObject(2, excludeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                 return rs.next();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error al verificar número de factura: " + billNumber, ex);
        }
    }

    @Override
    public void updateHeader(UUID id, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) return;
        
        updates.values().removeIf(v -> v == null);
        if (updates.isEmpty()) return;
        
        StringBuilder sql = new StringBuilder("UPDATE app.ventas_cabecera SET ");
        int idx = 0;
        for (Map.Entry<String, Object> e : updates.entrySet()) {
            if (idx++ > 0) sql.append(", ");
            if (e.getKey().equals("metodo_pago")) {
                sql.append("metodo_pago = ?::app.e_metodo_pago ");
            } else {
                sql.append(e.getKey()).append(" = ? ");
            }
        }
        sql.append(" WHERE id = ? ");
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int i = 1;
            for (Object val : updates.values()) {
                stmt.setObject(i++, val);
            }
            stmt.setObject(i, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new DataAccessException("Error al actualizar cabecera de venta con id: " + id, ex);
        }
    }

    @Override
    public void updateDetail(UUID id, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) return;
        
        updates.values().removeIf(v -> v == null);
        if (updates.isEmpty()) return;
        
        StringBuilder sql = new StringBuilder("UPDATE app.ventas_detalle SET ");
        int idx = 0;
        for (Map.Entry<String, Object> e : updates.entrySet()) {
            if (idx++ > 0) sql.append(", ");
            sql.append(e.getKey()).append(" = ?");
        }
        sql.append(" WHERE id = ?");
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int i = 1;
            for (Object val : updates.values()) {
                stmt.setObject(i++, val);
            }
            stmt.setObject(i, id);
            stmt.executeUpdate();
        }  catch (SQLException ex) {
            throw new DataAccessException("Error al actualizar detalle de venta con id: " + id, ex);
        }
        
    }

    @Override
    public UUID insertHeader(SaleHeader header) {
        String sql = "INSERT INTO app.ventas_cabecera "
                + "(id, numero_factura, fecha, id_cliente, total, metodo_pago, cargado_por) "
                + "VALUES(?,?,?,?,0,?::app.e_metodo_pago, ?) "
                + "RETURNING id";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            UUID newId = UUID.randomUUID();
            stmt.setObject(1, header.getId() == null ? newId : header.getId());
            stmt.setString(2, header.getBillNumber().trim().toLowerCase());
            stmt.setDate(3, header.getDate());
            stmt.setObject(4, header.getClientId());
            stmt.setString(5, header.getPaymentMethod().name());
            stmt.setObject(6, header.getLoadedBy());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return (UUID) rs.getObject(1);
                }
            }
            
            throw new DataAccessException("No se pudo obtener el ID generado para la cabecera.", null); 
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new DataAccessException("Error al insertar cabecera de venta.", ex);
        }
    }

    @Override
    public UUID insertDetail(SaleDetail detail) {
        String insertSql = "INSERT INTO app.ventas_detalle "
                + "(id, id_venta, id_producto, precio_unitario, subtotal, cantidad) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "RETURNING id";
 
        String updateTotalSql = "UPDATE app.ventas_cabecera "
                + "SET total = (SELECT COALESCE(SUM(subtotal), 0) "
                + "             FROM app.ventas_detalle "
                + "             WHERE id_venta = ?) "
                + "WHERE id = ?";
 
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
 
            UUID newId;
            UUID detailId = UUID.randomUUID();
 
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setObject(1, detailId);
                stmt.setObject(2, detail.getSaleId());
                stmt.setObject(3, detail.getProductId());
                stmt.setFloat(4, detail.getUnitPrice());
                stmt.setFloat(5, detail.getSubtotal());
                stmt.setInt(6, detail.getAmmount());
 
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        newId = (UUID) rs.getObject(1);
                    } else {
                        throw new DataAccessException(
                                "No se pudo obtener el ID generado para el ítem.", null);
                    }
                }
            }
 
            // Recalcular total de la cabecera
            try (PreparedStatement stmt = conn.prepareStatement(updateTotalSql)) {
                stmt.setObject(1, detail.getSaleId());
                stmt.setObject(2, detail.getSaleId());
                stmt.executeUpdate();
            }
 
            conn.commit();
            return newId;
 
        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rb) {
                    System.err.println("Error en rollback: " + rb.getMessage());
                }
            }
            ex.printStackTrace();
            throw new DataAccessException("Error al insertar ítem de venta.", ex);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {
                    System.err.println("Error al cerrar conexión: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public void deleteHeader(UUID saleId) {
        String sql = "DELETE FROM app.ventas_cabecera WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, saleId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error al eliminar venta: " + saleId.toString(), ex);
        }
    }

    @Override
    public void deleteDetail(UUID detailId) {
        String sql = "DELETE FROM app.ventas_detalle WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, detailId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error al eliminar ítem de venta: " + detailId.toString(), ex);
        }
    }

    @Override
    public SaleHeader findSaleById(UUID saleId) {
        SaleHeader sale = new SaleHeader();
        String sql = "SELECT * FROM app.ventas_cabecera WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, saleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sale = mapResultSetToHeader(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error al obtener venta con id: " + saleId.toString(), ex);
        }
        
        return sale;
    }

    @Override
    public SaleDetail findDetailById(UUID detailId) {
        SaleDetail detail = new SaleDetail();
        String sql = "SELECT * FROM app.ventas_detalle WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, detailId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    detail = mapResultSetToDetail(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Erro al obtener item con id: " + detailId.toString(), ex);
        }
        
        return detail;
    }
    
    private SaleHeader mapResultSetToHeader(ResultSet rs) throws SQLException {
        SaleHeader vc = new SaleHeader();
        vc.setId((UUID) rs.getObject("id"));
        vc.setBillNumber(rs.getString("numero_factura"));
        vc.setDate(rs.getDate("fecha"));
        vc.setClientId((UUID) rs.getObject("id_cliente"));
        vc.setTotal(rs.getFloat("total"));
        vc.setPaymentMethod(PaymentMethod.fromString(rs.getString("metodo_pago")));
        vc.setLoadedBy((UUID) rs.getObject("cargado_por"));
        return vc;
    }

    private SaleDetail mapResultSetToDetail(ResultSet rs) throws SQLException {
        SaleDetail vd = new SaleDetail();
        vd.setId((UUID) rs.getObject("id"));
        vd.setSaleId((UUID) rs.getObject("id_venta"));
        vd.setProductId((UUID) rs.getObject("id_producto"));
        vd.setUnitPrice(rs.getFloat("precio_unitario"));
        vd.setSubtotal(rs.getFloat("subtotal"));
        vd.setAmmount(rs.getInt("cantidad"));
        return vd;
    }
}
