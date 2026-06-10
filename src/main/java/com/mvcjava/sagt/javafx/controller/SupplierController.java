package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.async.SupplierLoadService;
import com.mvcjava.sagt.javafx.async.SupplierSaveService;
import com.mvcjava.sagt.javafx.dao.model.Supplier;
import com.mvcjava.sagt.javafx.filter.FilterState;
import com.mvcjava.sagt.javafx.filter.FilterableTableHelper;
import com.mvcjava.sagt.javafx.filter.SupplierFilterConfig;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import com.mvcjava.sagt.javafx.viewmodel.SupplierViewModel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javafx.scene.control.cell.TextFieldTableCell;

public class SupplierController {

    @FXML
    private TableView<SupplierViewModel> suppliersTable;
    
    @FXML
    private TextField searchField;

    private TableColumn<SupplierViewModel, Boolean> selectColumn;
    private TableColumn<SupplierViewModel, UUID> idColumn;
    private TableColumn<SupplierViewModel, String> nameColumn;
    private TableColumn<SupplierViewModel, String> phoneColumn;
    private TableColumn<SupplierViewModel, String> emailColumn;
    private TableColumn<SupplierViewModel, String> addressColumn;
    private TableColumn<SupplierViewModel, String> webColumn;
    private TableColumn<SupplierViewModel, String> locationColumn;
    private TableColumn<SupplierViewModel, String> provinceColumn;

    private ObservableList<SupplierViewModel> supplierViewModels;

    private Map<UUID, Map<String, Object>> suppliersToUpdate;
    private Set<Supplier> suppliersToDelete;

    private SupplierLoadService loadService;
    private SupplierSaveService saveService;
    
    //filter
    private FilterState<SupplierViewModel> filterState;
    private FilterableTableHelper<SupplierViewModel> filterHelper;

    public SupplierController() {}

    @FXML
    public void initialize() {
        initializeDependencies();
        supplierViewModels = FXCollections.observableArrayList();

        setupTableColumns();
        suppliersTable.setItems(supplierViewModels);

        loadData();

        suppliersToUpdate = new HashMap<>();
        suppliersToDelete = new HashSet<>();
    }

    private void initializeDependencies() {
        this.loadService = new SupplierLoadService();
        this.saveService = new SupplierSaveService();
    }
    
