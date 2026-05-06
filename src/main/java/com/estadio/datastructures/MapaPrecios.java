/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estadio.datastructures;

import java.util.HashMap;

/**
 * @author isaacgalvez
 */
public class MapaPrecios 
{
    private HashMap<String, Double> catalogoPrecios;
    
    public MapaPrecios(HashMap<String, Double> preciosDesdeBD) 
    {
        this.catalogoPrecios = new HashMap<>(); 

        if(preciosDesdeBD != null && !preciosDesdeBD.isEmpty()) 
        {
            this.catalogoPrecios = preciosDesdeBD;
        } 
        else 
        {
            this.catalogoPrecios = new HashMap<>();
            System.out.println("Advertencia: El catálogo inició vacío. Revisa la conexión a BD.");            
        }
    }
    
    public double obtenerPrecio(String categoria) 
    {
        // VALIDACIÓN NIVEL 1: Evitar que colapse si nos mandan un nulo o un texto vacío
        if (categoria == null || categoria.trim().isEmpty()) 
        {
            System.out.println("Error de Seguridad: La categoría recibida es nula o está vacía.");
            return 0.0;
        }
        
        String categoriaLimpia = categoria.trim().toUpperCase();
        
        if(catalogoPrecios.containsKey(categoriaLimpia)) 
        {
            return catalogoPrecios.get(categoriaLimpia);
        } 
        else 
        {
            System.out.println("Ups! La categoría '" + categoriaLimpia + "' no existe en el sistema."); 
            return 0.0;
        }
    }
    
    public void actualizarPrecio(String categoria, double nuevoPrecio) 
    {
        if (categoria == null || categoria.trim().isEmpty()) 
        {
            System.out.println("Error de Seguridad: No se puede actualizar una categoría nula.");
            return; 
        }
        
        String categoriaLimpia = categoria.trim().toUpperCase();
        
        if(nuevoPrecio > 0 && catalogoPrecios.containsKey(categoriaLimpia)) 
        {
            catalogoPrecios.put(categoriaLimpia, nuevoPrecio);
            System.out.println("El precio de " + categoriaLimpia + " se actualizo a $" + nuevoPrecio);        
        } 
        else if(nuevoPrecio <= 0) 
        {
            System.out.println("Ups! El precio no puede ser negativo o 0");
        } 
        else 
        {
            System.out.println("Ups! No se pudo actualizar. La categoría '" + categoriaLimpia + "' no existe");            
        }
    }
  
    public void mostrarTodosLosPrecios() 
    {
        if (catalogoPrecios.isEmpty()) 
        {
            System.out.println("El catálogo está completamente vacío.");
            return;
        }
        
        System.out.println("\n--- CATÁLOGO DE PRECIOS ---");
        for (String categoria : catalogoPrecios.keySet()) 
        {
            System.out.println(categoria + ": $" + catalogoPrecios.get(categoria));
        }
    }
}