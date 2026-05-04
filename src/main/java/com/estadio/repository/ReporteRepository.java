package com.estadio.repository;

import com.estadio.model.Boleto;
import com.estadio.model.Categoria;
import com.estadio.model.ReporteVenta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia dual: archivos .txt y MySQL.
 */
public class ReporteRepository {

    private static final String CARPETA   = "reportes/";
    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("ddMMyyyy");

    public ReporteRepository() {
        File dir = new File(CARPETA);
        if (!dir.exists()) dir.mkdirs();
    }

    // ── Archivo .txt ─────────────────────────────────────────

    public void guardarEnArchivo(ReporteVenta reporte) throws IOException {
        String nombreArchivo = CARPETA + "reporte_ventas_"
                + reporte.getFechaHora().format(FMT_FECHA) + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo, true))) {
            writer.write(reporte.toString());
            writer.newLine();
        }
    }

    // ── MySQL ─────────────────────────────────────────────────

    /**
     * Guarda el reporte en la BD.
     * NOTA: El estado de los asientos ya fue actualizado por GestorAsientos.
     *       Aquí solo guardamos el registro del reporte y la relación con boletos.
     */
    public static void guardarReporteSQL(ReporteVenta reporte, String eventoId) {

        String sqlReporte = "INSERT INTO reportes_venta " +
                            "(fecha_hora, categoria, cantidad_boletos, total_generado, evento_id) " +
                            "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.conectar()) {
            if (conn == null) {
                System.err.println(">>> [ERROR] Conexión nula al guardar reporte.");
                return;
            }
            conn.setAutoCommit(false);

            // A. Insertar el reporte principal y obtener su ID generado
            long reporteId;
            try (PreparedStatement stmt = conn.prepareStatement(
                    sqlReporte, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, reporte.getFechaHora().toString());
                stmt.setString(2, reporte.getCategoria().name());
                stmt.setInt(3, reporte.getCantidadBoletos());
                stmt.setDouble(4, reporte.getTotalGenerado());
                stmt.setString(5, eventoId);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        conn.rollback();
                        System.err.println(">>> [ERROR] No se obtuvo ID del reporte.");
                        return;
                    }
                    reporteId = rs.getLong(1);
                }
            }

            // B. Registrar cada boleto vendido en boletos_vendidos
            //    (el UPDATE de Asientos ya lo hizo GestorAsientos — no se duplica aquí)
            String sqlBoleto = "INSERT INTO boletos_vendidos (reporte_id, boleto_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlBoleto)) {
                for (Boleto b : reporte.getBoletosVendidos()) {
                    stmt.setLong(1, reporteId);
                    stmt.setString(2, b.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            System.out.println(">>> [SQL] Reporte guardado con ID: " + reporteId +
                               " | Boletos: " + reporte.getCantidadBoletos() +
                               " | Total: $" + reporte.getTotalGenerado());

        } catch (SQLException e) {
            System.err.println(">>> [ERROR SQL] Al guardar reporte: " + e.getMessage());
        }
    }

    // ── Consulta para el dashboard admin ─────────────────────

    public List<Object[]> obtenerReporteDetalladoSQL() {
        List<Object[]> datos = new ArrayList<>();
        String sql =
            "SELECT r.fecha_hora, r.categoria, r.total_generado, " +
            "IFNULL(GROUP_CONCAT(" +
            "  CONCAT('Fila:', a.fila, '-Col:', a.columna) " +
            "  ORDER BY a.fila, a.columna SEPARATOR ' | '" +
            "), 'Sin detalle') AS asientos " +
            "FROM reportes_venta r " +
            "LEFT JOIN boletos_vendidos bv ON r.id = bv.reporte_id " +
            "LEFT JOIN Asientos a ON bv.boleto_id = CAST(a.id_asiento AS CHAR) " +
            "GROUP BY r.id, r.fecha_hora, r.categoria, r.total_generado " +
            "ORDER BY r.fecha_hora DESC";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                datos.add(new Object[]{
                    rs.getString(1),  // fecha
                    rs.getString(2),  // categoría
                    rs.getDouble(3),  // ingresos
                    rs.getString(4)   // asientos
                });
            }
        } catch (SQLException e) {
            System.err.println(">>> [ERROR] Reporte admin: " + e.getMessage());
        }
        return datos;
    }
}