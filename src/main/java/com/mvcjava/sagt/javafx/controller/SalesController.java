/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.async.BasicProductLoadService;
import com.mvcjava.sagt.javafx.async.ClientLoadService;
import com.mvcjava.sagt.javafx.async.SaleDetailLoadService;
import com.mvcjava.sagt.javafx.async.SaleLoadService;
import com.mvcjava.sagt.javafx.async.SaleSaveService;
import com.mvcjava.sagt.javafx.auth.SessionContext;
import com.mvcjava.sagt.javafx.dao.model.Client;
import com.mvcjava.sagt.javafx.dao.model.Product;
import com.mvcjava.sagt.javafx.dao.model.SaleDetail;
import com.mvcjava.sagt.javafx.dao.model.SaleHeader;
import com.mvcjava.sagt.javafx.dto.DetailSaleWithProduct;
import com.mvcjava.sagt.javafx.dto.HeaderSaleWithClient;
import com.mvcjava.sagt.javafx.dto.SaleDetailFormData;
import com.mvcjava.sagt.javafx.dto.SaleHeaderFormData;
import com.mvcjava.sagt.javafx.enums.PaymentMethod;
import com.mvcjava.sagt.javafx.filter.FilterGroup;
import com.mvcjava.sagt.javafx.filter.FilterState;
import com.mvcjava.sagt.javafx.filter.FilterableTableHelper;
import com.mvcjava.sagt.javafx.filter.SaleFilterConfig;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import com.mvcjava.sagt.javafx.util.DatePickerTableCell;
import com.mvcjava.sagt.javafx.util.EditableCellFactory;
import com.mvcjava.sagt.javafx.viewmodel.DetailSaleViewModel;
import com.mvcjava.sagt.javafx.viewmodel.SaleViewModel;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;

/**
 *
 * @author lucas
 */
public class SalesController {
    @FXML
    private TableView<SaleViewModel> salesTable;
    @FXML
    private Label detailsLabel;
    @FXML
    private TableView<DetailSaleViewModel> detailTable;
    @FXML
    private Button addDetailBtn;
    @FXML
    private Button deleteDetailBtn;
    @FXML
    private TextField searchField;
    @FXML
    private HBox tableAndFilterContainer;
    
    //Columnas Master
    private TableColumn<SaleViewModel, Boolean> selectSaleColumn;
    private TableColumn<SaleViewModel, String> colBillNumber;
    private TableColumn<SaleViewModel, LocalDate> colDate;
    private TableColumn<SaleViewModel, String> colClient;
    private TableColumn<SaleViewModel, Number> colTotal;
    private TableColumn<SaleViewModel, PaymentMethod> colPaymentMethod;
    
    //Columnas Detail
    private TableColumn<DetailSaleViewModel, Boolean> selectDetailColumn;
    private TableColumn<DetailSaleViewModel, String> colProduct;
    private TableColumn<DetailSaleViewModel, Number> colAmmount;
    private TableColumn<DetailSaleViewModel, Number> colUnitPrice;
    private TableColumn<DetailSaleViewModel, Number> colSubtotal;
    
    private ObservableList<SaleViewModel> saleViewModels;
    private ObservableList<DetailSaleViewModel> detailViewModels;
    
    private List<Client> availableClients = new ArrayList<>();
    private List<Product> avaibleProducts = new ArrayList<>();
    
    //cambios pendientes
    private Map<UUID, Map<String, Object>> headersToUpdate;
    private Map<UUID, Map<String, Object>> detailsToUpdate;
    private List<UUID> headersToDelete;
    private List<UUID> detailsToDelete;
    
    //async
    private SaleLoadService saleLoadService;
    private SaleDetailLoadService detailLoadService;
    private SaleSaveService saleSaveService;
    private ClientLoadService clientLoadService;
    private BasicProductLoadService productLoadService;
    
    //Estado
    private SaleViewModel currentSale;
    
    //Filtro
    private FilterState<SaleViewModel> filterState;
    private FilterableTableHelper<SaleViewModel> filterHelper;
    private FilterPanelController<SaleViewModel> filterPanel;
    private boolean filterPanelVisible = false;
    
