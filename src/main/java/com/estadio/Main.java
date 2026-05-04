package com.estadio;

import com.estadio.view.VentanaWebView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada. Lanza la app JavaFX con WebView.
 */
public class Main extends Application 
{

    @Override
    public void start(Stage primaryStage) 
    {
        new VentanaWebView(primaryStage);
    }

    public static void main(String[] args) 
    {
        launch(args);
    }
}
