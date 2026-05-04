/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.estadio.datastructures;
import com.estadio.model.Boleto;

/**
 * Lista enlazada doble circular para gestionar Boletos
 * Ordenada por el ID (String UUID) del Boleto.
 * @author AlanP
 */
public class LinkedListDLC 
{
    
    private Nodo<Boleto> r; // 'r' es el ultimo nodo

    public Nodo<Boleto> getR() 
    {
        return r;
    }

    public void setR(Nodo<Boleto> r) 
    {
        this.r = r;
    }

    public void inserta(Nodo<Boleto> nodo) 
    {
        if (nodo == null) 
        {
            System.out.println("El nodo esta vacio, no se puede insertar");
            return;
        } 
        
        if (r == null) 
        { // Caso 2: Si la lista esta vacía
            System.out.println("La lista esta vacia, se inserta el nodo como cabeza");
            r = nodo; // r referencia a nodo
            r.setSig(nodo);
            r.setAnt(nodo); // El nodo se referencia a si mismo, es circular
        } else 
        {
            String idNuevo = nodo.getObjeto().getId();
            String idPrimero = r.getSig().getObjeto().getId();
            String idUltimo = r.getObjeto().getId();

            // Caso 3: Extremos, si va antes del primero o despues del ultimo
            if (idNuevo.compareTo(idPrimero) < 0 || idNuevo.compareTo(idUltimo) > 0) 
            {
                nodo.setSig(r.getSig()); // El siguiente es el primero
                nodo.setAnt(r); // El anterior es el actual ULTIMO
                r.getSig().setAnt(nodo); // El antiguo primero ahora ve hacia atras al nuevo nodo
                r.setSig(nodo); // El ultimo apunta al nuevo nodo

                if (idNuevo.compareTo(idUltimo) > 0) 
                { 
                    // Si es mayor, el nuevo nodo se convierte en el ultimo
                    r = nodo;
                }
            } else 
            {
                Nodo<Boleto> aux = r.getSig(); // aux apunta al primero
                while (aux != r) 
                { // Hasta no llegar al ultimo
                    String idSiguiente = aux.getSig().getObjeto().getId();
                    
                    // Si el ID nuevo es menor que el ID del SIGUIENTE de aux
                    if (idNuevo.compareTo(idSiguiente) < 0) 
                    {
                        nodo.setSig(aux.getSig()); // su sigNodo sera el sig de aux
                        nodo.setAnt(aux); // su antNodo sera aux
                        aux.getSig().setAnt(nodo); // el sig de aux ahora ve hacia atras al nodo
                        aux.setSig(nodo);
                        break; // Se rompe el ciclo porque ya se inserto el nodo
                    } else {
                        aux = aux.getSig(); // Para ir avanzando
                    }
                }
            }
        }
    }

    // Ojo: El parámetro ahora es un String, porque el ID del boleto es String
    public Nodo<Boleto> elimina(String id) 
    {
        Nodo<Boleto> nodoE = null;
        
        if (r == null) 
        {
            System.out.println("Lista Vacia");
        } else 
        {
            String idPrimero = r.getSig().getObjeto().getId();
            String idUltimo = r.getObjeto().getId();

            // Si el ID buscado es menor al primero o mayor al ultimo, no existe
            if (idPrimero.compareTo(id) > 0 || idUltimo.compareTo(id) < 0) 
            {
                System.out.println("El id no se encuentra en la lista");
            } else 
            {
                if (idPrimero.equals(id)) { // Caso: Eliminar el primero
                    System.out.println("Se elimina el nodo primero");
                    nodoE = r.getSig(); 
                    
                    if (r == r.getSig()) { // Si es el unico elemento
                        r = null;
                    } else 
                    {
                        nodoE.getSig().setAnt(r); // El segundo nodo ahora ve hacia atras al ultimo
                        r.setSig(nodoE.getSig()); // El ultimo ahora apunta al segundo nodo
                    }
                    
                    if (nodoE != null) 
                    {
                        nodoE.setSig(null); 
                        nodoE.setAnt(null);
                    }
                    
                } else if (idUltimo.equals(id)) { // Caso: Eliminar el ultimo
                    System.out.println("Se elimina el nodo ultimo");
                    nodoE = r;
                    r = r.getAnt(); // El nuevo ultimo es el penultimo
                    r.setSig(nodoE.getSig()); // Cierra el circulo
                    nodoE.getSig().setAnt(r); // Cierra el circulo
                    
                    nodoE.setSig(null);
                    nodoE.setAnt(null);
                    
                } else 
                { // Logica intermedia
                    Nodo<Boleto> aux = r.getSig(); 
                    
                    while (aux != r) 
                    {
                        // Verificamos si el SIGUIENTE de aux es el que queremos eliminar
                        if (aux.getSig().getObjeto().getId().equals(id)) {
                            nodoE = aux.getSig();
                            aux.setSig(nodoE.getSig()); // actualizo al vecino izq
                            nodoE.getSig().setAnt(aux); // actualizo al vecino Derecho
                            nodoE.setSig(null);
                            nodoE.setAnt(null);
                            break;
                        } else 
                        {
                            // Si el siguiente ya es mayor que el ID buscado, nos pasamos y no existe
                            if (aux.getSig().getObjeto().getId().compareTo(id) > 0) 
                            {
                                System.out.println("El id no se encuentra en la lista");
                                break;
                            } else 
                            {
                                aux = aux.getSig(); // Para ir avanzando
                            }
                        }
                    }
                }
            }
        }
        return nodoE;
    }

    public void depliegue() 
    {
        int i = 1;
        if (r == null) 
        {
            System.out.println("Lista vacia");
            return;
        }
        
        Nodo<Boleto> aux = r.getSig(); // Empezar en el primero
        
        do {
            System.out.printf("Nodo no." + i + "\t" + aux.toString() + "\n");
            System.out.println("Datos del Nodo\n" + aux.getObjeto().toString() + "\n");
            i++;
            aux = aux.getSig();
        } while (aux != r.getSig()); // Vuelta completa hasta volver al primero
    }
}