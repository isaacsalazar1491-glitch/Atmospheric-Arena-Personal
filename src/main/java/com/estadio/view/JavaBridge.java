package com.estadio.view;

import com.estadio.model.Boleto;
import com.estadio.model.Categoria;
import com.estadio.model.Evento;
import com.estadio.repository.ConexionDB;
import com.estadio.service.GestorAsientos;
import com.estadio.service.GestorBoletos;
import com.estadio.service.GestorEventos;
import com.estadio.service.GestorReportes;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaBridge {

    private final VentanaWebView ventana;
    private final WebEngine      engine;
    private final GestorBoletos  gestorBoletos;
    private final GestorAsientos gestorAsientos;
    private final GestorReportes gestorReportes;
    private final GestorEventos  gestorEventos;

    private Evento eventoActual = null;

    public JavaBridge(VentanaWebView ventana, WebEngine engine, GestorBoletos gestorBoletos,
                      GestorAsientos gestorAsientos, GestorReportes gestorReportes,
                      GestorEventos gestorEventos) {
        this.ventana        = ventana;
        this.engine         = engine;
        this.gestorBoletos  = gestorBoletos;
        this.gestorAsientos = gestorAsientos;
        this.gestorReportes = gestorReportes;
        this.gestorEventos  = gestorEventos;
    }

    // ── LOGIN / ADMIN ────────────────────────────────────────

    public void iniciarSesion(String usuario, String password) {
        String sql = "SELECT rol FROM Usuarios WHERE nombre_usuario = ? AND password = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String rol = rs.getString("rol");
                    Platform.runLater(() -> ventana.cambiarPantalla(rol));
                } else {
                    Platform.runLater(() -> engine.executeScript("mostrarError('Credenciales inválidas.')"));
                }
            }
        } catch (SQLException e) {
            Platform.runLater(() -> engine.executeScript("mostrarError('Error de BD: " + e.getMessage() + "')"));
        }
    }

    public String getReporteVentasDetallado() {
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT r.fecha_hora, r.categoria, r.total_generado, " +
                     "IFNULL(GROUP_CONCAT(CONCAT('Fila: ', a.fila, ' Col: ', a.columna) SEPARATOR ' | '), 'Sin detalle') as asientos " +
                     "FROM reportes_venta r " +
                     "LEFT JOIN boletos_vendidos bv ON r.id = bv.reporte_id " +
                     "LEFT JOIN Asientos a ON bv.boleto_id = a.id_asiento " +
                     "GROUP BY r.id ORDER BY r.fecha_hora DESC";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append(String.format(
                    "{\"fecha\":\"%s\",\"categoria\":\"%s\",\"ingresos\":%.2f,\"detalles\":\"%s\"}",
                    rs.getTimestamp("fecha_hora").toString(), rs.getString("categoria"),
                    rs.getDouble("total_generado"), esc(rs.getString("asientos"))));
                first = false;
            }
        } catch (SQLException e) {
            System.err.println("Error SQL Admin: " + e.getMessage());
        }
        return json.append("]").toString();
    }

    public void navegarALogin() {
        Platform.runLater(() ->
            engine.load(getClass().getResource("/html/login.html").toExternalForm()));
    }

    // ── HOME ─────────────────────────────────────────────────

    public String getEventos() {
        return gestorEventos.getTodosComoJson();
    }

    public void navegarASeleccion(String eventoId) {
        System.out.println(">>> navegarASeleccion id=" + eventoId);
        try {
            eventoActual = gestorEventos.getEvento(eventoId);
            System.out.println(">>> eventoActual cargado: " + eventoActual.getNombre());
        } catch (Exception e) {
            System.out.println(">>> ERROR cargando evento: " + e.getMessage());
            eventoActual = gestorEventos.getTodos().get(0);
        }
        Platform.runLater(() ->
            engine.load(getClass().getResource("/html/seleccion.html").toExternalForm()));
    }

    public void navegarAHome() {
        Platform.runLater(() ->
            engine.load(getClass().getResource("/html/home.html").toExternalForm()));
    }

    // ── SELECCIÓN ────────────────────────────────────────────

    /** Info básica del evento para la cabecera de seleccion.html */
    public String getInfoEvento() {
        System.out.println(">>> getInfoEvento() eventoActual=" +
            (eventoActual != null ? eventoActual.getNombre() : "null"));
        if (eventoActual == null) return "{}";
        return String.format(
            "{\"id\":\"%s\",\"nombre\":\"%s\",\"fecha\":\"%s\",\"lugar\":\"%s\",\"imagenUrl\":\"%s\"}",
            eventoActual.getId(),
            esc(eventoActual.getNombre()),
            esc(eventoActual.getFecha()),
            esc(eventoActual.getLugar()),
            esc(eventoActual.getImagenUrl()));
    }

    /**
     * Matrices de ocupación para las 3 zonas.
     * id_categoria en BD: 1=VIP, 2=PREFERENCIAL, 3=GENERAL
     */
    public String getAsientosEvento() {
        System.out.println(">>> getAsientosEvento() eventoActual=" +
            (eventoActual != null ? eventoActual.getId() : "null"));
        if (eventoActual == null) return "{}";

        boolean[][] matrizVip  = new boolean[10][10];
        boolean[][] matrizPref = new boolean[10][10];
        boolean[][] matrizGral = new boolean[10][10];

        String sql = "SELECT fila, columna, estado, id_categoria FROM Asientos WHERE id_evento = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventoActual.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int f     = rs.getInt("fila");
                    int c     = rs.getInt("columna");
                    int catId = rs.getInt("id_categoria");
                    boolean ocupado = !"Disponible".equalsIgnoreCase(rs.getString("estado"));
                    if (f < 10 && c < 10) {
                        if      (catId == 1) matrizVip[f][c]  = ocupado;
                        else if (catId == 2) matrizPref[f][c] = ocupado;
                        else if (catId == 3) matrizGral[f][c] = ocupado;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(">>> ERROR getAsientosEvento SQL: " + e.getMessage());
        }

        return "{" +
            "\"VIP\":"          + matrizAJson(matrizVip)  + "," +
            "\"PREFERENCIAL\":" + matrizAJson(matrizPref) + "," +
            "\"GENERAL\":"      + matrizAJson(matrizGral) +
        "}";
    }

    /**
     * Precios y disponibles de las 3 categorías.
     * Lee precios desde la tabla Categorias (id: 1=VIP, 2=PREFERENCIAL, 3=GENERAL)
     * y conteo de disponibles desde Asientos.
     */
    public String getInfoCategorias() {
        System.out.println(">>> getInfoCategorias() eventoActual=" +
            (eventoActual != null ? eventoActual.getId() : "null"));
        if (eventoActual == null) return "[]";

        StringBuilder sb = new StringBuilder("[");

        // Leer precios desde la BD directamente
        String sqlPrecios = "SELECT id_categoria, nombre, precio FROM Categorias ORDER BY id_categoria";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sqlPrecios);
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                int    idCat   = rs.getInt("id_categoria");
                String nombre  = rs.getString("nombre");
                double precio  = rs.getDouble("precio");

                // Mapear id numérico al nombre del enum que usa el JS
                String enumId;
                if      (idCat == 1) enumId = "VIP";
                else if (idCat == 2) enumId = "PREFERENCIAL";
                else                 enumId = "GENERAL";

                // Contar disponibles para este evento y categoría
                int disponibles = contarDisponiblesDB(eventoActual.getId(), idCat);

                if (!first) sb.append(",");
                sb.append(String.format(
                    "{\"id\":\"%s\",\"nombre\":\"%s\",\"precio\":%.2f,\"disponibles\":%d}",
                    enumId, esc(nombre), precio, disponibles));
                first = false;
            }
        } catch (SQLException e) {
            System.err.println(">>> ERROR getInfoCategorias SQL: " + e.getMessage());
            // Fallback con precios por defecto si falla la BD
            sb.setLength(0);
            sb.append("[");
            sb.append("{\"id\":\"VIP\",\"nombre\":\"VIP\",\"precio\":500.00,\"disponibles\":20},");
            sb.append("{\"id\":\"PREFERENCIAL\",\"nombre\":\"Preferencial\",\"precio\":250.00,\"disponibles\":30},");
            sb.append("{\"id\":\"GENERAL\",\"nombre\":\"General\",\"precio\":100.00,\"disponibles\":50}");
        }

        return sb.append("]").toString();
    }

    private int contarDisponiblesDB(String eventoId, int idCategoria) {
        String sql = "SELECT COUNT(*) FROM Asientos " +
                     "WHERE id_evento = ? AND id_categoria = ? AND estado = 'Disponible'";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventoId);
            ps.setInt(2, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(">>> ERROR contarDisponibles: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Procesa compra mixta. Formato recibido: "fila,col,CATEGORIA;fila,col,CATEGORIA;..."
     * Agrupa por categoría y registra cada grupo por separado.
     */
    public void confirmarCompraMixta(String asientosStr) {
        System.out.println(">>> confirmarCompraMixta: " + asientosStr);
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
                    String[] t = parte.trim().split(",");
                    int f = Integer.parseInt(t[0].trim());
                    int c = Integer.parseInt(t[1].trim());
                    Categoria cat = Categoria.valueOf(t[2].trim());
                    grupos.get(cat).add(new int[]{f, c});
                }

                // Registrar ventas y acumular boletos
                List<Boleto> todosComprados = new ArrayList<>();
                for (Map.Entry<Categoria, List<int[]>> entry : grupos.entrySet()) {
                    if (entry.getValue().isEmpty()) continue;
                    List<Boleto> grupo = gestorBoletos.registrarVentaEnBloque(
                        eventoActual.getId(), entry.getKey(), entry.getValue());
                    todosComprados.addAll(grupo);
                    gestorReportes.generarYGuardarReporte(entry.getKey(), grupo, eventoActual.getId());
                }

                double total = todosComprados.stream().mapToDouble(Boleto::getPrecio).sum();
                System.out.println(">>> Compra exitosa. Total: " + total);
                engine.executeScript("mostrarExito(" + todosComprados.size() + "," + total + ")");

            } catch (IllegalStateException | IllegalArgumentException e) {
                System.err.println(">>> ERROR compra (negocio): " + e.getMessage());
                engine.executeScript("mostrarError('" + esc(e.getMessage()) + "')");
            } catch (Exception e) {
                System.err.println(">>> ERROR compra (general): " + e.getMessage());
                engine.executeScript("mostrarError('Error: " + e.getClass().getSimpleName() + "')");
            }
        });
    }

    // ── HELPERS ──────────────────────────────────────────────

    private String matrizAJson(boolean[][] m) {
        StringBuilder sb = new StringBuilder("[");
        for (int f = 0; f < 10; f++) {
            sb.append("[");
            for (int c = 0; c < 10; c++) {
                sb.append(m[f][c]).append(c < 9 ? "," : "");
            }
            sb.append(f < 9 ? "]," : "]");
        }
        return sb.append("]").toString();
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'");
    }
}