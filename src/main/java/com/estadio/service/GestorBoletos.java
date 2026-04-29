package com.estadio.service;

import com.estadio.model.Boleto;
import com.estadio.model.Categoria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Gestiona el registro de boletos vendidos por evento+categoría.
 */
public class GestorBoletos {

    private static final Map<Categoria, Double> PRECIOS_DEFAULT = new HashMap<>();
    static {
        PRECIOS_DEFAULT.put(Categoria.VIP,          500.0);
        PRECIOS_DEFAULT.put(Categoria.PREFERENCIAL,  250.0);
        PRECIOS_DEFAULT.put(Categoria.GENERAL,       100.0);
    }

    private final Map<Categoria, Double>             precios         = new HashMap<>(PRECIOS_DEFAULT);
    private final Map<String, LinkedList<Boleto>>    boletosVendidos = new HashMap<>();
    private final GestorAsientos                     gestorAsientos;

    public GestorBoletos(GestorAsientos gestorAsientos) {
        if (gestorAsientos == null)
            throw new IllegalArgumentException("GestorAsientos no puede ser null.");
        this.gestorAsientos = gestorAsientos;
    }

    /** Clave por evento + categoría para la LinkedList */
    private String clave(String eventoId, Categoria categoria) {
        return eventoId + "::" + categoria.name();
    }

    private LinkedList<Boleto> getLista(String eventoId, Categoria categoria) {
        return boletosVendidos.computeIfAbsent(clave(eventoId, categoria), k -> new LinkedList<>());
    }

    /**
     * Registra la venta de múltiples asientos en bloque para un evento específico.
     * La validación atómica se delega a GestorAsientos.
     */
    public List<Boleto> registrarVentaEnBloque(String eventoId, Categoria categoria, List<int[]> asientos) {
        if (asientos == null || asientos.isEmpty())
            throw new IllegalArgumentException("La lista de asientos no puede ser null o vacía.");

        // Validación y ocupación atómica
        gestorAsientos.ocuparAsientosEnBloque(eventoId, categoria, asientos);

        double precio = precios.get(categoria);
        List<Boleto> nuevos = new ArrayList<>();
        for (int[] a : asientos) {
            Boleto b = new Boleto(categoria, precio, a[0], a[1]);
            getLista(eventoId, categoria).add(b);
            nuevos.add(b);
        }
        return nuevos;
    }

    public LinkedList<Boleto> getBoletosVendidos(String eventoId, Categoria categoria) {
        return new LinkedList<>(getLista(eventoId, categoria));
    }

    public double getPrecio(Categoria categoria)            { return precios.get(categoria); }

    public void actualizarPrecio(Categoria categoria, double precio) {
        if (precio <= 0) throw new IllegalArgumentException("El precio debe ser positivo.");
        precios.put(categoria, precio);
    }
}
