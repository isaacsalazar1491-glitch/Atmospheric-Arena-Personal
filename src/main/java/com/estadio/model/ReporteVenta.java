package com.estadio.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contiene el detalle de una venta completada.
 * Se genera después de cada compra y se encola para persistencia.
 */
public class ReporteVenta 
{

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final LocalDateTime fechaHora;
    private final Categoria categoria;
    private final List<Boleto> boletosVendidos;
    private final double totalGenerado;

    public ReporteVenta(Categoria categoria, List<Boleto> boletosVendidos) 
    {
        this.fechaHora       = LocalDateTime.now();
        this.categoria       = categoria;
        this.boletosVendidos = boletosVendidos;
        this.totalGenerado   = boletosVendidos.stream()
                                              .mapToDouble(Boleto::getPrecio)
                                              .sum();
    }

    // ── Getters ─────────────────────────────────────────────
    public LocalDateTime getFechaHora()         
    { 
        return fechaHora; 
    }
    public Categoria getCategoria()             
    { 
        return categoria; 
    }
    public List<Boleto> getBoletosVendidos()    
    { return boletosVendidos; }
    public double getTotalGenerado()            
    { 
        return totalGenerado; 
    }
    public int getCantidadBoletos()             
    {
        return boletosVendidos.size(); 
    }

    /**
     * Genera el contenido del reporte listo para ser escrito en .txt
     */
    @Override
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();
        String separador = "=".repeat(50);

        sb.append(separador).append("\n");
        sb.append("        REPORTE DE VENTA - ESTADIO\n");
        sb.append(separador).append("\n");
        sb.append("Fecha y hora    : ").append(fechaHora.format(FORMATO)).append("\n");
        sb.append("Categoría       : ").append(categoria).append("\n");
        sb.append("Boletos vendidos: ").append(getCantidadBoletos()).append("\n");
        sb.append("Total generado  : $").append(String.format("%.2f", totalGenerado)).append("\n");
        sb.append("\nDetalle de asientos vendidos:\n");

        for (Boleto b : boletosVendidos) 
        {
            sb.append("  - ID: ").append(b.getId())
              .append(" | Asiento: ").append(b.getNumeroAsiento())
              .append(" | Precio: $").append(String.format("%.2f", b.getPrecio())).append("\n");
        }

        sb.append(separador).append("\n");
        return sb.toString();
    }
}
