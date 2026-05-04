package com.estadio.model;

import java.util.UUID;

/**
 * Representa un boleto de estadio.
 *
 * MEJORAS:
 * - Validación en constructor: garantiza que el objeto nunca exista en estado inválido.
 * - disponible = false por defecto: los boletos se crean AL MOMENTO de vender,
 *   por lo que nacen ya vendidos. Esto refleja la realidad del negocio.
 */
public class Boleto 
{

    private final String   id;
    private final Categoria categoria;
    private final double   precio;
    private final String   numeroAsiento;
    private boolean        disponible;

    /**
     * @param categoria Categoría del boleto. No puede ser null.
     * @param precio    Precio del boleto. Debe ser mayor a 0.
     * @param fila      Fila del asiento (0-based).
     * @param columna   Columna del asiento (0-based).
     */
    public Boleto(Categoria categoria, double precio, int fila, int columna) 
    {
        if (categoria == null) 
        {
            throw new IllegalArgumentException("La categoría no puede ser null.");
        }
        if (precio <= 0) 
        {
            throw new IllegalArgumentException("El precio debe ser mayor a 0. Recibido: " + precio);
        }
        this.id           = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.categoria    = categoria;
        this.precio       = precio;
        this.numeroAsiento = "F" + fila + "-C" + columna;
        // Los boletos nacen vendidos: se crean únicamente en el momento de la compra.
        this.disponible   = false;
    }

    public String    getId()            
    {
        return id; 
    }
    public Categoria getCategoria()     
    {
        return categoria; 
    }
    public double    getPrecio()        
    { 
        return precio; 
    }
    public String    getNumeroAsiento() 
    {
        return numeroAsiento; 
    }
    public boolean   isDisponible()     
    {
        return disponible; 
    }

    /** Marca el boleto como vendido (estado semántico claro). */
    public void marcarComoVendido()     
    {
        this.disponible = false; 
    }

    @Override
    public String toString() 
    {
        return String.format("[%s] %s | Asiento: %s | $%.2f | %s",
                id, categoria, numeroAsiento, precio,
                disponible ? "Disponible" : "Vendido");
    }
}
