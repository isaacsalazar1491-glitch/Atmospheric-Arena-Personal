package com.estadio.model;

/**
 * Representa un evento del estadio.
 * Un evento NO está atado a una categoría — expone las tres (VIP, PREFERENCIAL, GENERAL).
 * La categoría la elige el usuario en la pantalla de selección.
 */
public class Evento {

    private final String id;
    private final String nombre;
    private final String fecha;
    private final String lugar;
    private final String imagenUrl;

    public Evento(String id, String nombre, String fecha, String lugar, String imagenUrl) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("El id del evento no puede ser vacío.");
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre del evento no puede ser vacío.");

        this.id        = id;
        this.nombre    = nombre;
        this.fecha     = fecha;
        this.lugar     = lugar;
        this.imagenUrl = imagenUrl;
    }

    public String getId()        { return id; }
    public String getNombre()    { return nombre; }
    public String getFecha()     { return fecha; }
    public String getLugar()     { return lugar; }
    public String getImagenUrl() { return imagenUrl; }

    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"nombre\":\"%s\",\"fecha\":\"%s\",\"lugar\":\"%s\",\"imagenUrl\":\"%s\"}",
            id, esc(nombre), esc(fecha), esc(lugar), esc(imagenUrl));
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\"", "\\\"").replace("'", "\\'");
    }
}
