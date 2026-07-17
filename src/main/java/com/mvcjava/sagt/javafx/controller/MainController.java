/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mvcjava.sagt.javafx.controller;

import com.mvcjava.sagt.javafx.App;
import com.mvcjava.sagt.javafx.auth.SessionContext;
import com.mvcjava.sagt.javafx.util.AlertUtils;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;

/**
 *
 * @author lucas
 */
public class MainController {
    private Map<String, Route> routes;
    
    @FXML
    private ToggleGroup menuGroup;
    @FXML
    private StackPane dynamicContentContainer;
    @FXML 
    private Label pageTitle;
    @FXML
    private FontAwesomeIconView titleIcon;
    
    @FXML private MenuButton userMenuLabel;
    
    @FXML
    public void initialize() {
        initializeRoutes();
        setupSideMenuListener();
        setupUserMenu();
    
        if (menuGroup != null && !menuGroup.getToggles().isEmpty()) {
            menuGroup.selectToggle(menuGroup.getToggles().get(0));
        }
    }
    
    private void initializeRoutes() {
        routes = new HashMap<>();
        routes.put("dashboard", new Route("/com/mvcjava/sagt/javafx/view/dashboardView.fxml", "Dashboard", "BAR_CHART"));
        routes.put("productos", new Route("/com/mvcjava/sagt/javafx/view/productsView.fxml", "Productos", "ARCHIVE"));
        routes.put("clientes", new Route("/com/mvcjava/sagt/javafx/view/clientsView.fxml", "Clientes", "USER"));
        routes.put("ventas", new Route("/com/mvcjava/sagt/javafx/view/salesView.fxml", "Ventas", "DOLLAR"));
        routes.put("proveedores", new Route("/com/mvcjava/sagt/javafx/view/suppliersView.fxml", "Proveedores", "TRUCK"));
        routes.put("categorias", new Route("/com/mvcjava/sagt/javafx/view/categoriesView.fxml", "Categorías", "LIST"));
    }
    
    private void setupSideMenuListener() {
        menuGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                // Forzar selección anterior al deseleccionar (Evita que el menú quede vacío)
                if (oldToggle != null) {
                    // Usamos Platform.runLater para evitar colisiones en el bucle de eventos de JavaFX
                    javafx.application.Platform.runLater(() -> menuGroup.selectToggle(oldToggle));
                }
                return;
            }

            var button = (ToggleButton) newToggle;
            String routeKey = button.getText().trim().toLowerCase();
        
            // Validacion para evitar NullPointerException
            if (!routes.containsKey(routeKey)) {
                System.err.println("Error: No existe una ruta registrada para la clave: " + routeKey);
                return;
            }

            Route currentRoute = routes.get(routeKey);
            String routePath = currentRoute.path;
        
            try {
                loadView(routePath);
                pageTitle.setText(currentRoute.title);
                titleIcon.setGlyphName(currentRoute.icon);
            } catch (IOException ex) {
                ex.printStackTrace();
                AlertUtils.showError("Error al cargar vista " + routeKey);
                if (oldToggle != null) {
                    javafx.application.Platform.runLater(() -> menuGroup.selectToggle(oldToggle));
                }
            }
        });
    }
    
    private void setupUserMenu() {
        if (SessionContext.isLoggedIn()) {
            userMenuLabel.setText(SessionContext.getCurrentUserName());
        }
    }
    
    @FXML
    public void handleLogout() {
        Optional<ButtonType> confirm = AlertUtils.showConfirmAlert("Cerrar sesión", "¿Está seguro que desea cerrar la sesión?");
        
        confirm.ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    App.logout();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    AlertUtils.showError("Error al cerrar sesión.");
                }
            }
        });
    }
    
    private void loadView(String route) throws IOException{
        if (route != null && !route.trim().isEmpty()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(route));
            Node view = loader.load();
            dynamicContentContainer.getChildren().setAll(view);
        }
    }
    
    private class Route {
        String path;
        String title;
        String icon;
        
        public Route(String path, String title, String icon) {
            this.path = path;
            this.title = title;
            this.icon = icon;
        }
    }
}
