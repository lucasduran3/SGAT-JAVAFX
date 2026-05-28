/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.async.ProductLoadService;
import com.mvcjava.sagt.javafx.async.ProductSaveService;
import com.mvcjava.sagt.javafx.dao.model.Category;
import com.mvcjava.sagt.javafx.dao.model.Product;
import com.mvcjava.sagt.javafx.service.interfaces.ProductService;
import com.mvcjava.sagt.javafx.service.interfaces.SupplierService;
import com.mvcjava.sagt.javafx.dao.model.Supplier;
import com.mvcjava.sagt.javafx.dto.ProductViewData;
import com.mvcjava.sagt.javafx.service.impl.ProductServiceImpl;
import com.mvcjava.sagt.javafx.service.impl.SupplierServiceImpl;
import com.mvcjava.sagt.javafx.viewmodel.ProductViewModel;
import com.mvcjava.sagt.javafx.dto.ProductWithRelations;
import com.mvcjava.sagt.javafx.service.impl.CategoryServiceImpl;
import com.mvcjava.sagt.javafx.service.interfaces.CategoryService;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import com.mvcjava.sagt.javafx.util.EditableCellFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.HBox;

/**
 *
 * @author lucas
 */
public class ProductController {
    private ProductService productService;
    private SupplierService supplierService;
    private CategoryService categoryService;
    
    @FXML 
    private TableView<ProductViewModel> productsTable;
    
    private TableColumn<ProductViewModel, Boolean> selectColumn;
    private TableColumn<ProductViewModel, UUID> idColumn;
    private TableColumn<ProductViewModel, String> nameColumn;
    private TableColumn<ProductViewModel, String> brandColumn;
    private TableColumn<ProductViewModel, String> modelColumn;
    private TableColumn<ProductViewModel, Number> purchasePriceColumn;
    private TableColumn<ProductViewModel, Number> salePriceColumn;
    private TableColumn<ProductViewModel, Number> stockColumn;
    private TableColumn<ProductViewModel, Number> minStockColumn;
    private TableColumn<ProductViewModel, Supplier> supplierColumn;
    private TableColumn<ProductViewModel, String> loadedByNameColumn;
    private TableColumn<ProductViewModel, Timestamp> entryDateColumn;
    private TableColumn<ProductViewModel, Timestamp> updateDateColumn;
    private TableColumn<ProductViewModel, String> categoriesColumn;
    
    private ObservableList<ProductViewModel> productViewModels;
    private ObservableList<Supplier> avaibleSuppliers;
    private ObservableSet<Category> avaibleCategories;
    
    private Map<UUID, Map<String, Object>> productsToUpdate;
    private Map<UUID, Set<UUID>> categoriesToUpdate;
    private Set<ProductViewModel> productsToDelete;
    
    //Async helpers
    private ProductLoadService loadService;
    private ProductSaveService saveService;
    
    public ProductController() {}
    
    @FXML
    public void initialize() {
        initializeDependencies();
        productViewModels = FXCollections.observableArrayList();
        avaibleSuppliers = FXCollections.observableArrayList();
        avaibleCategories = FXCollections.observableSet();
        
        setupTableColumns();
        productsTable.setItems(productViewModels);
        
        loadData();
        
        productsToUpdate = new HashMap<>();
        categoriesToUpdate = new HashMap<>();
        productsToDelete = new HashSet<>();
    }
    
    private void initializeDependencies() {
        this.productService = new ProductServiceImpl();
        this.supplierService = new SupplierServiceImpl();
        this.categoryService = new CategoryServiceImpl();
        this.loadService = new ProductLoadService(productService, supplierService, categoryService);
        this.saveService = new ProductSaveService(productService);
    }
    
