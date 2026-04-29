package com.estadio.service;

import com.estadio.model.Categoria;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona asientos por evento + categoría.
 * Clave: "eventoId::CATEGORIA" → boolean[][]
 * Cada evento tiene su propia matriz independiente.
 */
public class GestorAsientos {

    public static final int FILAS    = 10;
    public static final int COLUMNAS = 10;

    // Clave compuesta: eventoId + "::" + categoria.name()
    private final Map<String, boolean[][]> matrices = new HashMap<>();

    private String clave(String eventoId, Categoria categoria) {
        return eventoId + "::" + categoria.name();
    }

    /** Inicializa la matriz de un evento si aún no existe. */
    private boolean[][] obtenerOCrear(String eventoId, Categoria categoria) {
        return matrices.computeIfAbsent(clave(eventoId, categoria),
                k -> new boolean[FILAS][COLUMNAS]);
    }

    // ── Consultas ────────────────────────────────────────────

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
        boolean[][] m = obtenerOCrear(eventoId, categoria);
        int count = 0;
        for (int i = 0; i < FILAS; i++)
            for (int j = 0; j < COLUMNAS; j++)
                if (!m[i][j]) count++;
        return count;
    }

    // ── Ocupación individual ─────────────────────────────────

    public void ocuparAsiento(String eventoId, Categoria categoria, int fila, int columna) {
        validarCoordenadas(fila, columna);
        if (!isDisponible(eventoId, categoria, fila, columna)) {
            throw new IllegalStateException(
                "Asiento F" + fila + "-C" + columna + " ya está ocupado en " + eventoId);
        }
        obtenerOCrear(eventoId, categoria)[fila][columna] = true;
    }

    // ── Ocupación atómica en bloque ──────────────────────────

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
        // FASE 2: ocupar todos
        for (int[] a : asientos)
            obtenerOCrear(eventoId, categoria)[a[0]][a[1]] = true;
    }

    private void validarCoordenadas(int fila, int columna) {
        if (fila < 0 || fila >= FILAS || columna < 0 || columna >= COLUMNAS)
            throw new IllegalArgumentException("Coordenadas fuera de rango: F" + fila + "-C" + columna);
    }
}
