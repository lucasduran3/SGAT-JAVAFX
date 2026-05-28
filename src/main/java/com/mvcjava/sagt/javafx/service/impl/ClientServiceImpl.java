/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.service.impl;

import com.mvcjava.sagt.javafx.dao.impl.ClientDAOImpl;
import com.mvcjava.sagt.javafx.dao.interfaces.ClientDAO;
import com.mvcjava.sagt.javafx.dao.model.Client;
import com.mvcjava.sagt.javafx.exception.BusinessException;
import com.mvcjava.sagt.javafx.service.interfaces.ClientService;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author lucas
 */
public class ClientServiceImpl implements ClientService {
    private final ClientDAO dao;
    
    public ClientServiceImpl() {
        dao = new ClientDAOImpl();
    }

    @Override
    public Set<Client> getAll() {
        return dao.findAll();
    }

    @Override
    public Client getById(UUID id) {
        return dao.findById(id);
    }

    @Override
    public void createClient(Client client) throws BusinessException {
        String cuit = client.getCuitCuil();
        boolean exists = dao.alreadyExists(client.getId(), cuit);
        if (exists) {
            throw new BusinessException("El cliente con el cuit/cuil: " + cuit + " ya existe");
        }
        
        if (!BasicStringValidator.isValidCuit(cuit)) {
            throw new BusinessException("Número de cuit/cuil inválido.");
        }
        if (!BasicStringValidator.isValidPhone(client.getPhone())) {
            throw new BusinessException("Número de teléfono inválido.");
        }
        if (!BasicStringValidator.isValidEmail(client.getEmail())) {
            throw new BusinessException("Dirección de email inválida.");
        }
        
        dao.addClient(client);
    }

    @Override
    public void updateClient(UUID id, Map<String, Object> updates) throws BusinessException {
        Client currentClient = dao.findById(id);
        if (currentClient == null) {
            throw new BusinessException("El cliente que quiere actualizar no existe.");
        }
        
        if (updates.containsKey("cuit_cuil")) {
            if (!BasicStringValidator.isValidCuit(updates.get("cuit_cuil").toString())) {
                throw new BusinessException("Número de cuit/cuil inválido.");
            } else {
                String cuit = (String) updates.get("cuit_cuil");
                boolean exists = dao.alreadyExists(id, cuit);
                if (exists) {
                    throw new BusinessException("Ya existe otro cliente con el mismo cuit/cuil.");
                }
            }
        }
        if (updates.containsKey("telefono")) {
            if (!BasicStringValidator.isValidPhone(updates.get("telefono").toString())) {
                throw new BusinessException("Número de teléfono inválido.");
            }
        }
        if (updates.containsKey("email")) {
            if (!BasicStringValidator.isValidEmail(updates.get("email").toString())) {
                throw new BusinessException("Dirección de email inválida.");
            }
        }
        
        dao.updateClient(id, updates);
    }

    @Override
    public void deleteClient(UUID id) throws BusinessException {
        Client currentClient = dao.findById(id);
        if (currentClient == null) {
            throw new BusinessException("El cliente que quiere eliminar no existe.");
        }
        
        dao.deleteClient(id);
    }

    @Override
    public void saveChanges(Set<Client> newClients, Map<UUID, Map<String, Object>> clientsToUpdate, Set<Client> clientsToDelete) throws BusinessException {
        for (Map.Entry<UUID, Map<String, Object>> entry : clientsToUpdate.entrySet()) {
            updateClient(entry.getKey(), entry.getValue());
        }
        
        for (Client client: clientsToDelete) {
            System.out.println("Eliminando clientes");
            deleteClient(client.getId());
        }
        
        for (Client newClient : newClients) {
            System.out.println("Creando clientes");
            createClient(newClient);
        }
    }
}
