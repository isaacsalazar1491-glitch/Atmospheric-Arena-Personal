package com.estadio.service;

import com.estadio.model.Boleto;
import com.estadio.model.Categoria;
import com.estadio.datastructures.LinkedListDLC;
import com.estadio.datastructures.Nodo;
import com.estadio.datastructures.MapaPrecios; // Importamos tu nueva clase
import com.estadio.repository.ConexionDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona el registro de boletos vendidos por evento+categoría.
 */
public class GestorBoletos 
{
    // Instancia de tu clase personalizada de precios
    private final MapaPrecios mapaPrecios;
    
    private final Map<String, LinkedListDLC> boletosVendidos = new HashMap<>();
    private final GestorAsientos gestorAsientos;

   public GestorBoletos(GestorAsientos gestorAsientos) 
    {
        if (gestorAsientos == null)
            throw new IllegalArgumentException("GestorAsientos no puede ser null.");
        this.gestorAsientos = gestorAsientos;
        
        // 1. Cargamos los precios crudos desde MySQL
        HashMap<String, Double> preciosSQL = ConexionDB.cargarPrecios();
        
        // 2. Si la BD está vacía, usamos valores de respaldo (Default)
        if (preciosSQL.isEmpty()) {
            preciosSQL.put(Categoria.VIP.name(), 500.0);
            preciosSQL.put(Categoria.PREFERENCIAL.name(), 250.0);
            preciosSQL.put(Categoria.GENERAL.name(), 100.0);
        }
        
        // 3. Inicializamos tu MapaPrecios con la data real
        this.mapaPrecios = new MapaPrecios(preciosSQL);
    }

    private String clave(String eventoId, Categoria categoria) 
    {
        return eventoId + "::" + categoria.name();
    }

    private LinkedListDLC getLista(String eventoId, Categoria categoria) 
    {
        return boletosVendidos.computeIfAbsent(clave(eventoId, categoria), k -> new LinkedListDLC());
    }

    public List<Boleto> registrarVentaEnBloque(String eventoId, Categoria categoria, List<int[]> asientos) 
    {
        if (asientos == null || asientos.isEmpty())
            throw new IllegalArgumentException("La lista de asientos no puede ser null o vacía.");

        gestorAsientos.ocuparAsientosEnBloque(eventoId, categoria, asientos);

        // Obtenemos el precio usando tu clase personalizada
        double precio = mapaPrecios.obtenerPrecio(categoria.name());
        
        List<Boleto> nuevos = new ArrayList<>();
        
        for (int[] a : asientos) 
        {
            Boleto b = new Boleto(categoria, precio, a[0], a[1]);
            Nodo<Boleto> nodoBoleto = new Nodo<>(b);
            getLista(eventoId, categoria).inserta(nodoBoleto);
            nuevos.add(b);
        }
        return nuevos;
    }

    public List<Boleto> getBoletosVendidos(String eventoId, Categoria categoria) 
    {
        LinkedListDLC listaCustom = getLista(eventoId, categoria);
        List<Boleto> listaExportada = new ArrayList<>();

        Nodo<Boleto> ultimo = listaCustom.getR(); 
        
        if (ultimo != null) {
            Nodo<Boleto> aux = ultimo.getSig();
            do {
                listaExportada.add(aux.getObjeto());
                aux = aux.getSig();
            } while (aux != ultimo.getSig()); 
        }

        return listaExportada;
    }

    public double getPrecio(Categoria categoria)            
    {
        // Delegamos a tu clase MapaPrecios
        return mapaPrecios.obtenerPrecio(categoria.name()); 
    }

    public void actualizarPrecio(Categoria categoria, double precio) 
    {
        // Delegamos a tu clase MapaPrecios
        mapaPrecios.actualizarPrecio(categoria.name(), precio);
    }
}