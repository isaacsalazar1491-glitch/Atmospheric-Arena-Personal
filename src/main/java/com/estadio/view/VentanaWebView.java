package com.estadio.view;

import com.estadio.repository.ReporteRepository;
import com.estadio.service.*;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

/**
 * Clase principal de la interfaz WebView.
 */
public class VentanaWebView {

    private WebEngine engine;
    private final JavaBridge bridge;

    public VentanaWebView(Stage stage) {

        // 1. Inicialización de servicios
        GestorAsientos gestorAsientos = new GestorAsientos();
        GestorBoletos  gestorBoletos  = new GestorBoletos(gestorAsientos);
        GestorReportes gestorReportes = new GestorReportes(new ReporteRepository());
        GestorEventos  gestorEventos  = new GestorEventos();

        // 2. Configuración del WebView
        WebView webView = new WebView();
        webView.setPrefSize(1280, 820);
        this.engine = webView.getEngine();

        // ── DEBUG: captura alertas de JavaScript ──────────────
        this.engine.setOnAlert(event ->
            System.out.println(">>> JS Alert: " + event.getData()));

        // ── DEBUG: captura excepciones del worker ─────────────
        this.engine.getLoadWorker().exceptionProperty().addListener((obs, old, ex) -> {
            if (ex != null)
                System.err.println(">>> WebView Exception: " + ex.getMessage());
        });

        // 3. Creación del Puente
        this.bridge = new JavaBridge(this, engine, gestorBoletos, gestorAsientos,
                                     gestorReportes, gestorEventos);

        // 4. Ciclo de vida de la página
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {

                // Inyectar el objeto Java en window
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaApp", bridge);

                // ── DEBUG: captura errores de JavaScript ──────
                engine.executeScript(
                    "window.onerror = function(msg, src, line, col, err) {" +
                    "  alert('JS ERROR | ' + msg + ' | linea ' + line);" +
                    "  return false;" +
                    "};"
                );

                // Llamar a inicializar() si la página lo define
                engine.executeScript(
                    "if (typeof inicializar === 'function') { inicializar(); }"
                );

                System.out.println(">>> [WebView] Puente javaApp vinculado con éxito.");
            }

            if (newState == Worker.State.FAILED) {
                System.err.println(">>> [WebView] Falló la carga de la página.");
            }
        });

        // 5. Carga inicial: Home
        try {
            String url = getClass().getResource("/html/home.html").toExternalForm();
            engine.load(url);
        } catch (Exception e) {
            System.err.println("Error al cargar home.html: " + e.getMessage());
        }

        // 6. Configuración de la escena
        Scene scene = new Scene(webView);
        stage.setTitle("Atmospheric Arena — Gestión de Estadio");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Navega entre vistas según el ROL obtenido de MySQL.
     */
    public void cambiarPantalla(String rol) {
        javafx.application.Platform.runLater(() -> {
            String path = "ADMIN".equalsIgnoreCase(rol)
                    ? "/html/admin_dashboard.html"
                    : "/html/home.html";
            try {
                engine.load(getClass().getResource(path).toExternalForm());
                System.out.println(">>> [Navegación] Cambiando a vista: " + rol);
            } catch (Exception e) {
                System.err.println("Error al cambiar pantalla: " + e.getMessage());
            }
        });
    }
}