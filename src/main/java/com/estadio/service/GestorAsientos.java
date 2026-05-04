package com.estadio.service;

import com.estadio.model.Categoria;
import com.estadio.repository.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona asientos por evento + categoría.
 * id_categoria en BD: 1=VIP, 2=PREFERENCIAL, 3=GENERAL
 */
public class GestorAsientos {

    public static final int FILAS    = 10;
    public static final int COLUMNAS = 10;

    // Cache en memoria: clave "eventoId::CATEGORIA" → boolean[][]
    private final Map<String, boolean[][]> matrices = new HashMap<>();

    // Mapeo entre el enum Categoria y el id numérico de la BD
    private int categoriaAId(Categoria categoria) {
        switch (categoria) {
            case VIP:          return 1;
            case PREFERENCIAL: return 2;
            case GENERAL:      return 3;
            default: throw new IllegalArgumentException("Categoría desconocida: " + categoria);
        }
    }

    private String clave(String eventoId, Categoria categoria) {
        return eventoId + "::" + categoria.name();
    }

    /**
     * Obtiene la matriz desde cache. Si no existe, la carga desde MySQL.
     */
    private boolean[][] obtenerOCrear(String eventoId, Categoria categoria) {
        return matrices.computeIfAbsent(
            clave(eventoId, categoria),
            k -> cargarDesdeDB(eventoId, categoria)
        );
    }

    /**
     * Lee el estado real de los asientos desde MySQL usando id_categoria numérico.
     */
    private boolean[][] cargarDesdeDB(String eventoId, Categoria categoria) {
        boolean[][] matriz = new boolean[FILAS][COLUMNAS];
        String sql = "SELECT fila, columna, estado FROM Asientos " +
                     "WHERE id_evento = ? AND id_categoria = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventoId);
            ps.setInt(2, categoriaAId(categoria));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int f = rs.getInt("fila");
                    int c = rs.getInt("columna");
                    boolean ocupado = !"Disponible".equalsIgnoreCase(rs.getString("estado"));
                    if (f < FILAS && c < COLUMNAS) {
                        matriz[f][c] = ocupado;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar asientos desde BD: " + e.getMessage());
        }
        return matriz;
    }

    // ── Consultas ─────────────────────────────────────────────

    public boolean isDisponible(String eventoId, Categoria categoria, int fila, int columna) {
        validarCoordenadas(fila, columna);
        return !obtenerOCrear(eventoId, categoria)[fila][columna];
    }

    public boolean[][] getMatrizAsientos(String eventoId, Categoria categoria) {
        boolean[][] original = obtenerOCrear(eventoId, categoria);
        boolean[][] copia    = new boolean[FILAS][COLUMNAS];
        for (int i = 0; i < FILAS; i++)
            System.arraycopy(original[i], 0, copia[i], 0, COLUMNAS);
        return copia;
    }

    public int contarDisponibles(String eventoId, Categoria categoria) {
        String sql = "SELECT COUNT(*) FROM Asientos " +
                     "WHERE id_evento = ? AND id_categoria = ? AND estado = 'Disponible'";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventoId);
            ps.setInt(2, categoriaAId(categoria));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar disponibles: " + e.getMessage());
        }
        return 0;
    }

    // ── Ocupación individual ──────────────────────────────────

    public void ocuparAsiento(String eventoId, Categoria categoria, int fila, int columna) {
        validarCoordenadas(fila, columna);
        if (!isDisponible(eventoId, categoria, fila, columna)) {
            throw new IllegalStateException(
                "Asiento F" + fila + "-C" + columna + " ya está ocupado en " + eventoId);
        }
        actualizarEstadoEnDB(eventoId, categoria, fila, columna, "Vendido");
        obtenerOCrear(eventoId, categoria)[fila][columna] = true;
    }

    // ── Ocupación atómica en bloque ───────────────────────────

    public void ocuparAsientosEnBloque(String eventoId, Categoria categoria, List<int[]> asientos) {
        if (asientos == null || asientos.isEmpty())
            throw new IllegalArgumentException("La lista de asientos no puede ser null o vacía.");

        // FASE 1: validar todos
        for (int[] a : asientos) {
            validarCoordenadas(a[0], a[1]);
            if (!isDisponible(eventoId, categoria, a[0], a[1])) {
                throw new IllegalStateException(
                    "El asiento F" + a[0] + "-C" + a[1] + " ya está ocupado. " +
                    "No se procesó ningún asiento.");
            }
        }

        // FASE 2: persistir en MySQL (transacción atómica)
        actualizarBloqueEnDB(eventoId, categoria, asientos, "Vendido");

        // FASE 3: actualizar cache en memoria
        boolean[][] matriz = obtenerOCrear(eventoId, categoria);
        for (int[] a : asientos) {
            matriz[a[0]][a[1]] = true;
        }
    }

    // ── Helpers de BD ─────────────────────────────────────────

    private void actualizarEstadoEnDB(String eventoId, Categoria categoria,
                                       int fila, int columna, String nuevoEstado) {
        String sql = "UPDATE Asientos SET estado = ? " +
                     "WHERE id_evento = ? AND id_categoria = ? AND fila = ? AND columna = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, eventoId);
            ps.setInt(3, categoriaAId(categoria));
            ps.setInt(4, fila);
            ps.setInt(5, columna);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar asiento: " + e.getMessage());
            throw new RuntimeException("No se pudo guardar el cambio en la base de datos.", e);
        }
    }

    /**
     * Actualiza un bloque de asientos en una sola transacción.
     * Si falla uno → rollback de todos.
     */
    private void actualizarBloqueEnDB(String eventoId, Categoria categoria,
                                       List<int[]> asientos, String nuevoEstado) {
        String sql = "UPDATE Asientos SET estado = ? " +
                     "WHERE id_evento = ? AND id_categoria = ? AND fila = ? AND columna = ?";
        Connection conn = null;
        try {
            conn = ConexionDB.conectar();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int[] a : asientos) {
                    ps.setString(1, nuevoEstado);
                    ps.setString(2, eventoId);
                    ps.setInt(3, categoriaAId(categoria));
                    ps.setInt(4, a[0]);
                    ps.setInt(5, a[1]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            System.out.println(">>> [BD] " + asientos.size() +
                               " asiento(s) de " + categoria.name() + " marcados como " + nuevoEstado);

        } catch (SQLException e) {
            System.err.println("Error en transacción: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignorar */ }
            }
            throw new RuntimeException("No se pudo guardar la compra en la base de datos.", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* ignorar */ }
            }
        }
    }

    private void validarCoordenadas(int fila, int columna) {
        if (fila < 0 || fila >= FILAS || columna < 0 || columna >= COLUMNAS)
            throw new IllegalArgumentException(
                "Coordenadas fuera de rango: F" + fila + "-C" + columna);
    }
}