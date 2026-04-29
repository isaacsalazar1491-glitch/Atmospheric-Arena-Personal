package com.estadio.service;

import com.estadio.datastructures.Cola;
import com.estadio.model.Boleto;
import com.estadio.model.Categoria;
import com.estadio.model.ReporteVenta;
import com.estadio.repository.ReporteRepository;

import java.io.IOException;
import java.util.List;

/**
 * Coordina la generación, encolado y persistencia de reportes de venta.
 *
 * MEJORAS:
 *  - Validación de inputs (Categoria null, lista null/vacía).
 *  - Verificación de consistencia: todos los boletos deben pertenecer a la categoría indicada.
 *  - Si el guardado en disco falla, el reporte se desencola (consistencia entre cola y archivo).
 */
public class GestorReportes {

    private final Cola<ReporteVenta> colaReportes = new Cola<>();
    private final ReporteRepository  repositorio;

    public GestorReportes(ReporteRepository repositorio) {
        if (repositorio == null) throw new IllegalArgumentException("ReporteRepository no puede ser null.");
        this.repositorio = repositorio;
    }

    /**
     * Genera un reporte, lo encola y lo persiste en disco.
     *
     * Validaciones:
     *  1. Categoria no puede ser null.
     *  2. La lista de boletos no puede ser null ni vacía.
     *  3. Todos los boletos deben pertenecer a la Categoria indicada.
     *
     * Consistencia:
     *  - Si la escritura en disco falla, el reporte se desencola para mantener
     *    coherencia entre la cola en memoria y los archivos persistidos.
     *
     * @throws IllegalArgumentException si los inputs son inválidos o inconsistentes.
     * @throws IOException              si falla la escritura del archivo.
     */
    public ReporteVenta generarYGuardarReporte(Categoria categoria, List<Boleto> boletos, String eventoId)
            throws IOException {

        // ── Validación de inputs ─────────────────────────────
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser null.");
        }
        if (boletos == null || boletos.isEmpty()) {
            throw new IllegalArgumentException(
                "La lista de boletos no puede ser null ni vacía.");
        }

        // ── Verificación de consistencia ─────────────────────
        for (Boleto b : boletos) {
            if (b == null) {
                throw new IllegalArgumentException("La lista contiene un boleto null.");
            }
            if (!b.getCategoria().equals(categoria)) {
                throw new IllegalArgumentException(
                    "Inconsistencia: el boleto " + b.getId() +
                    " pertenece a la categoría " + b.getCategoria() +
                    " pero se esperaba " + categoria + ".");
            }
        }

        // ── Generar y encolar ────────────────────────────────
        ReporteVenta reporte = new ReporteVenta(categoria, boletos);
        colaReportes.encolar(reporte);

        // ── Persistir (si falla → desencolar para mantener consistencia) ──
        try {
            repositorio.guardar(reporte);
        } catch (IOException ex) {
            colaReportes.desencolar(); // rollback de la cola
            throw ex;
        }
        // ── Persistir en base de datos ──  ← ESTO FALTABA
        ReporteRepository.guardarReporte(reporte,eventoId);

        return reporte;
    }

    public ReporteVenta extraerSiguienteReporte() {
        return colaReportes.desencolar();
    }

    public Cola<ReporteVenta> getColaReportes() { return colaReportes; }
    public int totalReportesEnCola()            { return colaReportes.tamanio(); }
}
