package com.estadio.service;

// Importamos tu nueva estructura de datos de Colas
import com.estadio.datastructures.ColaReportes;
import com.estadio.model.Boleto;
import com.estadio.model.Categoria;
import com.estadio.model.ReporteVenta;
import com.estadio.repository.ReporteRepository;

import java.io.IOException;
import java.util.List;

/**
 * Coordina la generación, encolado y persistencia de reportes de venta.
 * Utiliza tu propia ColaReportes (Lista Enlazada Simple FIFO).
 */
public class GestorReportes 
{
    // Usamos la nueva clase ColaReportes que creaste
    private final ColaReportes colaReportes = new ColaReportes();
    private final ReporteRepository repositorio;

    public GestorReportes(ReporteRepository repositorio) 
    {
        if (repositorio == null) throw new IllegalArgumentException("ReporteRepository no puede ser null.");
        this.repositorio = repositorio;
    }

 
    public ReporteVenta generarYGuardarReporte(Categoria categoria, List<Boleto> boletos, String eventoId)
            throws IOException 
    {
        // ... (tus validaciones previas se quedan igual) ...

        // ── Generar y encolar ────────────────────────────────
        ReporteVenta reporte = new ReporteVenta(categoria, boletos);
        
        // Encolamos usando tu método de Alan
        colaReportes.Encolar(reporte);

        // ── Persistencia 1: Archivo .txt (Toque de Yas) ──
        try {
            repositorio.guardarEnArchivo(reporte); // <--- Nombre actualizado
        } catch (IOException ex) 
        {
            // Si falla el archivo, deshacemos el encolado (Consistencia de Yas)
            colaReportes.Desencolar(); 
            throw ex;
        }
        
        // ── Persistencia 2: Base de Datos SQL (Toque de Isaac) ──  
        // Este método es estático en ReporteRepository
        ReporteRepository.guardarReporteSQL(reporte, eventoId); // <--- Nombre actualizado

        return reporte;
    }

    public ReporteVenta extraerSiguienteReporte() 
    {
        return colaReportes.Desencolar();
    }

    public ColaReportes getColaReportes() 
    {
        return colaReportes; 
    }
    
    public int totalReportesEnCola()            
    {
        int contador = 0;
        ColaReportes colaAuxiliar = new ColaReportes();
        
        // Extraemos y contamos
        ReporteVenta reporteTemp = colaReportes.Desencolar();
        while(reporteTemp != null) 
        {
            contador++;
            colaAuxiliar.Encolar(reporteTemp);
            reporteTemp = colaReportes.Desencolar();
        }
        
        // Regresamos los reportes a la cola original
        reporteTemp = colaAuxiliar.Desencolar();
        while(reporteTemp != null)
        {
            colaReportes.Encolar(reporteTemp);
            reporteTemp = colaAuxiliar.Desencolar();
        }
        
        return contador;
    }
}