    @FXML
    public void initialize() {
        currentSale = null;
        
        saleViewModels = FXCollections.observableArrayList();
        detailViewModels = FXCollections.observableArrayList();
        
        headersToUpdate = new HashMap<>();
        detailsToUpdate = new HashMap<>();
        headersToDelete = new ArrayList<>();
        detailsToDelete = new ArrayList<>();
                
        saleLoadService = new SaleLoadService();
        detailLoadService = new SaleDetailLoadService();
        saleSaveService = new SaleSaveService();
        clientLoadService = new ClientLoadService();
        productLoadService = new BasicProductLoadService();
        
        setupMasterColumns();
        setupDetailColumns();
        
        salesTable.setItems(saleViewModels);
        detailTable.setItems(detailViewModels);
        salesTable.setEditable(true);
        detailTable.setEditable(true);
        
        salesTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        currentSale = newVal;
                        addDetailBtn.setDisable(false);
                        deleteDetailBtn.setDisable(false);
                        loadProducts(newVal.getId(), newVal.getBillNumber());
                    } else {
                        currentSale = null;
                        addDetailBtn.setDisable(true);
                        deleteDetailBtn.setDisable(true);
                        detailViewModels.clear();
                        detailsLabel.setText("Selecciona una venta para ver su detalle");
                    }
                });
        
        loadClients();
    }
    
    private void initFilterSystem() {
        List<FilterGroup<SaleViewModel>> groups = SaleFilterConfig.buildGroups();
        filterState = new FilterState<>();
 
        try {
            filterPanel = FilterPanelController.create(groups, filterState);
            filterPanel.getRoot().setVisible(false);
            filterPanel.getRoot().setManaged(false);
            tableAndFilterContainer.getChildren().add(filterPanel.getRoot());
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
            AlertUtils.showError("Error al cargar el panel de filtros.");
            return;
        }
 
        filterHelper = new FilterableTableHelper<>(
                saleViewModels,
                searchField,
                filterState,
                SaleFilterConfig::textMatch);
 
        salesTable.setItems(filterHelper.getFilteredList());
    }
    
    private void setupMasterColumns() {
        selectSaleColumn = new TableColumn<>("");
        selectSaleColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectSaleColumn.setCellFactory(col -> new CheckBoxTableCell<>());
        selectSaleColumn.setEditable(true);
        selectSaleColumn.setResizable(false);
        selectSaleColumn.setMaxWidth(50);
        selectSaleColumn.setMinWidth(50);
        
        colBillNumber = new TableColumn<>("Nº Factura");
        colBillNumber.setPrefWidth(150);
        colBillNumber.setEditable(true);
        colBillNumber.setUserData("numero_factura");
        colBillNumber.setCellValueFactory(data -> data.getValue().billNumberProperty());
        colBillNumber.setCellFactory(TextFieldTableCell.forTableColumn());
        colBillNumber.setOnEditCommit(e -> handleHeaderStringEdit(e, "numero_factura"));
 
        // Fecha – solo lectura por ahora
        colDate = new TableColumn<>("Fecha");
        colDate.setUserData("fecha");
        colDate.setPrefWidth(170);
        colDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDate().toLocalDate()));
        colDate.setCellFactory(DatePickerTableCell.forTableColumn());
        colDate.setOnEditCommit(this::handleDateEdit);
        colDate.setEditable(true);
 
        // Cliente – solo lectura por ahora
        colClient = new TableColumn<>("Cliente");
        colDate.setUserData("id_cliente");
        colClient.setPrefWidth(220);
        colClient.setEditable(false);
        colClient.setCellValueFactory(data -> data.getValue().clientNameProperty());
        colClient.setCellFactory(col -> new TableCell<SaleViewModel, String>() {
 
            private final Label label = new Label();
            private final HBox  container = new HBox(label);
 
            {
                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(2, 5, 2, 5));
 
                container.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !isEmpty()) {
                        SaleViewModel vm = getTableRow().getItem();
                        if (vm != null) {
                            openClientSelectDialog(vm);
                        }
                    }
                });
            }
            
             @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    label.setText(item);
                    label.setStyle("-fx-text-fill: #1E88E5; -fx-underline: true;");
                    javafx.scene.control.Tooltip tip =
                            new javafx.scene.control.Tooltip("Doble clic para cambiar el cliente");
                    setTooltip(tip);
                    setGraphic(container);
                }
            }
        });
 
        // Total – solo lectura (se recalcula en el detalle)
        colTotal = new TableColumn<>("Total");
        colTotal.setUserData("total");
        colTotal.setPrefWidth(110);
        colTotal.setCellValueFactory(data -> data.getValue().totalProperty());
 
        // Método de pago – editable con ComboBox
        colPaymentMethod = new TableColumn<>("Método de pago");
        colPaymentMethod.setUserData("metodo_pago");
        colPaymentMethod.setPrefWidth(160);
        colPaymentMethod.setCellValueFactory(data -> data.getValue().paymentMethodProperty());
        colPaymentMethod.setCellFactory(ComboBoxTableCell.forTableColumn(PaymentMethod.values()));
        colPaymentMethod.setOnEditCommit(this::handlePaymentMethodEdit);
 
        salesTable.getColumns().addAll(
                selectSaleColumn, colBillNumber, colDate, colClient, colTotal, colPaymentMethod);
    }
    
    private void setupDetailColumns() {
        selectDetailColumn = new TableColumn<>("");
        selectDetailColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectDetailColumn.setCellFactory(col -> new CheckBoxTableCell<>());
        selectDetailColumn.setEditable(true);
        selectDetailColumn.setResizable(false);
        selectDetailColumn.setMaxWidth(50);
        selectDetailColumn.setMinWidth(50);
        
        colProduct = new TableColumn<>("Producto");
        colProduct.setUserData("id_producto");
        colProduct.setPrefWidth(240);
        colProduct.setEditable(false);
        colProduct.setCellValueFactory(data -> data.getValue().productNameProperty());
        colProduct.setCellFactory(col -> new TableCell<DetailSaleViewModel, String>() {
            private final Label label = new Label();
            private final HBox container = new HBox(label);
            
            {
                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(2,5,2,5));
                container.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && !isEmpty()) {
                        DetailSaleViewModel vm = getTableRow().getItem();
                        if (vm != null) {
                            openProductSelectDialog(vm);
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    label.setText(item);
                    label.setStyle("-fx-text-fill: #1E88E5; -fx-underline: true;");
                    javafx.scene.control.Tooltip tip =
                            new javafx.scene.control.Tooltip("Doble clic para cambiar el producto");
                    setTooltip(tip);
                    setGraphic(container);
                }
            }
        });
 
        colAmmount = new TableColumn<>("Cantidad");
        colAmmount.setUserData("cantidad");
        colAmmount.setPrefWidth(100);
        colAmmount.setCellValueFactory(data -> data.getValue().ammountProperty());
        colAmmount.setCellFactory(EditableCellFactory.forNumber(
                v -> v > 0, 
                "La cantidad debe ser mayor a 0.",
                (vm, value) -> {
                    vm.ammountProperty().set(value.intValue()); 
                    vm.recalculateSubtotal(); 
                    recalculateTotal(vm.getDetail().getSaleId());
                    registerDetailUpdate(vm, "subtotal", vm.getSubtotal());},
                false)
        );
        colAmmount.setOnEditCommit(this::handleDetailNumberEdit);

        colUnitPrice = new TableColumn<>("Precio unitario");
        colUnitPrice.setUserData("precio_unitario");
        colUnitPrice.setPrefWidth(150);
        colUnitPrice.setCellValueFactory(data -> data.getValue().unitPriceProperty());
        colUnitPrice.setCellFactory(EditableCellFactory.forNumber(
                v -> v > 0,
                "El precio unitario debe ser mayor a 0.",
                (vm, value) -> {
                    vm.unitPriceProperty().set(value.floatValue());
                    vm.recalculateSubtotal();
                    recalculateTotal(vm.getDetail().getSaleId());
                    registerDetailUpdate(vm, "subtotal", vm.getSubtotal());},
                true)
        );
        colUnitPrice.setOnEditCommit(this::handleDetailNumberEdit);
 
        // Subtotal – calculado, solo lectura
        colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setUserData("subtotal");
        colSubtotal.setPrefWidth(130);
        colSubtotal.setCellValueFactory(data -> data.getValue().subtotalProperty());
 
        detailTable.getColumns().addAll(
                selectDetailColumn,colProduct, colAmmount, colUnitPrice, colSubtotal);
    }
    
    private void openClientSelectDialog(SaleViewModel vm) {
        if (availableClients.isEmpty()) {
            AlertUtils.showError("No hay clientes disponibles para seleccionar.");
            return;
        }
 
        UUID currentClientId = vm.getHeader().getClientId();
 
        Client chosen = RadioDialogController.showDialog(
                salesTable.getScene().getWindow(),
                "Seleccionar Cliente",
                availableClients,
                Client::getCompanyName,
                Client::getId,
                currentClientId);
 
        if (chosen == null) return;
        if (chosen.getId().equals(currentClientId)) return;
 
        vm.clientNameProperty().set(chosen.getCompanyName());
        vm.getHeader().setClientId(chosen.getId());
        registerHeaderUpdate(vm, "id_cliente", chosen.getId());
    }
    
    private void openProductSelectDialog(DetailSaleViewModel vm) {
        if (avaibleProducts.isEmpty()) {
            AlertUtils.showError("No hay productos disponibles para seleccionar.");
            return;
        }
        
        UUID currentProductId = vm.getDetail().getProductId();
        
        Product chosen = RadioDialogController.showDialog(
                detailTable.getScene().getWindow(),
                "Seleccionar Producto", 
                avaibleProducts, 
                Product::getName,
                Product::getId,
                currentProductId);
        
        if (chosen == null) return;
        if (chosen.getId().equals(currentProductId)) return;
        
        vm.productNameProperty().set(chosen.getName());
        vm.getDetail().setProductId(chosen.getId());
        registerDetailUpdate(vm, "id_producto", chosen.getId());
    }
    
    @FXML
    protected void handleFilterToggle() {
        if (filterPanel == null) return;
        filterPanelVisible = !filterPanelVisible;
        filterPanel.getRoot().setVisible(filterPanelVisible);
        filterPanel.getRoot().setManaged(filterPanelVisible);
    }
    
    @FXML
    public void handleAddSale() {
        if (availableClients.isEmpty()) {
            AlertUtils.showError("No hay clientes cargados. Recargue la vista.");
            return;
        }
        
        SaleHeaderFormData data = SaleHeaderFormController.showForm(salesTable.getScene().getWindow(), availableClients);
        
        if (data == null) return;
        
        SaleHeader header = new SaleHeader();
        header.setId(UUID.randomUUID());
        header.setBillNumber(data.billNumber);
        header.setDate(Date.valueOf(data.date));
        header.setClientId(data.client.getId());
        header.setPaymentMethod(data.paymentMethod);
        header.setTotal(0f);
        
        try {
            header.setLoadedBy(SessionContext.getCurrentUserId());
        } catch (IllegalStateException ex) {
            //Sin sesión activa no se asigna usuario
        }
         
        SaleViewModel vm = new SaleViewModel(new HeaderSaleWithClient(header, data.client.getCompanyName()));
        vm.setIsNew(true);
        
        saleViewModels.add(vm);
        
        salesTable.getSelectionModel().select(vm);
        salesTable.scrollTo(vm);
        //persistHeader(header, data.client.getCompanyName());
    }
    
    @FXML
    public void handleAddDetail() {
        if (currentSale == null) {
            AlertUtils.showError("Seleccione una venta antes de agregar ítems.");
            return;
        }
        
        if (avaibleProducts.isEmpty()) {
            AlertUtils.showError("No hay productos disponibles. Recargue la vista.");
            return;
        }
        
        SaleDetailFormData data = SaleDetailFormController.showForm(
                detailTable.getScene().getWindow(),
                avaibleProducts,
                currentSale.getBillNumber()
        );
        
        if (data == null) return;
        
        SaleDetail detail = new SaleDetail();
        detail.setId(UUID.randomUUID());
        detail.setSaleId(currentSale.getId());
        detail.setProductId(data.product.getId());
        detail.setUnitPrice(data.unitPrice);
        detail.setAmmount(data.ammount);
        detail.setSubtotal(data.subtotal);
        
        DetailSaleViewModel vm = new DetailSaleViewModel(new DetailSaleWithProduct(detail, data.product.getName()));
        vm.setIsNew(true);
        
        detailViewModels.add(vm);
        recalculateTotal(detail.getSaleId());
        
        //persistDetail(detail, data.product.getName());
    }
    
    private void handleDateEdit(TableColumn.CellEditEvent<SaleViewModel, LocalDate> e) {
        LocalDate newValue = e.getNewValue();
        
        if (newValue == null) {
            salesTable.refresh();
            return;
        }
        
        if (newValue.isAfter(LocalDate.now())) {
            AlertUtils.showError("La fecha de la venta no puede ser posterior a la fecha actual");
            salesTable.refresh();
            return;
        }
        
        SaleViewModel vm = e.getRowValue();
        
        //Comparar contra el valor actual del viewModel por que getOldValue ya puede reflejar el nuevo valor
        LocalDate current = vm.getDate() != null ? vm.getDate().toLocalDate() : null;
        if (newValue.equals(current)) {
            return;
        }
        
        vm.dateProperty().set(Date.valueOf(newValue));
        registerHeaderUpdate(vm, "fecha", Date.valueOf(newValue));
    }
    
    private void handleHeaderStringEdit(TableColumn.CellEditEvent<SaleViewModel, String> e, String dbField) {
        String newValue = normalize(e.getNewValue());
        String oldValue = normalize(e.getOldValue());
        
        if (newValue.equals(oldValue)) return;
        
        if (newValue.isEmpty()) {
            AlertUtils.showError("El campo no puede estar vacío.");
            salesTable.refresh();
            return;
        }
        if (dbField.equals("numero_factura") && newValue.length() > 20) {
            AlertUtils.showError("El número de factura no puede superar los 20 carácteres.");
            salesTable.refresh();
            return;
        }
        
        SaleViewModel vm = e.getRowValue();
        vm.billNumberProperty().set(newValue);
        registerHeaderUpdate(vm, dbField, newValue);
    }
    
    private void handlePaymentMethodEdit(TableColumn.CellEditEvent<SaleViewModel, PaymentMethod> e) {
        PaymentMethod newValue = e.getNewValue();
        PaymentMethod oldValue = e.getOldValue();
        
        if (newValue == null || newValue == oldValue) return;
        
        SaleViewModel vm = e.getRowValue();
        vm.paymentMethodProperty().set(newValue);
        
        registerHeaderUpdate(vm, "metodo_pago", newValue.name());
    }
    
    private void handleDetailNumberEdit(TableColumn.CellEditEvent<DetailSaleViewModel, Number> e) {
        Number newValue = e.getNewValue();
        Number oldValue = e.getOldValue();
        
        if (newValue == null || newValue == oldValue) return;
        
        DetailSaleViewModel vm = e.getRowValue();
        String columnName = e.getTableColumn().getUserData().toString();
        
        registerDetailUpdate(vm, columnName, newValue);
    }
    
    private void registerHeaderUpdate(SaleViewModel vm, String field, Object value) {
        if (!vm.getIsNew()) {
            headersToUpdate.computeIfAbsent(vm.getId(), k -> new HashMap<>()).put(field, value);   
        }
    }
    
    private void registerDetailUpdate(DetailSaleViewModel vm, String field, Object value) {
        if (!vm.getIsNew()) {
            detailsToUpdate.computeIfAbsent(vm.getId(), k -> new HashMap<>()).put(field, value);    
        }
    }
    
    private void recalculateTotal(UUID saleId) {
        if (saleId == null) return;

        float total = detailViewModels.stream()
            .filter(e -> saleId.equals(e.getDetail().getSaleId()))
            .map(DetailSaleViewModel::getSubtotal)
            .reduce(0.00f, Float::sum);

        System.out.println("Total calculado: " + total + " tamaño de lista " + detailViewModels.stream()
            .filter(e -> saleId.equals(e.getDetail().getSaleId())).collect(Collectors.toList()).size());
    
        currentSale.totalProperty().set(total);
        
        registerHeaderUpdate(saleViewModels.filtered(s -> s.getId().equals(saleId)).get(0), "total", total);
    }
    
    private void loadClients() {
        if (clientLoadService.isRunning()) return;
 
        clientLoadService.reset();
 
        clientLoadService.setOnSucceeded(e -> {
            Set<Client> clientSet = clientLoadService.getValue();
            availableClients = new ArrayList<>(clientSet);
            // Sort alphabetically for convenience
            availableClients.sort(
                java.util.Comparator.comparing(Client::getCompanyName));
            loadHeaders();
        });
 
        clientLoadService.setOnFailed(e -> {
            AlertUtils.showError(
                "Error al cargar clientes: " + clientLoadService.getException().getMessage());
            // Load headers anyway – client column will just be read-only effectively
            loadHeaders();
        });
 
        clientLoadService.start();
    }
    
    private void loadProducts(UUID saleId, String billNumber) {
        if (productLoadService.isRunning()) return;
        
        productLoadService.reset();
        
        productLoadService.setOnSucceeded(e -> {
            List<Product> productList = productLoadService.getValue();
            avaibleProducts = new ArrayList<>(productList);
            
            avaibleProducts.sort(Comparator.comparing(Product::getName));
            loadDetail(saleId, billNumber);
        });
        
        productLoadService.setOnFailed(e -> {
            AlertUtils.showError("Error al cargar productos: " + productLoadService.getException().getMessage());
            loadDetail(saleId, billNumber);
        });
        
        productLoadService.start();
    }
    
    private void loadHeaders() {
        if (saleLoadService.isRunning()) return;
 
        saleLoadService.reset();
 
        saleLoadService.setOnSucceeded(e -> {
            List<HeaderSaleWithClient> cabeceras = saleLoadService.getValue();
            List<SaleViewModel> vms = cabeceras.stream()
                    .map(SaleViewModel::new)
                    .collect(Collectors.toList());
            
            if (filterHelper != null) {
                filterHelper.setAll(vms);
            } else {
                saleViewModels.setAll(vms);
                initFilterSystem();
            }
        });
 
        saleLoadService.setOnFailed(e ->
            AlertUtils.showError(saleLoadService.getException().getMessage()));
 
        saleLoadService.start();
    }
    
    private void loadDetail(UUID saleId, String billNumber) {
        detailsLabel.setText("Cargando detalle…");
        detailViewModels.clear();
 
        detailLoadService.setSaleId(saleId);
        detailLoadService.reset();
 
        detailLoadService.setOnSucceeded(e -> {
            List<DetailSaleWithProduct> items = detailLoadService.getValue();
            List<DetailSaleViewModel> vms = items.stream()
                    .map(DetailSaleViewModel::new)
                    .collect(Collectors.toList());
            detailViewModels.setAll(vms);
            detailsLabel.setText("Detalle de venta  " + billNumber
                    + "  (" + vms.size() + " ítem" + (vms.size() != 1 ? "s" : "") + ")");
        });
 
        detailLoadService.setOnFailed(e -> {
            detailsLabel.setText("Error al cargar el detalle");
            AlertUtils.showError(detailLoadService.getException().getMessage());
        });
 
        detailLoadService.start();
    }
    
    @FXML
    public void handleReload() {
        long newSalesCount = saleViewModels.stream().filter(SaleViewModel::getIsNew).count();
        
        if (newSalesCount > 0 || !headersToUpdate.isEmpty() || !detailsToUpdate.isEmpty()) {
            Optional<ButtonType> confirm = AlertUtils.showConfirmAlert(
                    "Hay cambios sin guardar",
                    "Si recargás, perderás los cambios no guardados. ¿Continuar?");
            if (confirm.isEmpty() || confirm.get() != ButtonType.OK) return;
        }
 
        salesTable.getSelectionModel().clearSelection();
        detailViewModels.clear();
        detailsLabel.setText("Seleccioná una venta para ver su detalle");
        saleViewModels.clear();
        headersToUpdate.clear();
        detailsToUpdate.clear();
 
        loadHeaders();
    }
    
    @FXML
    public void handleSaveChanges() {
        //Obtener nuevas ventas e items
        List<SaleHeader> newHeaders = saleViewModels.stream()
                .filter(SaleViewModel::getIsNew)
                .map(SaleViewModel::getHeader)
                .collect(Collectors.toList());
        
        List<SaleDetail> newDetails = detailViewModels.stream()
                .filter(DetailSaleViewModel::getIsNew)
                .map(DetailSaleViewModel::getDetail)
                .collect(Collectors.toList());
        
        
        if (headersToUpdate.isEmpty() && detailsToUpdate.isEmpty() &&
                headersToDelete.isEmpty() && detailsToDelete.isEmpty() &&
                newHeaders.isEmpty() && newDetails.isEmpty()) {
            AlertUtils.showError("No hay cambios pendientes para guardar.");
            return;
        }
 
        Optional<ButtonType> confirm = AlertUtils.showConfirmAlert(
                "Guardar cambios",
                "¿Desea guardar los cambios en la base de datos?");
 
        confirm.ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                saveData(newHeaders, newDetails);
            }
        });
    }
    
    @FXML
    public void handleDeleteSale() {
        Optional<ButtonType> btn = AlertUtils.showConfirmAlert("Eliminar venta", 
                "Desea eliminar las ventas seleccionadas?\nLas ventas sin guardar no se podrán recuperar.");
        
        btn.ifPresent(p -> {
            if (p == ButtonType.OK) {
                List<SaleViewModel> selected = saleViewModels.stream().filter(s -> s.isSelected()).collect(Collectors.toList());
                
                for (SaleViewModel sale : selected) {
                    if (!sale.getIsNew()) {
                        headersToDelete.add(sale.getId());
                    }
                    saleViewModels.remove(sale);
                }
            }
        });
    }
    
    @FXML
    public void handleDeleteDetail() {
        Optional<ButtonType> btn = AlertUtils.showConfirmAlert("Eliminar ítem de detalle",
                "Desea eliminar los ítems seleccionados?\nLos items sin guardar no se podrán recuperar.");
        
        btn.ifPresent(p -> {
            if (p == ButtonType.OK) {
                List<DetailSaleViewModel> selected = detailViewModels.stream().filter(s -> s.isSelected()).collect(Collectors.toList());
                
                for (DetailSaleViewModel detail : selected) {
                    if (!detail.getIsNew()) {
                        detailsToDelete.add(detail.getId());    
                    }
                    detailViewModels.remove(detail);
                }
                
                recalculateTotal(currentSale.getId());
            }
        });
    }
    
    private void saveData(List<SaleHeader> newHeaders, List<SaleDetail> newDetails) {
        if (saleSaveService.isRunning()) return;
 
        saleSaveService.reset();
        
        saleSaveService.setData(newHeaders, newDetails, headersToUpdate, detailsToUpdate, headersToDelete, detailsToDelete);
 
        int totalHeaders = headersToUpdate.size();
        int totalDetails = detailsToUpdate.size();
        int totalNewHeaders = newHeaders.size();
        int totalNewDetails = newDetails.size();
        int totalRemovedHeaders = headersToDelete.size();
        int totalRemovedItems = detailsToDelete.size();
 
        saleSaveService.setOnSucceeded(e -> {
            headersToUpdate.clear();
            detailsToUpdate.clear();
            headersToDelete.clear();
            detailsToDelete.clear();
 
            AlertUtils.showSuccess(
                    "Operación exitosa",
                    "Cambios guardados con éxito",
                    "Ventas (cabecera) creadas: " + totalNewHeaders 
                    + "\nVentas (cabecera) actualizadas: " + totalHeaders
                    + "\nVentas (cabecera) eliminadas: " + totalRemovedHeaders
                    +"\n"
                    + "\nÍtems de detalle creados: " + totalNewDetails
                    + "\nÍtems de detalle actualizados: " + totalDetails
                    + "\nÍtems de detalle eliminados: " + totalRemovedItems);
            
            loadHeaders();
        });
 
        saleSaveService.setOnFailed(e ->
                AlertUtils.showError(saleSaveService.getException().getMessage()));
 
        saleSaveService.start();
    }
    
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
