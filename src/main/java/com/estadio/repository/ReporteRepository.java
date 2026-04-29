package com.estadio.repository;
import com.estadio.model.Boleto;       // ← clase Boleto
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.estadio.model.ReporteVenta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Responsable de persistir los reportes de venta en archivos .txt.
 * Un archivo por día, con formato: reporte_ventas_ddMMyyyy.txt
 */
public class ReporteRepository {

    private static final String CARPETA = "reportes/";
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("ddMMyyyy");

    public ReporteRepository() {
        // Crea la carpeta si no existe
        File dir = new File(CARPETA);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Guarda un reporte en el archivo correspondiente al día de la venta.
     * Si el archivo ya existe, agrega al final (append = true).
     *
     * @param reporte El reporte a guardar.
     * @throws IOException si ocurre un error de escritura.
     */
    public void guardar(ReporteVenta reporte) throws IOException {
        String nombreArchivo = CARPETA + "reporte_ventas_"
                + reporte.getFechaHora().format(FMT_FECHA) + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo, true))) {
            writer.write(reporte.toString());
            writer.newLine();
        }
    }

    /**
     * Retorna la ruta del archivo del día actual.
     * Útil para mostrársela al usuario.
     */
    public String getRutaArchivoHoy() {
        String fecha = java.time.LocalDate.now().format(FMT_FECHA);
        return CARPETA + "reporte_ventas_" + fecha + ".txt";
    }
    public static void guardarReporte(ReporteVenta reporte, String eventoId) {
        String sqlReporte = """
        INSERT INTO reportes_venta (fecha_hora, categoria, cantidad_boletos, total_generado, evento_id)
        VALUES (?, ?, ?, ?, ?)
    """;

        String sqlBoletoVendido = """
        INSERT INTO boletos_vendidos (reporte_id, boleto_id) VALUES (?, ?)
    """;

        String sqlMarcarVendido = """
        UPDATE boletos SET vendido = 1 WHERE id = ?
    """;

        try (Connection conn = ConexionDB.conectar()) {
            conn.setAutoCommit(false); // todo o nada

            // Insertar el reporte
            long reporteId;
            try (var stmt = conn.prepareStatement(sqlReporte, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, reporte.getFechaHora().toString());
                stmt.setString(2, reporte.getCategoria().toString());
                stmt.setInt(3, reporte.getCantidadBoletos());
                stmt.setDouble(4, reporte.getTotalGenerado());
                stmt.setString(5, eventoId);
                stmt.executeUpdate();
                reporteId = stmt.getGeneratedKeys().getLong(1);
            }

            // Insertar el detalle de cada boleto
            for (Boleto b : reporte.getBoletosVendidos()) {
                try (var stmt = conn.prepareStatement(sqlBoletoVendido)) {
                    stmt.setLong(1, reporteId);
                    stmt.setString(2, b.getId());
                    stmt.executeUpdate();
                }
                try (var stmt = conn.prepareStatement(sqlMarcarVendido)) {
                    stmt.setString(1, b.getId());
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("Reporte guardado con ID: " + reporteId);

        } catch (SQLException e) {
            System.err.println("Error al guardar reporte: " + e.getMessage());
        }
    }
}
