package com.mvcjava.sagt.javafx;

import com.mvcjava.sagt.javafx.auth.SessionContext;
import com.mvcjava.sagt.javafx.config.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {
    private static Stage primaryStage;
    
    private static final double AUTH_WIDTH = 1280;
    private static final double AUTH_HEIGHT = 760;
    
    private static final double MAIN_WIDTH = 1280;
    private static final double MAIN_HEIGTH = 760;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("SGAT");
        primaryStage.setResizable(true);
        
        Thread warmupThread = new Thread(DatabaseManager::warmUp, "db-warmup");
        warmupThread.setDaemon(true);
        warmupThread.start();
        
        showAuthView();
        primaryStage.show();
    }
    
    public static void showAuthView() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/mvcjava/sagt/javafx/view/authView.fxml")
        );
        
        Scene scene  = new Scene(loader.load(), AUTH_WIDTH, AUTH_HEIGHT);
        
        primaryStage.setScene(scene);
        primaryStage.setWidth(AUTH_WIDTH);
        primaryStage.setHeight(AUTH_HEIGHT);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
    }
    
    public static void showMainView() throws IOException {
        if (!SessionContext.isLoggedIn()) {
            throw new IllegalStateException("No hay usuario autenticado. Inicie sesión antes de cargar la vista principal.");
        }
        
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/mvcjava/sagt/javafx/view/mainView.fxml"));
        
        Scene scene = new Scene(loader.load());
        
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MAIN_WIDTH);
        primaryStage.setMinHeight(MAIN_HEIGTH);
        primaryStage.setMaximized(true);
        primaryStage.centerOnScreen();
        primaryStage.setResizable(true);
    }
    
    public static void logout() throws IOException {
        SessionContext.clear();
        primaryStage.setResizable(false);
        showAuthView();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}