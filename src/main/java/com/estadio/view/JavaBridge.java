package com.estadio.view;

import com.estadio.model.Boleto;
import com.estadio.model.Categoria;
import com.estadio.model.Evento;
import com.estadio.service.GestorAsientos;
import com.estadio.service.GestorBoletos;
import com.estadio.service.GestorEventos;
import com.estadio.service.GestorReportes;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Puente JS ↔ Java.
 * El mapa de asientos ahora muestra zonas mixtas en una sola vista.
 * La selección puede combinar VIP, PREFERENCIAL y GENERAL en una compra.
 */
public class JavaBridge {

    // Zona de cada fila: filas 0-1 = VIP, 2-4 = PREFERENCIAL, 5-9 = GENERAL
    private static final Categoria[] ZONA_POR_FILA = {
        Categoria.VIP,          // fila 0 (A)
        Categoria.VIP,          // fila 1 (B)
        Categoria.PREFERENCIAL, // fila 2 (C)
        Categoria.PREFERENCIAL, // fila 3 (D)
        Categoria.PREFERENCIAL, // fila 4 (E)
        Categoria.GENERAL,      // fila 5 (F)
        Categoria.GENERAL,      // fila 6 (G)
        Categoria.GENERAL,      // fila 7 (H)
        Categoria.GENERAL,      // fila 8 (I)
        Categoria.GENERAL,      // fila 9 (J)
    };

    private final WebEngine      engine;
    private final GestorBoletos  gestorBoletos;
    private final GestorAsientos gestorAsientos;
    private final GestorReportes gestorReportes;
    private final GestorEventos  gestorEventos;

    private Evento eventoActual = null;

    public JavaBridge(WebEngine engine, GestorBoletos gestorBoletos,
                      GestorAsientos gestorAsientos, GestorReportes gestorReportes,
                      GestorEventos gestorEventos) {
        this.engine         = engine;
        this.gestorBoletos  = gestorBoletos;
        this.gestorAsientos = gestorAsientos;
        this.gestorReportes = gestorReportes;
        this.gestorEventos  = gestorEventos;
    }

    // ── home.html ────────────────────────────────────────────

    public String getEventos() {
        return gestorEventos.getTodosComoJson();
    }

    public void navegarASeleccion(String eventoId) {
        try { eventoActual = gestorEventos.getEvento(eventoId); }
        catch (Exception e) { eventoActual = gestorEventos.getTodos().get(0); }
        Platform.runLater(() ->
            engine.load(getClass().getResource("/html/seleccion.html").toExternalForm()));
    }

    public void navegarAHome() {
        Platform.runLater(() ->
            engine.load(getClass().getResource("/html/home.html").toExternalForm()));
    }

    // ── seleccion.html ───────────────────────────────────────

    /** Info básica del evento para la cabecera */
    public String getInfoEvento() {
        if (eventoActual == null) return "{}";
        return String.format(
            "{\"id\":\"%s\",\"nombre\":\"%s\",\"fecha\":\"%s\"," +
            "\"lugar\":\"%s\",\"imagenUrl\":\"%s\"}",
            eventoActual.getId(), esc(eventoActual.getNombre()),
            esc(eventoActual.getFecha()), esc(eventoActual.getLugar()),
            esc(eventoActual.getImagenUrl()));
    }

    /**
     * Devuelve el estado de TODAS las matrices del evento en un solo JSON.
     * El JS determina la zona de cada fila por su índice.
     * Formato: { "VIP": [[...]], "PREFERENCIAL": [[...]], "GENERAL": [[...]] }
     */
    public String getAsientosEvento() {
        if (eventoActual == null) return "{}";
        StringBuilder sb = new StringBuilder("{");
        Categoria[] cats = Categoria.values();
        for (int ci = 0; ci < cats.length; ci++) {
            if (ci > 0) sb.append(",");
            sb.append("\"").append(cats[ci].name()).append("\":[");
            boolean[][] m = gestorAsientos.getMatrizAsientos(eventoActual.getId(), cats[ci]);
            for (int i = 0; i < GestorAsientos.FILAS; i++) {
                if (i > 0) sb.append(",");
                sb.append("[");
                for (int j = 0; j < GestorAsientos.COLUMNAS; j++) {
                    if (j > 0) sb.append(",");
                    sb.append(m[i][j] ? "true" : "false");
                }
                sb.append("]");
            }
            sb.append("]");
        }
        return sb.append("}").toString();
    }

    /**
     * Precios y disponibles de las 3 categorías para el resumen lateral.
     */
    public String getInfoCategorias() {
        if (eventoActual == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        Categoria[] cats = Categoria.values();
        for (int i = 0; i < cats.length; i++) {
            if (i > 0) sb.append(",");
            Categoria cat = cats[i];
            sb.append(String.format(
                "{\"id\":\"%s\",\"nombre\":\"%s\",\"precio\":%.2f,\"disponibles\":%d}",
                cat.name(), cat.toString(),
                gestorBoletos.getPrecio(cat),
                gestorAsientos.contarDisponibles(eventoActual.getId(), cat)));
        }
        return sb.append("]").toString();
    }

    /**
     * Procesa compra MIXTA. Cada asiento lleva su categoría:
     * formato: "fila,col,CATEGORIA;fila,col,CATEGORIA;..."
     * Agrupa por categoría y llama registrarVentaEnBloque para cada grupo.
     */
    public void confirmarCompraMixta(String asientosStr) {
        if (eventoActual == null) return;
        Platform.runLater(() -> {
            try {
                if (asientosStr == null || asientosStr.isBlank()) {
                    engine.executeScript("mostrarError('Selecciona al menos un asiento.')");
                    return;
                }

                // Agrupar asientos por categoría
                Map<Categoria, List<int[]>> grupos = new HashMap<>();
                for (Categoria cat : Categoria.values()) grupos.put(cat, new ArrayList<>());

                for (String parte : asientosStr.split(";")) {
                    String[] t = parte.split(",");
                    int f = Integer.parseInt(t[0].trim());
                    int c = Integer.parseInt(t[1].trim());
                    Categoria cat = Categoria.valueOf(t[2].trim());
                    grupos.get(cat).add(new int[]{f, c});
                }

                // Registrar ventas por categoría y acumular boletos
                List<Boleto> todosComprados = new ArrayList<>();
                for (Map.Entry<Categoria, List<int[]>> entry : grupos.entrySet()) {
                    if (entry.getValue().isEmpty()) continue;
                    List<Boleto> grupo = gestorBoletos.registrarVentaEnBloque(
                        eventoActual.getId(), entry.getKey(), entry.getValue());
                    todosComprados.addAll(grupo);
                    // Reporte por categoría
                    gestorReportes.generarYGuardarReporte(entry.getKey(), grupo, eventoActual.getId());
                }

                double total = todosComprados.stream().mapToDouble(Boleto::getPrecio).sum();
                engine.executeScript("mostrarExito(" + todosComprados.size() + "," + total + ")");

            } catch (IllegalStateException | IllegalArgumentException e) {
                engine.executeScript("mostrarError('" + esc(e.getMessage()) + "')");
            } catch (Exception e) {
                engine.executeScript("mostrarError('Error: " + e.getClass().getSimpleName() + "')");
            }
        });
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"").replace("'","\\'");
    }
}
