/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.service.impl;

import com.mvcjava.sagt.javafx.dao.impl.ProductDAOImpl;
import java.util.Map;
import java.util.UUID;

import com.mvcjava.sagt.javafx.dao.interfaces.ProductDAO;
import com.mvcjava.sagt.javafx.dao.model.Product;
import com.mvcjava.sagt.javafx.dto.ProductWithRelations;
import com.mvcjava.sagt.javafx.exception.BusinessException;
import com.mvcjava.sagt.javafx.service.interfaces.ProductService;
import java.util.List;
import java.util.Set;

/**
 *
 * @author lucas
 */
public class ProductServiceImpl implements ProductService {
    private final ProductDAO productDAO;
    
    public ProductServiceImpl() {
        this.productDAO = new ProductDAOImpl();
    }

    @Override
    public void createProduct(Product product) throws BusinessException {
        boolean exist = productDAO.alreadyExist(product.getId(), product.getName(), product.getModel(), product.getBrand());
        if (exist) {
            throw new BusinessException("Este producto ya existe: " + product.getName() + " " + product.getBrand() + " " + product.getModel());
        }
            
        if (product.getPurchasePrice() > product.getSalePrice()) {
            throw new BusinessException("El precio de venta debe ser mayor al precio de compra.");
        }
       
        productDAO.addProduct(product);
    }

    @Override
    public void createProductWithCategories(Product product, Set<UUID> categoryIds) throws BusinessException {
        boolean exists = productDAO.alreadyExist(product.getId(), product.getName(), product.getModel(), product.getBrand());
        
        if (exists) {
            throw new BusinessException("Este producto ya existe: " + product.getName() + " " + product.getBrand() + " " + product.getModel());
        }
        
        if (product.getPurchasePrice() > product.getSalePrice()) {
            throw new BusinessException("El precio de venta debe ser mayor al precio de compra.");
        }
        
        productDAO.addProductWithCategories(product, categoryIds);
    }    

    @Override
    public Product getProduct(UUID id) {
        return productDAO.getProduct(id);
    }

    @Override
    public List<Product> getAll() {
        return productDAO.findAll();
    }
    
    @Override
    public List<ProductWithRelations> getAllWithRelations() {
        return productDAO.findAllWithRelations();
    }

    @Override
    public void updateProduct(UUID id, Map<String, Object> updates) throws BusinessException {
        Product currentProduct = productDAO.getProduct(id);
        if (currentProduct == null) {
            throw new BusinessException("El producto que quiere actualizar no existe: " + id);
        }
        
        String newName = updates.containsKey("nombre") ? (String) updates.get("nombre")
                : currentProduct.getName();
        String newBrand = updates.containsKey("marca") ? (String) updates.get("marca")
                : currentProduct.getBrand();
        String newModel = updates.containsKey("modelo") ? (String) updates.get("modelo")
                : currentProduct.getModel();
        
        boolean dataChanged = updates.containsKey("nombre") ||
                updates.containsKey("modelo") ||
                updates.containsKey("marca");
        
        System.out.println("datachanged?: " + dataChanged);
        
        if (dataChanged) {
            boolean exists = productDAO.alreadyExist(id, newName, newModel, newBrand); 
            if (exists) {
                throw new BusinessException("Ya existe otro producto con el mismo nombre, marca y modelo.");
            }
        }
        
        if (updates.containsKey(("precio_compra")) || updates.containsKey("precio_venta")) {
            float newPurchasePrice = updates.containsKey("precio_compra") ? ((Float) updates.get("precio_compra")) : currentProduct.getPurchasePrice();
            float newSalePrice = updates.containsKey("precio_venta") ? ((Float) updates.get("precio_venta")) : currentProduct.getSalePrice();
            
            if (newPurchasePrice > newSalePrice) {
                throw new BusinessException("El precio de venta debe ser mayor al precio de compra.");
            }
        }
        
        productDAO.updateProduct(id, updates);
    }

    @Override
    public void updateProductCategories(Map<UUID, Set<UUID>> updates) throws BusinessException{
        //Validaciones
        for (Map.Entry<UUID, Set<UUID>> entry : updates.entrySet()) {
            UUID productId = entry.getKey();
            
            if (productId == null) {
                throw new BusinessException("ID de producto inválido");
            }
        }
        
        productDAO.updateProductCategories(updates);
    }
    
    @Override
    public void deleteProduct(UUID id) throws BusinessException {
        Product currentProduct = productDAO.getProduct(id);
        if (currentProduct == null) {
            throw new BusinessException("El producto que quiere eliminar no existe: " + id);
        }
        productDAO.deleteProduct(id);
    }

    @Override
    public void saveChanges (
            Map<Product, 
            Set<UUID>> newProducts, 
            Map<UUID, Map<String, Object>> updatedProducts,
            Map<UUID, Set<UUID>> updatedCategories,
            Set<UUID> deletedProducts
    ) throws BusinessException {
        //Actualizacion
        for (Map.Entry<UUID, Map<String, Object>> entry : updatedProducts.entrySet()) {
            updateProduct(entry.getKey(), entry.getValue());
        }
        
        updateProductCategories(updatedCategories);
        
        //Eliminación
        for (UUID id : deletedProducts) {
            deleteProduct(id);
        }
        
        //Creacion
        for (Map.Entry<Product, Set<UUID>> entry : newProducts.entrySet()) {
            createProductWithCategories(entry.getKey(), entry.getValue());
        }
        
    }
}
