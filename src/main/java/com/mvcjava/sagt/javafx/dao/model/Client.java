/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.dao.model;

import com.mvcjava.sagt.javafx.enums.ClientType;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

/**
 *
 * @author lucas
 */
public class Client {
    private UUID id;
    private String cuit_cuil;
    private String companyName;
    private ClientType clientType;
    private String phone;
    private String email;
    private String address;
    private String location;
    private String province;
    private Date entryDate;
    
    public Client() {}
    
    public Client(UUID id, String cuit_cuil, String companyName, ClientType clientType, String phone, String email, String location, String province, String address) {
        setId(id);
        setCuitCuil(cuit_cuil);
        setCompanyName(companyName);
        setClientType(clientType);
        setPhone(phone);
        setEmail(email);
        setLocation(location);
        setProvince(province);
        setAddress(address);
        setEntryDate(Date.valueOf(LocalDate.now()));
    }
    
    /**
     * 
     * SETTERS
     */
    
    public void setId(UUID id) {
        if (id != null) {
            this.id = id;
        } else {
            throw new IllegalArgumentException("El id de cliente no puede ser null");
        }
    }
    
    public void setCuitCuil(String cuit_cuil) {
        cuit_cuil = cuit_cuil.trim();
        BasicStringValidator.validate(cuit_cuil, 11, 11, "cuit/cuil");
        this.cuit_cuil = cuit_cuil;
    }
    
    public void setCompanyName(String companyName) {
        companyName = companyName.trim();
        BasicStringValidator.validate(companyName, 1, 100, "razon social");
        this.companyName = companyName;
    }
    
    public void setClientType(ClientType clientType) {
        if (clientType != null) {
            this.clientType = clientType;
        } else {
            throw new IllegalArgumentException("El tipo de cliente no puede ser null");
        }
    }
    
    public void setPhone(String phone) {
        phone = phone.trim();
        BasicStringValidator.validate(phone, 8, 20, "telefono");
        this.phone = phone;
    }
    
    public void setEmail(String email) {
        email = email.trim();
        BasicStringValidator.validate(email, 4, 255, "email");
        this.email = email;
    }
    
    public void setAddress(String address) {
        address = address.trim();
        BasicStringValidator.validate(address, 3, 100, "direccion");
        this.address = address;        
    }
    
    public void setLocation(String location) {
        location = location.trim();
        BasicStringValidator.validate(location, 3, 50, "localidad");
        this.location = location;        
    }
    
    public void setProvince(String province) {
        province = province.trim();
        BasicStringValidator.validate(province, 3, 50, "provincia");
        this.province = province;            
    }
    
    public void setEntryDate(Date entryDate) {
        if (entryDate != null) {
            this.entryDate = entryDate;
        } else {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser null");
        }
    }
    
    /**
     * 
     * GETTERS
     */
    
    public UUID getId() { return this.id; }
    public String getCuitCuil() { return this.cuit_cuil; }
    public String getCompanyName() { return this.companyName; }
    public ClientType getClientType() { return this.clientType; }
    public String getPhone() { return this.phone; }
    public String getEmail() { return this.email; }
    public String getAddress() { return this.address; }
    public String getLocation() { return this.location; }
    public String getProvince() { return this.province; }
    public Date getEntryDate() { return this.entryDate; }
    
}   
