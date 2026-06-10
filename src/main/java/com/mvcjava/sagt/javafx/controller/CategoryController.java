/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.async.CategoryLoadService;
import com.mvcjava.sagt.javafx.async.CategorySaveService;
import com.mvcjava.sagt.javafx.dao.model.Category;
import com.mvcjava.sagt.javafx.filter.CategoryFilterConfig;
import com.mvcjava.sagt.javafx.filter.FilterState;
import com.mvcjava.sagt.javafx.filter.FilterableTableHelper;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import com.mvcjava.sagt.javafx.util.EditableCellFactory;
import com.mvcjava.sagt.javafx.viewmodel.CategoryViewModel;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;

/**
 *
 * @author lucas
 */
public class CategoryController {
    @FXML
    private TableView<CategoryViewModel> categoriesTable;
    
    @FXML
    private TextField searchField;
    
    private TableColumn<CategoryViewModel, Boolean> selectColumn;
    private TableColumn<CategoryViewModel, UUID> idColumn;
    private TableColumn<CategoryViewModel, String> nameColumn;
    
    private ObservableList<CategoryViewModel> categoryViewModels;
    
    private Set<Category> categoriesToUpdate;
    private Set<Category> categoriesToDelete;
    
    private CategoryLoadService loadService;
    private CategorySaveService saveService;
    
    //filter
    private FilterState<CategoryViewModel> filterState;
    private FilterableTableHelper<CategoryViewModel> filterHelper;
    
    public CategoryController() {}
    
    @FXML
    public void initialize() {
        this.loadService = new CategoryLoadService();
        this.saveService = new CategorySaveService();
        
        categoryViewModels = FXCollections.observableArrayList();
        
        setupTableColumns();
        categoriesTable.setItems(categoryViewModels);
        
        loadData();
        
        categoriesToUpdate = new HashSet<>();
        categoriesToDelete = new HashSet<>();
    }
    
    private void initFilterSystem() {
        filterState = new FilterState<>();
        filterHelper = new FilterableTableHelper<>(categoryViewModels, searchField, filterState, CategoryFilterConfig::textMatch);
        categoriesTable.setItems(filterHelper.getFilteredList());
    }
    
    private void setupTableColumns() {
        selectColumn = new TableColumn<>("");
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(col -> new CheckBoxTableCell<>());
        selectColumn.setEditable(true);
        selectColumn.setResizable(false);
        selectColumn.setMaxWidth(50);
        selectColumn.setMinWidth(50);
        
        idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        idColumn.setEditable(false);
        
        nameColumn = new TableColumn<>("Nombre");
        nameColumn.setUserData("nombre");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameColumn.setCellFactory(EditableCellFactory.forString(
                3, 30, (vm, value) -> vm.nameProperty().set(value)
        ));
        nameColumn.setEditable(true);
        nameColumn.setOnEditCommit(col -> handleStringEdit(col));
        
        categoriesTable.getColumns().addAll(selectColumn, idColumn, nameColumn);
        categoriesTable.setEditable(true);
        categoriesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void loadData() {
        if (!loadService.isRunning()) {
            loadService.reset();
            loadService.setOnSucceeded(e -> {
                Set<Category> categories = loadService.getValue();
                List<CategoryViewModel> viewModels = categories.stream().map(CategoryViewModel::new).collect(Collectors.toList());
                
                if (filterHelper != null) {
                    filterHelper.setAll(viewModels);
                } else {
                    categoryViewModels.setAll(viewModels);
                    initFilterSystem();
                }
            });
            loadService.setOnFailed(e -> {
                AlertUtils.showError(e.getSource().getException().getMessage());
            });
            
            loadService.start();
        }
    }
    
    private void saveData() {
        if (!saveService.isRunning()) {
            saveService.reset();
            
            Set<Category> newCategories = categoryViewModels.stream().filter(CategoryViewModel::getIsNew).map(CategoryViewModel::getModel).collect(Collectors.toSet());
            
            saveService.setData(newCategories, categoriesToUpdate, categoriesToDelete);
            
            saveService.setOnSucceeded(e -> {
                loadData();
                AlertUtils.showSuccess("Operación exitosa", "Cambios guardados con éxito", "Categorias nuevas: " + newCategories.size() + "\nCategorias actualizadas: " + categoriesToUpdate.size() + "\nCategorias eliminadas: " + categoriesToDelete.size());
                categoriesToDelete.clear();
                categoriesToUpdate.clear();
                categoriesTable.refresh();
            });
            
            saveService.setOnFailed(e -> {
                AlertUtils.showError(e.getSource().getException().getMessage());
            });
            
            saveService.start();
        }
    }
    
    @FXML
    protected void handleAddCategory() {
        CategoryViewModel vm = CategoryFormController.showForm(categoriesTable.getScene().getWindow());
        
        if (vm != null) {
            vm.setIsNew(true);
            
            boolean existingName = categoryViewModels.stream()
                    .anyMatch(c -> Objects.equals(vm.nameProperty().get(), c.nameProperty().get()));
            
            if (existingName) {
                AlertUtils.showError("Ya existe otra categoría con el mismo nombre.");
            } else {
                categoryViewModels.add(vm);
            }
        }
    }
    
    @FXML
    protected void handleDeleteCategory() {
        Optional<ButtonType> btn = AlertUtils.showConfirmAlert("Eliminar categorías", "¿Desea eliminar las categorías seleccionadas?\nLas categorías recien agregadas no se podran recuperar.");
        
        btn.ifPresent(p -> {
            if (p == ButtonType.OK) {
                Set<CategoryViewModel> selected = categoryViewModels.stream().filter(CategoryViewModel::isSelected).collect(Collectors.toSet());
                
                for (CategoryViewModel vm : selected) {
                    if (!vm.getIsNew()) {
                        categoriesToDelete.add(vm.getModel());   
                    }
                    categoryViewModels.remove(vm);
                }
            }
        });
    }
    
    @FXML
    protected void handleSaveChanges() {
        Optional<ButtonType> btn = AlertUtils.showConfirmAlert("Guardar cambios", "¿Desea guardar los cambios en la base de datos?");
        
        btn.ifPresent(p -> {
            if (p == ButtonType.OK) {
                saveData();
            }
        });
    }
    
    @FXML
    protected void handleRefresh() {
        long newCategoryCount = categoryViewModels.stream().filter(e -> e.getIsNew()).count();
        if (newCategoryCount > 0 || !categoriesToUpdate.isEmpty() || !categoriesToDelete.isEmpty()) {
            Optional<ButtonType> confirm = AlertUtils.showConfirmAlert("Hay cambios sin guardar", 
                    "Si recargás, perderás los cambios no guardados. ¿Continuar?");
            if (confirm.isEmpty() || confirm.get() != ButtonType.OK) return;
        }
        
        categoriesTable.getSelectionModel().clearSelection();
        categoryViewModels.clear();
        categoriesToUpdate.clear();
        categoriesToDelete.clear();
        
        loadData();
    }
    
    private void handleStringEdit(TableColumn.CellEditEvent<CategoryViewModel, String> e) {
        if (!e.getOldValue().equalsIgnoreCase(e.getNewValue())) {
            CategoryViewModel vm = e.getRowValue();
            if (!vm.getIsNew()) {
                categoriesToUpdate.add(vm.getModel());
            }
        }    
    }
}
