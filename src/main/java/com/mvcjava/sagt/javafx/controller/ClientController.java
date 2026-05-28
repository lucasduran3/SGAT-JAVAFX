/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.async.ClientLoadService;
import com.mvcjava.sagt.javafx.async.ClientSaveService;
import com.mvcjava.sagt.javafx.dao.model.Client;
import com.mvcjava.sagt.javafx.enums.ClientType;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import com.mvcjava.sagt.javafx.util.BasicStringValidator;
import com.mvcjava.sagt.javafx.viewmodel.ClientViewModel;
import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;

/**
 *
 * @author lucas
 */
public class ClientController {
    @FXML
    private TableView<ClientViewModel> clientsTable;

    private TableColumn<ClientViewModel, Boolean> selectColumn;
    private TableColumn<ClientViewModel, UUID> idColumn;
    private TableColumn<ClientViewModel, String> cuitColumn;
    private TableColumn<ClientViewModel, String> companyNameColumn;
    private TableColumn<ClientViewModel, ClientType> clientTypeColumn;
    private TableColumn<ClientViewModel, String> phoneColumn;
    private TableColumn<ClientViewModel, String> emailColumn;
    private TableColumn<ClientViewModel, String> addressColumn;
    private TableColumn<ClientViewModel, String> locationColumn;
    private TableColumn<ClientViewModel, String> provinceColumn;
    private TableColumn<ClientViewModel, Date> entryDateColumn;

    private ObservableList<ClientViewModel> clientViewModels;

    private Map<UUID, Map<String, Object>> clientsToUpdate;
    private Set<ClientViewModel> clientsToDelete;
    
    private ClientLoadService loadService;
    private ClientSaveService saveService;

    public ClientController() {}

    @FXML
    public void initialize() {
        initializeDependencies();
        clientViewModels = FXCollections.observableArrayList();

        setupTableColumns();
        clientsTable.setItems(clientViewModels);

        loadData();

        clientsToUpdate = new HashMap<>();
        clientsToDelete = new HashSet<>();
    }

    private void initializeDependencies() {
        this.loadService = new ClientLoadService();
        this.saveService = new ClientSaveService();
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

        cuitColumn = createStringColumn("CUIT/CUIL", "cuit_cuil", ClientViewModel::cuitCuilProperty, 11, 11, true, false, false);
        companyNameColumn = createStringColumn("Razón social", "razon_social", ClientViewModel::companyNameProperty, 1, 100, false, false, false);
        phoneColumn = createStringColumn("Teléfono", "telefono", ClientViewModel::phoneProperty, 8, 20, false, true, false);
        emailColumn = createStringColumn("Email", "email", ClientViewModel::emailProperty, 4, 255, false, false, true);
        addressColumn = createStringColumn("Dirección", "direccion", ClientViewModel::addressProperty, 3, 100, false, false, false);
        locationColumn = createStringColumn("Localidad", "localidad", ClientViewModel::locationProperty, 3, 50, false, false, false);
        provinceColumn = createStringColumn("Provincia", "provincia", ClientViewModel::provinceProperty, 3, 50, false, false, false);

        clientTypeColumn = new TableColumn<>("Tipo");
        clientTypeColumn.setUserData("tipo");
        clientTypeColumn.setCellValueFactory(cellData -> cellData.getValue().clientTypeProperty());
        clientTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(ClientType.values()));
        clientTypeColumn.setOnEditCommit(this::handleClientTypeEdit);
        clientTypeColumn.setEditable(true);

        entryDateColumn = new TableColumn<>("Fecha de alta");
        entryDateColumn.setCellValueFactory(cellData -> cellData.getValue().entryDateProperty());
        entryDateColumn.setEditable(false);

        clientsTable.getColumns().addAll(
                selectColumn,
                idColumn,
                cuitColumn,
                companyNameColumn,
                clientTypeColumn,
                phoneColumn,
                emailColumn,
                addressColumn,
                locationColumn,
                provinceColumn,
                entryDateColumn
        );

