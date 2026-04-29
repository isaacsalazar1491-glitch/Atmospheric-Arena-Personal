package com.estadio.datastructures;

import java.util.LinkedList;

/**
 * Cola genérica FIFO implementada sobre LinkedList.
 * Se usa para almacenar reportes de ventas en orden de generación.
 *
 * @param <T> Tipo de elemento almacenado.
 */
public class Cola<T> {

    private final LinkedList<T> elementos = new LinkedList<>();

    /** Agrega un elemento al final de la cola. */
    public void encolar(T elemento) {
        if (elemento == null) throw new IllegalArgumentException("No se puede encolar un elemento nulo.");
        elementos.addLast(elemento);
    }

    /** Extrae y retorna el elemento del frente de la cola. */
    public T desencolar() {
        if (estaVacia()) throw new IllegalStateException("La cola está vacía.");
        return elementos.pollFirst();
    }

    /** Consulta el elemento al frente sin extraerlo. */
    public T frente() {
        if (estaVacia()) throw new IllegalStateException("La cola está vacía.");
        return elementos.peekFirst();
    }

    public boolean estaVacia() { return elementos.isEmpty(); }
    public int tamanio()       { return elementos.size(); }

    /** Retorna todos los elementos sin modificar la cola. */
    public LinkedList<T> getElementos() {
        return new LinkedList<>(elementos); // copia defensiva
    }
}
