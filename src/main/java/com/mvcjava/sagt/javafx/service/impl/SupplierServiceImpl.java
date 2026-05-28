/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.service.impl;

import com.mvcjava.sagt.javafx.dao.impl.SupplierDAOImpl;
import com.mvcjava.sagt.javafx.dao.interfaces.SupplierDAO;
import com.mvcjava.sagt.javafx.dao.model.Supplier;
import com.mvcjava.sagt.javafx.exception.BusinessException;
import com.mvcjava.sagt.javafx.service.interfaces.SupplierService;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author lucas
 */
public class SupplierServiceImpl implements SupplierService {
    private final SupplierDAO supplierDAO;
    
    public SupplierServiceImpl() {
        this.supplierDAO = new SupplierDAOImpl();
    }

    @Override
    public List<Supplier> getAll() {
        return supplierDAO.findAll();
    }

    @Override
    public void createSupplier(Supplier supplier) throws BusinessException {
        boolean alreadyExists = supplierDAO.alreadyExists(supplier.getName(), supplier.getPhone());
        
        if (alreadyExists) {
            throw new BusinessException("Este proveedor ya existe.");
        }
        
        if (!BasicStringValidator.isValidPhone(supplier.getPhone())) {
            throw new BusinessException("Número de teléfono inválido.");
        }
        if (!BasicStringValidator.isValidEmail(supplier.getEmail())) {
            throw new BusinessException("Dirección de email inválida.");
        }
        
        supplierDAO.addSupplier(supplier);
    }

    @Override
    public void deleteSupplier(UUID id) throws BusinessException {
        System.out.println("Eliminando dese service");
        Supplier currentSupplier = getSupplier(id);
        if (currentSupplier == null) {
            throw new BusinessException("El proveedor que desea eliminar no existe.");
        }
        
        supplierDAO.deleteSupplier(id);
    }

    @Override
    public Supplier getSupplier(UUID id) {
        return supplierDAO.getSupplier(id);
    }

    @Override
    public void updateSupplier(UUID id, Map<String, Object> updates) throws BusinessException {
        Supplier currentSupplier = supplierDAO.getSupplier(id);
        if (currentSupplier == null) {
            throw new BusinessException("El proveedor que desea actualizar no existe.");
        }
        
        //Validaciones
        if (updates.containsKey("telefono")) {
            if(!BasicStringValidator.isValidPhone(updates.get("telefono").toString())) {
                throw new BusinessException("Número de teléfono inválido.");
            }
        }
        if (updates.containsKey(("email"))) {
            if (!BasicStringValidator.isValidEmail((updates.get("email").toString()))) {
                throw new BusinessException("Dirección de email inválida.");
            }
        }
        
        String newName = updates.containsKey("nombre") ? (String) updates.get("nombre")
                : currentSupplier.getName();
        
        String newPhone = updates.containsKey("telefono") ? (String) updates.get("telefono")
                : currentSupplier.getPhone();
        
        boolean dataChanged = updates.containsKey("nombre") || updates.containsKey("telefono");
        
        if (dataChanged) {
            boolean exists = supplierDAO.alreadyExists(newName, newPhone);
            if (exists) {
                throw new BusinessException("Ya existe un proveedor con el mismo nombre y telefono.");
            }
        }
        
        supplierDAO.updateSupplier(id, updates);
    }

    @Override
    public void saveChanges(List<Supplier> newSuppliers, Map<UUID, Map<String, Object>> suppliersToUpdate, List<Supplier> suppliersToDelete) throws BusinessException{
        for (Map.Entry<UUID, Map<String, Object>> entry: suppliersToUpdate.entrySet()) {
            updateSupplier(entry.getKey(), entry.getValue());
        }
        
        for (Supplier supplier : suppliersToDelete) {
            deleteSupplier(supplier.getId());
        }
        
        for (Supplier supplier : newSuppliers) {
            createSupplier(supplier);
        }
    }
}
