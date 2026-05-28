/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.service.impl;

import com.mvcjava.sagt.javafx.dao.impl.CategoryDAOImpl;
import com.mvcjava.sagt.javafx.dao.interfaces.CategoryDAO;
import com.mvcjava.sagt.javafx.dao.model.Category;
import com.mvcjava.sagt.javafx.exception.BusinessException;
import com.mvcjava.sagt.javafx.service.interfaces.CategoryService;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author lucas
 */
public class CategoryServiceImpl implements CategoryService {
    private final CategoryDAO dao;
    
    public CategoryServiceImpl() {
        this.dao = new CategoryDAOImpl();
    }

    @Override
    public Set<Category> getAll() {
        return this.dao.findAll();
    }

    @Override
    public Category getById(UUID id) {
        return this.dao.findById(id);
    }

    @Override
    public void createCategory(Category category) throws BusinessException {
        boolean exists = dao.alreadyExists(category.getId(), category.getName());
        if (exists) {
            throw new BusinessException("La categoria " + category.getName() + " ya existe.");
        }
        
        dao.addCategory(category);
    }

    @Override
    public void updateCategory(Category category) throws BusinessException {
        Category currentCategory = getById(category.getId());
        if (currentCategory == null) {
            throw new BusinessException("La categoria que quiere actualizar no existe.");
        }
        dao.updateCategory(category);
    }

    @Override
    public void deleteCategory(UUID id) throws BusinessException {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException("La categoria que quiere eliminar no existe.");
        }
        dao.deleteCategory(id);
    }

    @Override
    public void saveChanges(Set<Category> newCategories, Set<Category> updates, Set<Category> categoriesToDelete) throws BusinessException {
        for (Category c : updates) {
            updateCategory(c);
        }
        
        for (Category c : categoriesToDelete) {
            deleteCategory(c.getId());
        }
        
        for (Category c : newCategories) {
            createCategory(c);
        }
    }
}