    private void initFilterSystem() {
        filterState = new FilterState<>();
        
        filterHelper = new FilterableTableHelper<>(supplierViewModels, searchField, filterState, SupplierFilterConfig::textMatch);
        suppliersTable.setItems(filterHelper.getFilteredList());
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

        nameColumn    = createStringColumn("Nombre",    "nombre",    SupplierViewModel::nameProperty,     3,   30,  false, false, false);
        phoneColumn   = createStringColumn("Teléfono",  "telefono",  SupplierViewModel::phoneProperty,    8,   20,  false, true,  false);
        emailColumn   = createStringColumn("Email",     "email",     SupplierViewModel::emailProperty,    4,   255, false, false, true);
        addressColumn = createStringColumn("Dirección", "direccion", SupplierViewModel::addressProperty,  3,   100, false, false, false);
        webColumn     = createStringColumn("Web",       "web",       SupplierViewModel::webProperty,      4,   255, false, false, false);
        locationColumn = createStringColumn("Localidad", "localidad", SupplierViewModel::cityProperty, 3,   50,  false, false, false);
        provinceColumn= createStringColumn("Provincia", "provincia", SupplierViewModel::provinceProperty, 3,   50,  false, false, false);

        suppliersTable.getColumns().addAll(
                selectColumn,
                idColumn,
                nameColumn,
                phoneColumn,
                emailColumn,
                addressColumn,
                webColumn,
                locationColumn,
                provinceColumn
        );

        suppliersTable.setEditable(true);
        suppliersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private TableColumn<SupplierViewModel, String> createStringColumn(
            String title,
            String dbField,
            javafx.util.Callback<SupplierViewModel, javafx.beans.property.StringProperty> propertyGetter,
            int minLength,
            int maxLength,
            boolean validateCuit,
            boolean validatePhone,
            boolean validateEmail
    ) {
        TableColumn<SupplierViewModel, String> column = new TableColumn<>(title);
        column.setUserData(dbField);
        column.setCellValueFactory(cellData -> propertyGetter.call(cellData.getValue()));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setEditable(true);

        column.setOnEditCommit(e -> {
            String newValue = normalize(e.getNewValue());
            String oldValue = normalize(e.getOldValue());

            if (newValue.equals(oldValue)) return;

            try {
                BasicStringValidator.validate(newValue, minLength, maxLength, title.toLowerCase());
                if (validatePhone && !BasicStringValidator.isValidPhone(newValue)) {
                    throw new IllegalArgumentException("Número de teléfono inválido.");
                }
                if (validateEmail && !BasicStringValidator.isValidEmail(newValue)) {
                    throw new IllegalArgumentException("Dirección de email inválida.");
                }

                SupplierViewModel vm = e.getRowValue();
                propertyGetter.call(vm).set(newValue);
                registerUpdate(vm.getId(), dbField, newValue);
            } catch (IllegalArgumentException ex) {
                AlertUtils.showError(ex.getMessage());
                suppliersTable.refresh();
            }
        });

        return column;
    }

    private void registerUpdate(UUID supplierId, String fieldName, Object value) {
        suppliersToUpdate.computeIfAbsent(supplierId, k -> new HashMap<>()).put(fieldName, value);
    }

    private void loadData() {
        if (loadService.isRunning()) return;

        loadService.reset();

        loadService.setOnSucceeded(e -> {
            java.util.List<Supplier> suppliers = loadService.getValue();
            List<SupplierViewModel> viewModels = suppliers.stream().map(SupplierViewModel::new).collect(Collectors.toList());
            
            if (filterHelper != null) {
                filterHelper.setAll(viewModels);
            } else {
                supplierViewModels.setAll(viewModels);
                initFilterSystem();
            }
        });

        loadService.setOnFailed(e ->
                AlertUtils.showError(e.getSource().getException().getMessage())
        );

        loadService.start();
    }

    private void saveData() {
        if (saveService.isRunning()) return;

        saveService.reset();

        List<Supplier> newSuppliers = supplierViewModels.stream()
                .filter(SupplierViewModel::getIsNew)
                .map(SupplierViewModel::getModel)
                .collect(Collectors.toList());

        List<Supplier> deleted = suppliersToDelete.stream()
                .collect(Collectors.toList());

        saveService.setData(newSuppliers, suppliersToUpdate, deleted);

        saveService.setOnSucceeded(e -> {
            loadData();
            AlertUtils.showSuccess(
                    "Operación exitosa",
                    "Cambios guardados con éxito",
                    "Proveedores nuevos: " + newSuppliers.size()
                    + "\nProveedores actualizados: " + suppliersToUpdate.size()
                    + "\nProveedores eliminados: " + suppliersToDelete.size()
            );
            suppliersToDelete.clear();
            suppliersToUpdate.clear();
            suppliersTable.refresh();
        });

        saveService.setOnFailed(e ->
            AlertUtils.showError(e.getSource().getException().getMessage())
        );

        saveService.start();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    @FXML
    protected void handleAddSupplier() {
        SupplierViewModel newSupplier = SupplierFormController.showSupplierForm(suppliersTable.getScene().getWindow());

        if (newSupplier != null) {
            newSupplier.setIsNew(true);

            boolean namePhoneExists = supplierViewModels.stream().anyMatch(s ->
                    s.nameProperty().get().equalsIgnoreCase(newSupplier.nameProperty().get()) &&
                    s.phoneProperty().get().equals(newSupplier.phoneProperty().get())
            );

            if (namePhoneExists) {
                AlertUtils.showError("Ya existe un proveedor con el mismo nombre y teléfono.");
            } else {
                supplierViewModels.add(newSupplier);
            }
        }
    }

    @FXML
    protected void handleDeleteSupplier() {
        Optional<ButtonType> btn = AlertUtils.showConfirmAlert(
                "Eliminar proveedores",
                "¿Desea eliminar los proveedores seleccionados?\nLos proveedores recién agregados no se podrán recuperar."
        );

        btn.ifPresent(p -> {
            if (p == ButtonType.OK) {
                Set<SupplierViewModel> selected = supplierViewModels.stream()
                        .filter(SupplierViewModel::isSelected)
                        .collect(Collectors.toSet());

                for (SupplierViewModel vm : selected) {
                    if (!vm.getIsNew()) {
                        suppliersToDelete.add(vm.getModel());   
                    }
                    supplierViewModels.remove(vm);
                }
            }
        });
    }

    @FXML
    protected void handleSaveChanges() {
        Optional<ButtonType> btn = AlertUtils.showConfirmAlert(
                "Guardar cambios",
                "¿Desea guardar los cambios en la base de datos?"
        );

        btn.ifPresent(p -> {
            if (p == ButtonType.OK) {
                saveData();
            }
        });
    }
    
    @FXML
    protected void handleRefresh() {
        long newSuppliersCount = supplierViewModels.stream().filter(SupplierViewModel::getIsNew).count();
        if (newSuppliersCount > 0 || !suppliersToUpdate.isEmpty() || !suppliersToDelete.isEmpty()) {
            Optional<ButtonType> confirm = AlertUtils.showConfirmAlert("Hay cambios sin guardar", 
                    "Si recargás, perderás los cambios no guardados. ¿Continuar?");
            if (confirm.isEmpty() || confirm.get() != ButtonType.OK) return;
        }
        
        suppliersTable.getSelectionModel().clearSelection();
        supplierViewModels.clear();
        suppliersToUpdate.clear();
        suppliersToDelete.clear();
        
        loadData();
    }
}