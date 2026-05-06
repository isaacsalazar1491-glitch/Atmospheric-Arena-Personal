/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estadio.datastructures;

import com.estadio.model.ReporteVenta;

/**
 * Lista enlazada simple específica para gestionar ReporteVentas.
 * Utilizada como base para la ColaReportes.
 * @author isaacgalvez
 */
public class ListaEnlazadaReportes 
{
    private class Nodo
    {
        ReporteVenta reporte;
        Nodo siguiente;
        
        public Nodo(ReporteVenta reporte)
        {
            this.reporte = reporte;
            this.siguiente = null;    
        }
    }
    
    private Nodo primero;
    
    public ListaEnlazadaReportes()
    {
        this.primero = null;
    }
    
    public void insertarALInicio(ReporteVenta reporte)
    {
        Nodo nuevo = new Nodo(reporte);
        nuevo.siguiente = primero;
        primero = nuevo;
    }
    
    public ReporteVenta EliminarAlFinal()
    {
        if(primero == null)
        {
            return null;
        }
        
        if(primero.siguiente == null)
        {
            ReporteVenta temporal = primero.reporte;
            primero = null;
            return temporal;
        }    
        
        Nodo actual = primero;
            
        while (actual.siguiente.siguiente != null)
        {
            actual = actual.siguiente;
        }
            
        ReporteVenta reporteExtraido = actual.siguiente.reporte;
        actual.siguiente = null; 
        return reporteExtraido;
    }
        
    public boolean estaVacia()
    {
        return primero == null;
    }
        
    public void mostrarLista()
    {
        Nodo actual = primero;
        while(actual != null)
        {
            System.out.println(actual.reporte.toString()); 
            actual = actual.siguiente;
        }
    }
}