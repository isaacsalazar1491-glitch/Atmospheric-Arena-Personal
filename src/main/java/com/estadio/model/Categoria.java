package com.estadio.model;

/**
 * Enum que representa las categorías de boletos disponibles.
 *
 * DECISIÓN DE DISEÑO: El precio fue eliminado de este enum.
 * Los precios son gestionados por GestorBoletos (HashMap), que es la
 * única fuente de verdad. Tener el precio aquí también causaría:
 *   - Duplicación de datos → riesgo de inconsistencias.
 *   - Responsabilidades mezcladas → viola el principio de responsabilidad única.
 *   - Estado mutable en un enum → antipatrón en Java.
 */
public enum Categoria {
    VIP,
    PREFERENCIAL,
    GENERAL;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
