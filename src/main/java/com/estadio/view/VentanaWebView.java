package com.estadio.view;

import com.estadio.repository.ReporteRepository;
import com.estadio.service.GestorAsientos;
import com.estadio.service.GestorBoletos;
import com.estadio.service.GestorEventos;
import com.estadio.service.GestorReportes;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class VentanaWebView {

    public VentanaWebView(Stage stage) {
        GestorAsientos gestorAsientos = new GestorAsientos();
        GestorBoletos  gestorBoletos  = new GestorBoletos(gestorAsientos);
        GestorReportes gestorReportes = new GestorReportes(new ReporteRepository());
        GestorEventos  gestorEventos  = new GestorEventos();

        WebView webView = new WebView();
        webView.setPrefSize(1280, 820);
        WebEngine engine = webView.getEngine();
        JavaBridge bridge = new JavaBridge(engine, gestorBoletos, gestorAsientos,
                                           gestorReportes, gestorEventos);

        engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaApp", bridge);
                engine.executeScript("if(typeof inicializar==='function') inicializar();");
            }
        });

        engine.load(getClass().getResource("/html/home.html").toExternalForm());

        Scene scene = new Scene(webView);
        stage.setTitle("Atmospheric Arena — Venta de Boletos");
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(680);
        stage.show();
    }
}
