/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.dao.model;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import java.util.Objects;

/**
 *
 * @author lucas
 */
public class Product {
    private UUID id;
    private String name;
    private String brand;
    private String model;
    private float purchasePrice;
    private float salePrice;
    private int stock;
    private int minStock;
    private UUID idSupplier;
    private UUID loadedBy;
    private Timestamp entryDate;
    private Timestamp updateDate;
    
    public Product() {}
    
    public Product(UUID id, String name, String brand, String model, float purchasePrice, 
                    float salePrice, int stock, int minStock, UUID idSupplier, 
                    UUID loadedBy, Timestamp entryDate, Timestamp updateDate) {
           
        this.setId(id);
        this.setName(name);
        this.setBrand(brand);
        this.setModel(model);
        this.setPurchasePrice(purchasePrice);
        this.setSalePrice(salePrice);
        this.setStock(stock);
        this.setMinStock(minStock);
        this.setIdSupplier(idSupplier);
        this.setLoadedBy(loadedBy);
        this.setEntryDate(entryDate);
        this.setUpdateDate(updateDate);
    }
    
    public Product(UUID id, String name, String brand, String model, float purchasePrice, 
                    float salePrice, int stock, int minStock, UUID idSupplier, UUID loadedBy ) {
        
        this.setId(id);
        this.setName(name);
        this.setBrand(brand);
        this.setModel(model);
        this.setPurchasePrice(purchasePrice);
        this.setSalePrice(salePrice);
        this.setStock(stock);
        this.setMinStock(minStock);
        this.setIdSupplier(idSupplier);
        this.setLoadedBy(loadedBy);
        this.setEntryDate(Timestamp.from(Instant.now()));
        this.setUpdateDate(null);
    }
    
    public Product(String name, String brand, String model, float purchasePrice, 
                    float salePrice, int stock, int minStock, UUID idSupplier, UUID loadedBy ) {
        
        this.setId(null);
        this.setName(name);
        this.setBrand(brand);
        this.setModel(model);
        this.setPurchasePrice(purchasePrice);
        this.setSalePrice(salePrice);
        this.setStock(stock);
        this.setMinStock(minStock);
        this.setIdSupplier(idSupplier);
        this.setLoadedBy(loadedBy);
        this.setEntryDate(Timestamp.from(Instant.now()));
        this.setUpdateDate(null);
    }
    
    /*SETTERS*/
    public void setId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser null");
        }
        this.id = id; 
    }
    
    public void setName(String name) {
        BasicStringValidator.validate(name, 3, 100, "nombre");
        this.name = name.trim(); 
    }
    
    public void setBrand(String brand) {
        BasicStringValidator.validate(brand, 1, 100, "marca");
        this.brand = brand.trim();
    }
    
    public void setModel(String model) {
        BasicStringValidator.validate(model, 1, 100, "modelo");
        this.model = model.trim();
    }
    
    public void setPurchasePrice(float purchasePrice) {
        if (purchasePrice < 0) {
            throw new IllegalArgumentException("El precio de compra no puede ser negativo.");
        }
        this.purchasePrice = purchasePrice; 
    }
    
    public void setSalePrice(float salePrice) {
        if (salePrice < 0) {
            throw new IllegalArgumentException("El precio de venta no puede ser negativo.");
        }
        this.salePrice = salePrice; 
    }
    
    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        this.stock = stock; 
    }
    
    public void setMinStock(int minStock) {
        if (minStock < 0) {
            throw new IllegalArgumentException("El sotck minimo no puede ser negativo.");
        }
        this.minStock = minStock;
    }
    
    public void setIdSupplier(UUID idSupplier) {
        this.idSupplier = idSupplier; 
    }
    
    public void setLoadedBy(UUID loadedBy) {
        this.loadedBy = loadedBy;
    }
    
    public void setEntryDate(Timestamp entryDate) {        
        if (entryDate == null) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser null.");
        }
        this.entryDate = entryDate; 
    }
    
    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate; 
    }
    
    /*GETTERS*/
    public UUID getId() { return this.id; }
    public String getName() { return this.name; }
    public String getBrand() { return this.brand; }
    public String getModel() { return this.model; }
    public float getPurchasePrice() { return this.purchasePrice; }
    public float getSalePrice() { return this.salePrice; }
    public int getStock() { return this.stock; }
    public int getMinStock() { return this.minStock; }
    public UUID getIdSupplier() { return this.idSupplier; }
    public UUID getLoadedBy() { return this.loadedBy; }
    public Timestamp getEntryDate() { return this.entryDate; }
    public Timestamp getUpdateDate() { return this.updateDate; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product other = (Product) obj;
        
        return Objects.equals(name.toLowerCase(), other.name.toLowerCase()) &&
                Objects.equals(brand.toLowerCase(), other.brand.toLowerCase()) &&
                Objects.equals(model.toLowerCase(), other.model.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, brand, model);
    }
}
