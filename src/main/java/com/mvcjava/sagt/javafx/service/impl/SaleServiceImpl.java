/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.service.impl;

import com.mvcjava.sagt.javafx.dao.impl.SaleDAOImpl;
import com.mvcjava.sagt.javafx.dao.interfaces.SaleDAO;
import com.mvcjava.sagt.javafx.dao.model.SaleDetail;
import com.mvcjava.sagt.javafx.dao.model.SaleHeader;
import com.mvcjava.sagt.javafx.dto.DetailSaleWithProduct;
import com.mvcjava.sagt.javafx.dto.HeaderSaleWithClient;
import com.mvcjava.sagt.javafx.enums.PaymentMethod;
import com.mvcjava.sagt.javafx.exception.BusinessException;
import com.mvcjava.sagt.javafx.service.interfaces.SaleService;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author lucas
 */
public class SaleServiceImpl implements SaleService {
    
    private final SaleDAO saleDAO;
    
    public SaleServiceImpl() {
        this.saleDAO = new SaleDAOImpl();
    }

    @Override
    public List<HeaderSaleWithClient> getAllHeaders() {
        return saleDAO.findAllHeaders();
    }

    @Override
    public List<DetailSaleWithProduct> getDetailBySaleId(UUID saleId) {
        return saleDAO.findDetailBySaleId(saleId);
    }

    @Override
    public void updateHeader(UUID id, Map<String, Object> updates) throws BusinessException {
        if (updates == null || updates.isEmpty()) return;
        
        if (updates.containsKey("numero_factura")) {
            String billNumber = updates.get("numero_factura").toString().trim();
            if (billNumber.isEmpty()) {
                throw new BusinessException("El número de factura no puede estar vacío");
            }
            if (billNumber.length() > 20) {
                throw new BusinessException("El numero de factura no puede superar los 20 carácteres");
            }
            boolean exists = saleDAO.billNumberExists(id, billNumber);
            if (exists) {
                throw new BusinessException("Ya existe otra venta con el número de fáctura: " + billNumber);
            }
            
            //normalizar a minusculas
            updates.put("numero_factura", billNumber.toLowerCase());
            
            //validar metodo de pago
            if (updates.containsKey("metodo_pago")) {
                String raw = updates.get("metodo_pago").toString();
                try {
                    PaymentMethod.fromString(raw);
                    updates.put("metodo_pago", raw.trim().toLowerCase());
                } catch (IllegalArgumentException ex) {
                    throw new BusinessException("Metodo de pago inválido: " + raw);
                }
            }
        }
        saleDAO.updateHeader(id, updates);
    }

    @Override
    public void updateDetail(UUID id, Map<String, Object> updates) throws BusinessException {
        if (updates == null || updates.isEmpty()) return;
        saleDAO.updateDetail(id, updates);
    }

    @Override
    public UUID createHeader(SaleHeader header) throws BusinessException {
        if (header == null) {
            throw new BusinessException("La cabecera de venta no puede ser nula.");
        }
        
        String billNumber = header.getBillNumber() == null ? "" : header.getBillNumber().trim();
        try {
            BasicStringValidator.validate(billNumber, 1, 20, "número de factura");
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ex.getMessage());
        }
        
        boolean exists = saleDAO.billNumberExists(new UUID(0L, 0L), billNumber);
        if (exists) {
            throw new BusinessException("Ya existe una venta con el número de factura: " + billNumber);
        }
        
        if (header.getClientId() == null) {
            throw new BusinessException("El cliente es obligatorio.");
        }
        
        if (header.getPaymentMethod() == null) {
            throw new BusinessException("El método de pago es obligatorio.");
        }
        
        if (header.getDate() == null) {
            throw new BusinessException("La fecha es obligatoria.");
        }
        
        return saleDAO.insertHeader(header);
    }

    @Override
    public UUID createDetail(SaleDetail detail) throws BusinessException {
        if (detail == null) {
            throw new BusinessException("El item de dealle no puede ser nulo.");
        }
        if (detail.getSaleId() == null) {
            throw new BusinessException("El id de la venta no puede ser nulo.");
        }
        if (detail.getProductId() == null) {
            throw new BusinessException("Debe seleccionar un producto.");
        }
        if (detail.getAmmount() <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a 0.");
        }
        if (detail.getUnitPrice() <= 0) {
            throw new BusinessException("El precio unitario debe ser mayor a 0.");
        }
        
        return saleDAO.insertDetail(detail);
    }

    @Override
    public void deleteHeader(UUID saleId) throws BusinessException {
        SaleHeader currentSale = saleDAO.findSaleById(saleId);
        if (currentSale == null) {
            throw new BusinessException("La venta que quiere eliminar no existe.");
        }
        saleDAO.deleteHeader(saleId);
    }

    @Override
    public void deleteDetail(UUID detailId) throws BusinessException {
        SaleDetail currentDetail = saleDAO.findDetailById(detailId);
        if (currentDetail == null) {
            throw new BusinessException("El item de venta que quiere eliminar no existe.");
        }
        saleDAO.deleteDetail(detailId);
    }

    @Override
    public void saveChanges(
            List<SaleHeader> newSales, 
            List<SaleDetail> newDetails,
            Map<UUID, Map<String, Object>> headersToUpdate,
            Map<UUID, Map<String, Object>> detailsToUpdate,
            List<UUID> headersToDelete,
            List<UUID> detailsToDelete
    ) throws BusinessException {
        //Actualizacion
        for (Map.Entry<UUID, Map<String, Object>> entry : headersToUpdate.entrySet()) {
            updateHeader(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<UUID, Map<String, Object>> entry : detailsToUpdate.entrySet()) {
            updateDetail(entry.getKey(), entry.getValue());
        }
        
        //Eliminacion
        for(UUID id: detailsToDelete) {
            deleteDetail(id);
        }
        for (UUID id: headersToDelete) {
            deleteHeader(id);
        }
        
        //Insercion
        for (SaleHeader header : newSales) {
            createHeader(header);
        }
        for (SaleDetail detail : newDetails) {
            createDetail(detail);
        }
        
    }
}
