/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estadio.datastructures;

/**
 * 
 * 
 */
public class Nodo<T> 
{
    
    private T objeto;       // La información que guarda el nodo (el Boleto)
    private Nodo<T> sig;    // Apuntador al siguiente nodo
    private Nodo<T> ant;    // Apuntador al nodo anterior

    // Constructor vacío
    public Nodo() 
    {
    }

    // Constructor con objeto
    public Nodo(T objeto) 
    {
        this.objeto = objeto;
    }

    // --- Getters y Setters ---

    public T getObjeto() 
    {
        return objeto;
    }

    public void setObjeto(T objeto) 
    {
        this.objeto = objeto;
    }

    public Nodo<T> getSig() 
    {
        return sig;
    }

    public void setSig(Nodo<T> sig) 
    {
        this.sig = sig;
    }

    public Nodo<T> getAnt() 
    {
        return ant;
    }

    public void setAnt(Nodo<T> ant) 
    {
        this.ant = ant;
    }
}