/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.dao.model;

import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import java.util.UUID;

/**
 *
 * @author lucas
 */
public class Supplier {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String web;
    private String location;
    private String province;
    
    public Supplier() {}
    
    //SETTERS
    public void setId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El id no puede ser null.");
        }    
        this.id = id;
    }

    public void setName(String name) {
        BasicStringValidator.validate(name, 3, 30, "nombre");
        this.name = name.trim();
    }
    
    public void setPhone(String phone) {
        BasicStringValidator.validate(phone, 8, 20, "telefono");
        this.phone = phone;
    }
    
    public void setEmail(String email) {
        BasicStringValidator.validate(email, 4, 255, "email");
        this.email = email.trim();
    }
    
    public void setAddress(String direction) {
        BasicStringValidator.validate(direction, 3, 100, "direccion");
        this.address = direction.trim();
    }
    
    public void setWeb(String web) {
        BasicStringValidator.validate(web, 4, 255, "web");
        this.web = web.trim();
    }
    
    public void setLocation(String location) {
        BasicStringValidator.validate(location, 3, 50, "localidad");
        this.location = location.trim();
    }
    
    public void setProvince(String province) {
        BasicStringValidator.validate(province, 3, 50, "provincia");
        this.province = province.trim();
    }
    
    //GETTERS
    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
    
    public String getPhone() {
        return this.phone;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public String getWeb() {
        return this.web;
    }
    
    public String getLocation() {
        return this.location;
    }
    
    public String getProvince() {
        return this.province;
    }

    @Override
    public String toString() { //ComboBox usa toString para mostrar cada item al hacer items.setALl()
        return name;
    }
}
