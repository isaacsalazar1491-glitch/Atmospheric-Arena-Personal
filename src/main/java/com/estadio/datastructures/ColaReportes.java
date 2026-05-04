/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estadio.datastructures;

import com.estadio.model.ReporteVenta;

public class ColaReportes 
{
    private ListaEnlazadaReportes miCola = new ListaEnlazadaReportes();    
    
    public void Encolar(ReporteVenta reporte)
    {
        if (reporte == null)
        {
            System.out.println("Ups! No se puede almacenar un reporte inexistente");
            return;
        }
        
        miCola.insertarALInicio(reporte);
        System.out.println("Reporte encolado exitosamente.");
    }
    
    public ReporteVenta Desencolar()
    {
        if(miCola.estaVacia())
        {
            System.out.println("La cola esta vacia");
            return null;
        }
        return miCola.EliminarAlFinal();
    }      
    
    public void MostrarCola()
    {
        if (miCola.estaVacia())
        {
            System.out.println("No hay ventas registradas en la cola actual");
            return;
        }
        
        System.out.println("Cola de Reportes (ORDEN POR GENERACION DE TICKET)");
        miCola.mostrarLista();
    }
}