    private void setupTableColumns() {
        selectColumn = new TableColumn<>();
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(col -> new CheckBoxTableCell<>());
        selectColumn.setEditable(true);
        selectColumn.setResizable(false);
        selectColumn.setMaxWidth(50);
        selectColumn.setMinWidth(50);
        
        idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getId())
        );
        idColumn.setEditable(false);
        
        nameColumn = new TableColumn<>("Nombre");
        nameColumn.setUserData("nombre");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameColumn.setCellFactory(EditableCellFactory.forString(
                3,
                100,
                (vm, value) -> vm.nameProperty().set(value))
        );
        nameColumn.setEditable(true);
        nameColumn.setOnEditCommit(col -> handleStringEdit(col));
        
        brandColumn = new TableColumn<>("Marca");
        brandColumn.setUserData("marca");
        brandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());
        brandColumn.setCellFactory(EditableCellFactory.forString(
                1,
                100,
                (vm, value) -> vm.brandProperty().set(value))
        );
        brandColumn.setEditable(true);
        brandColumn.setOnEditCommit(col -> handleStringEdit(col));
        
        modelColumn = new TableColumn<>("Modelo");
        modelColumn.setUserData("modelo");
        modelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        modelColumn.setCellFactory(EditableCellFactory.forString(
                1,
                100,
                (vm, value) -> vm.modelProperty().set(value))
        );
        modelColumn.setEditable(true);
        modelColumn.setOnEditCommit(col -> handleStringEdit(col));
        
        purchasePriceColumn = new TableColumn<>("Precio de compra");
        purchasePriceColumn.setUserData("precio_compra");
        purchasePriceColumn.setCellValueFactory(cellData -> cellData.getValue().purchasePriceProperty());
        purchasePriceColumn.setCellFactory(EditableCellFactory.forNumber(
                price -> (price >= 0), "El precio no puede ser negativo", 
                (vm, value) -> vm.purchasePriceProperty().set(value.floatValue()),
                true)
        );
        purchasePriceColumn.setEditable(true);
        purchasePriceColumn.setOnEditCommit(this::handleNumberEdit);
        purchasePriceColumn.getStyleClass().add("highlited-text-blue");
        
        salePriceColumn = new TableColumn<>("Precio de venta");
        salePriceColumn.setUserData("precio_venta");
        salePriceColumn.setCellValueFactory(cellData -> cellData.getValue().salePriceProperty());
        salePriceColumn.setCellFactory(EditableCellFactory.forNumber(
                price -> price >= 0,
                "El precio no puede ser negativo",
                (vm, value) -> vm.salePriceProperty().set(value.floatValue()),
                true)
        );
        salePriceColumn.setEditable(true);
        salePriceColumn.setOnEditCommit(this::handleNumberEdit);
        salePriceColumn.getStyleClass().add("highlited-text-green");
        
        stockColumn = new TableColumn<>("Stock");
        stockColumn.setUserData("stock");
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty());
        stockColumn.setCellFactory(EditableCellFactory.forNumber(
                stock -> stock >= 0,
                "El stock no puede ser negativo", 
                (vm, value) -> vm.stockProperty().set(value.intValue()),
                false)
        );
        stockColumn.setEditable(true);
        stockColumn.setOnEditCommit(this::handleNumberEdit);
        
        minStockColumn = new TableColumn<>("Stock minimo");
        minStockColumn.setUserData("stock_minimo");
        minStockColumn.setCellValueFactory(cellData -> cellData.getValue().minStockProperty());
        minStockColumn.setCellFactory(EditableCellFactory.forNumber(
                stock -> stock >= 0,
                "El stock minimo no puede ser negativo", 
                (vm, value) -> vm.minStockProperty().set(value.intValue()),
                false)
        );
        minStockColumn.setEditable(true);
        minStockColumn.setOnEditCommit(this::handleNumberEdit);
        
        supplierColumn = new TableColumn<>("Proveedor");
        supplierColumn.setUserData("id_proveedor");
        supplierColumn.setCellValueFactory(cellData -> 
            cellData.getValue().supplierProperty()
        );
        
        //configurar comboBox
        supplierColumn.setCellFactory(col -> {
            ComboBoxTableCell<ProductViewModel, Supplier> cell = 
                    new ComboBoxTableCell<ProductViewModel, Supplier>() {
                    
                        @Override
                        public void updateItem(Supplier item, boolean empty) {
                            super.updateItem(item, empty);
                            
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getName());
                            }
                        }
                    };
            cell.getItems().setAll(avaibleSuppliers);
            
            return cell;
        });
        supplierColumn.setEditable(true);
        supplierColumn.setOnEditCommit(col -> handleSupplierEdit(col));
        
        loadedByNameColumn = new TableColumn<>("Cargado por");
        loadedByNameColumn.setCellValueFactory(cellData -> 
                cellData.getValue().loadedByNameProperty()
        );
        loadedByNameColumn.setEditable(false);
        
        entryDateColumn = new TableColumn<>("Fecha de carga");
        entryDateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getEntryDate())
        );
        entryDateColumn.setEditable(false);
        
        updateDateColumn = new TableColumn<>("Fecha de actualizacion");
        updateDateColumn.setCellValueFactory(cellData -> 
            cellData.getValue().updateDateProperty()
        );
        updateDateColumn.setEditable(false);
        
        categoriesColumn = new TableColumn<>("Categorias");
        categoriesColumn.setCellValueFactory(cellData -> cellData.getValue().categoriesDisplayProperty());
        categoriesColumn.setCellFactory(col -> {
            return new TableCell<ProductViewModel, String>() {
                private final Label label = new Label();
                private final HBox container = new HBox(10, label);
                {
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setPadding(new Insets(2,5,2,5));
                    
                    container.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !isEmpty()) { // Doble click
                            ProductViewModel vm = getTableRow().getItem();
                            if (vm != null) {
                                openCategoriesDialog(vm);
                            }
                        }
                    });
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        label.setText(item);
                        setGraphic(container);
                    }
                }
            };
        });
        categoriesColumn.setEditable(true);
        
        productsTable.getColumns().addAll(
                selectColumn,
                idColumn,
                nameColumn,
                brandColumn,
                modelColumn,
                categoriesColumn,
                purchasePriceColumn,
                salePriceColumn,
                stockColumn,
                minStockColumn,
                supplierColumn,
                loadedByNameColumn,
                entryDateColumn,
                updateDateColumn
        );
        
        productsTable.setEditable(true);
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    @FXML
    protected void handleAddProduct(ActionEvent e) {
        ProductViewModel newProduct = ProductFormController.showProductForm(
                avaibleCategories, 
                avaibleSuppliers,
                productsTable.getScene().getWindow()
        );
        
        if (newProduct != null) {
            newProduct.setIsNew(true);
            if (!productViewModels.contains(newProduct)) {
                productViewModels.add(newProduct);
            } else {
                AlertUtils.showError("Ya existe otro producto con el mismo nombre, marca y modelo.");
            }
        }
    }
    
    @FXML
    protected void handleDeleteProduct(ActionEvent e) {
        Optional<ButtonType> btn = AlertUtils
                .showConfirmAlert("Eliminar productos", "Desea eliminar los productos seleccionados?"
                        + "\nLos nuevos productos sin guardar no se podran recuperar");
        
        btn.ifPresent(p -> {
            if (p == ButtonType.OK) {
                List<ProductViewModel> selected = productViewModels.stream().filter(ProductViewModel::isSelected).collect(Collectors.toList());
                
                for (ProductViewModel vm : selected) {
                    productsToDelete.add(vm);
                    productViewModels.remove(vm);
                }
            }
        });
        
        productsTable.refresh();
    }
    
    @FXML
    protected void refresh(ActionEvent e) {
        productsTable.refresh();
    }
    
    @FXML
    protected void handleSaveChanges(ActionEvent e) {
        Optional<ButtonType> btn = AlertUtils
                .showConfirmAlert("Guardar cambios", "Desea guardar los cambios en la base de datos?");
        
        btn.ifPresent(p -> {
            if (p == ButtonType.OK) {
                saveData();
            }
        });
    }
    
    private void saveData() {
        if (!saveService.isRunning()) {
            saveService.reset();
            
            List<ProductViewModel> newProductsVm = productViewModels.stream().filter(v -> v.getIsNew() == true).collect(Collectors.toList());
            Map<Product, Set<UUID>> newProductsWithCategories = new HashMap<>();
        
            for (ProductViewModel vm : newProductsVm) {
                Set<UUID> categories = vm.getCategories().stream().map(Category::getId).collect(Collectors.toSet());
                newProductsWithCategories.put(vm.getModel(), categories);
            }
        
            Set<UUID> deletedProducts = productsToDelete.stream().filter(v -> v.getIsNew() == false).map(ProductViewModel::getId).collect(Collectors.toSet());
            
            saveService.setDataToSave(newProductsWithCategories, productsToUpdate, categoriesToUpdate, deletedProducts);
            
            saveService.setOnSucceeded(e -> {
                loadData();
                AlertUtils.showSuccess("Operación exitosa", "Cambios guardados con éxito", "Productos nuevos: " + newProductsVm.size()  + "\nProductos actualizados: " + productsToUpdate.size() + "\nProductos eliminados: " + productsToDelete.size());
                productsToDelete.clear();
                productsToUpdate.clear();
                categoriesToUpdate.clear();
                productsTable.refresh();                
            });
            saveService.setOnFailed(e -> AlertUtils.showError(e.getSource().getException().getMessage()));
            
            saveService.start();
        }
    }
    
    private void loadData() {
        if (!loadService.isRunning()) {
            loadService.reset();
            
            loadService.setOnSucceeded(e -> {
                ProductViewData viewData = loadService.getValue();
            
                avaibleCategories.addAll(viewData.getCategories());
                avaibleSuppliers.setAll(viewData.getSuppliers());
            
                List<ProductWithRelations> products = viewData.getProducts();
           
                List<ProductViewModel> viewModels = new ArrayList<>();
                for (ProductWithRelations dto : products) {
                    UUID supplierId = dto.getProduct().getIdSupplier();
                    Supplier supplier = findSupplierById(supplierId);
               
                    ProductViewModel productVm = new ProductViewModel(dto, supplier);
                    viewModels.add(productVm);
                }
           
                productViewModels.setAll(viewModels);
                });
        
            loadService.setOnFailed(e -> {
                AlertUtils.showError(e.getSource().getException().getMessage());
            });
            
            loadService.start();
        }
    }
    
    private Supplier findSupplierById(UUID id) {
        if (id == null) {
            return null;
        }
        
        return avaibleSuppliers.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    private void openCategoriesDialog(ProductViewModel viewModel) {
        Set<Category> result = new HashSet<>(CheckBoxDialogController.showDialog(
                productsTable.getScene().getWindow(),
                "Gestionar categorias",
                "Producto: " + viewModel.nameProperty().getValue(),
                "Seleccione las categorias que desea agregar a este producto: ",
                new ArrayList<>(avaibleCategories),
                new ArrayList<>(viewModel.getCategories())
        ));
        
        if (result != null) {
            if (!viewModel.getCategories().equals(result)) {
                viewModel.setCategories(new HashSet(result));
                
                if (!viewModel.getIsNew()) {
                    Set<UUID> idsCategories = result.stream().map(Category::getId).collect(Collectors.toSet());
                    categoriesToUpdate.put(viewModel.getId(), idsCategories);
                
                    productsToUpdate.computeIfAbsent(viewModel.getId(), v -> new HashMap<>())
                            .put("fecha_actualizacion", Timestamp.from(Instant.now()));  
                }
            } else {
                System.out.println("El resultado es el mismo");
            }
        }     
    }
    
    private void handleStringEdit(TableColumn.CellEditEvent<ProductViewModel, String> e) {
        if (!e.getOldValue().equalsIgnoreCase(e.getNewValue())) {
            ProductViewModel vm = e.getRowValue();
            UUID productId = vm.getId();
            String value = e.getNewValue();
            String fieldName = e.getTableColumn().getUserData().toString();
        
            if (!vm.getIsNew()) {
                //ComputeIfAbsent siempre devuelve ese mapa asociado a la id, sea viejo o nuevo
                productsToUpdate.computeIfAbsent(productId, v -> new HashMap<>()).put(fieldName, value);
                productsToUpdate.get(productId).put("fecha_actualizacion", Timestamp.from(Instant.now()));
            }
        }
    }
    
    private void handleNumberEdit(TableColumn.CellEditEvent<ProductViewModel, Number> e) {
        if (e.getOldValue() != e.getNewValue()) {
            ProductViewModel vm = e.getRowValue();
            UUID productId = vm.getId();
            Number value = e.getNewValue();
            String fieldName = e.getTableColumn().getUserData().toString();
            
            if (!vm.getIsNew()) {
               productsToUpdate.computeIfAbsent(productId, v -> new HashMap<>()).put(fieldName, value);
               productsToUpdate.get(productId).put("fecha_actualizacion", Timestamp.from(Instant.now()));
            }
        }
    }
    
    private void handleSupplierEdit(TableColumn.CellEditEvent<ProductViewModel, Supplier> e) {
        if (!e.getNewValue().equals(e.getOldValue())) {
            ProductViewModel vm = e.getRowValue();
            UUID productId = vm.getId();
            Supplier value = e.getNewValue();
            String fieldName = e.getTableColumn().getUserData().toString();
            
            vm.supplierProperty().set(value);
            
            if (!vm.getIsNew()) {
                productsToUpdate.computeIfAbsent(productId, v -> new HashMap<>()).put(fieldName, value.getId());
                productsToUpdate.get(productId).put("fecha_actualizacion", Timestamp.from(Instant.now()));
            }
        }
    }
}