        clientsTable.setEditable(true);
        clientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private TableColumn<ClientViewModel, String> createStringColumn(
            String title,
            String dbField,
            javafx.util.Callback<ClientViewModel, javafx.beans.property.StringProperty> propertyGetter,
            int minLength,
            int maxLength,
            boolean validateCuit,
            boolean validatePhone,
            boolean validateEmail
    ) {
        TableColumn<ClientViewModel, String> column = new TableColumn<>(title);
        column.setUserData(dbField);
        column.setCellValueFactory(cellData -> propertyGetter.call(cellData.getValue()));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setEditable(true);

        column.setOnEditCommit(e -> {
            String newValue = normalize(e.getNewValue());
            String oldValue = normalize(e.getOldValue());

            if (newValue.equals(oldValue)) {
                return;
            }

            try {
                BasicStringValidator.validate(newValue, minLength, maxLength, title.toLowerCase());
                if (validateCuit && !BasicStringValidator.isValidCuit(newValue)) {
                    throw new IllegalArgumentException("Número de cuit/cuil inválido.");
                }
                if (validatePhone && !BasicStringValidator.isValidPhone(newValue)) {
                    throw new IllegalArgumentException("Número de teléfono inválido.");
                }
                if (validateEmail && !BasicStringValidator.isValidEmail(newValue)) {
                    throw new IllegalArgumentException("Dirección de email inválida.");
                }

                ClientViewModel vm = e.getRowValue();
                propertyGetter.call(vm).set(newValue);
                registerUpdate(vm.getId(), dbField, newValue);
            } catch (IllegalArgumentException ex) {
                AlertUtils.showError(ex.getMessage());
                clientsTable.refresh();
            }
        });

        return column;
    }

    private void handleClientTypeEdit(TableColumn.CellEditEvent<ClientViewModel, ClientType> e) {
        ClientType newValue = e.getNewValue();
        ClientType oldValue = e.getOldValue();

        if (newValue == null || newValue == oldValue) {
            return;
        }

        ClientViewModel vm = e.getRowValue();
        vm.clientTypeProperty().set(newValue);
        registerUpdate(vm.getId(), "tipo", newValue.name());
    }

    private void registerUpdate(UUID clientId, String fieldName, Object value) {
        clientsToUpdate.computeIfAbsent(clientId, k -> new HashMap<>()).put(fieldName, value);
    }

    private void loadData() {
        if (!loadService.isRunning()) {
            loadService.reset();
            
            loadService.setOnSucceeded(e -> {
                Set<Client> clients = loadService.getValue();
                clientViewModels.setAll(clients.stream().map(ClientViewModel::new).collect(Collectors.toList()));
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
            
            Set<Client> newClients = clientViewModels.stream().filter(ClientViewModel::getIsNew).map(ClientViewModel::getModel).collect(Collectors.toSet());
            Set<Client> deleted = clientsToDelete.stream().map(ClientViewModel::getModel).collect(Collectors.toSet());
            
            saveService.setData(newClients, clientsToUpdate, deleted);
            
            saveService.setOnSucceeded(e -> {
                loadData();
                AlertUtils.showSuccess("Operación exitosa", "Cambios guardados con éxito", "Productos nuevos: " + newClients.size()  + "\nProductos actualizados: " + clientsToUpdate.size() + "\nProductos eliminados: " + clientsToDelete.size());
                clientsToDelete.clear();
                clientsToUpdate.clear();
                clientsTable.refresh();                
            });
            
            saveService.setOnFailed(e -> {
                AlertUtils.showError(e.getSource().getException().getMessage());
            });
            
            saveService.start();
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
    
    @FXML
    protected void handleAddClient() {
        ClientViewModel newClient = ClientFormController.showClientForm(clientsTable.getScene().getWindow());
        
        if (newClient != null) {
            newClient.setIsNew(true);
            
            boolean existingCuit = clientViewModels.stream()
                .anyMatch(c -> Objects.equals(newClient.cuitCuilProperty().get(), c.cuitCuilProperty().get()));
            
            if (existingCuit) {
                AlertUtils.showError("Ya existe otro cliente con el mismo cuit/cuil");
            } else {
                clientViewModels.add(newClient);
            }
        }
    }
    
    @FXML
    protected void handleDeleteClient() {
        Optional<ButtonType> btn = AlertUtils
                .showConfirmAlert("Eliminar clientes", "¿Desea eliminar los clientes seleccionados?"
                + "\nLos clientes recien agregados no se podrán recuperar.");
        
        btn.ifPresent(p -> {
            if (p == ButtonType.OK) {
                Set<ClientViewModel> selected = clientViewModels.stream().filter(c -> c.isSelected()).collect(Collectors.toSet());
                
                for (ClientViewModel vm : selected) {
                    if (!vm.getIsNew()) {
                        clientsToDelete.add(vm);
                    }
                    clientViewModels.remove(vm);
                }
            }
        });
    }
    
    @FXML
    protected void handleSaveChanges() {
        Optional<ButtonType> btn = AlertUtils
                .showConfirmAlert("Guardar cambios", "Desea guardar los cambios en la base de datos?");
        
        btn.ifPresent(p -> {
            if (p == ButtonType.OK) {
                saveData();
            }
        });        
    }
}
