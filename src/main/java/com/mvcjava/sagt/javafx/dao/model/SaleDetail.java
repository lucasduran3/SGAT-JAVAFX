/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.dao.model;

import java.util.UUID;

/**
 *
 * @author lucas
 */
public class SaleDetail {
    private UUID id;
    private UUID saleId;
    private UUID productId;
    private float unitPrice;
    private int ammount;
    private float subtotal;
    
    public SaleDetail() {}
    
    public SaleDetail(UUID id, UUID saleId, UUID productId, float unitPrice, int ammount, float subtotal) {
        this.setId(id);
        this.setSaleId(saleId);
        this.setProductId(productId);
        this.setUnitPrice(unitPrice);
        this.setAmmount(ammount);
        this.setSubtotal(subtotal);
    }
    
    //SETTERS
    
    public void setId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de venta detalle no puede ser null.");
        }
        this.id = id;
    }
    
    public void setSaleId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la cabecera de venta no puede ser null.");
        }
        this.saleId = id;
    }
    
    public void setProductId(UUID id) {
        this.productId = id;
    }
    
    public void setUnitPrice(float price) {
        if (price < 0 ) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
        this.unitPrice = price;
    }
    
    public void setAmmount(int ammount) {
        if (ammount < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        this.ammount = ammount;
    }
    
    public void setSubtotal(float subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("El subtotal no puede ser negativo");
        }
        this.subtotal = subtotal;
    }
    
    // GETTERS
    
    public UUID getId() {
        return this.id;
    }
    
    public UUID getSaleId() {
        return this.saleId;
    }
    
    public UUID getProductId() {
        return this.productId;
    }
    
    public float getUnitPrice() {
        return this.unitPrice;
    }
    
    public int getAmmount() {
        return this.ammount;
    }
    
    public float getSubtotal() {
        return this.subtotal;
    }
}
