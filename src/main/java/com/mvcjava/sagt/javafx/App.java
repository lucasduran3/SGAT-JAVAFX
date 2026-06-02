package com.mvcjava.sagt.javafx;

import com.mvcjava.sagt.javafx.auth.SessionContext;
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
    
    private static final double AUTH_WIDTH = 860;
    private static final double AUTH_HEIGHT = 600;
    private static final double MAIN_WIDTH = 1280;
    private static final double MAIN_HEIGTH = 760;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("SGAT");
        primaryStage.setResizable(false);
        
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
        primaryStage.centerOnScreen();
    }
    
    public static void showMainView() throws IOException {
        if (!SessionContext.isLoggedIn()) {
            throw new IllegalStateException("No hay usuario autenticado. Inicie sesión antes de cargar la vista principal.");
        }
        
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/mvcjava/sagt/javafx/view/mainView.fxml"));
        
        Scene scene = new Scene(loader.load(), MAIN_WIDTH, MAIN_HEIGTH);
        
        primaryStage.setScene(scene);
        primaryStage.setWidth(MAIN_WIDTH);
        primaryStage.setHeight(MAIN_HEIGTH);
        primaryStage.centerOnScreen();
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