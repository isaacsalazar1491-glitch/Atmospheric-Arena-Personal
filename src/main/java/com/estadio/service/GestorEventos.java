package com.estadio.service;

import com.estadio.model.Evento;

import java.util.*;

/**
 * Catálogo de eventos.
 */
public class GestorEventos 
{

    private final Map<String, Evento> eventos = new LinkedHashMap<>();

    public GestorEventos() 
    {
        registrarEventosIniciales();
    }

    private void registrarEventosIniciales() 
    {
        registrar(new Evento("EVT-001",
            "London Lions vs. Manchester Titans",
            "Sábado, Oct 24 · 15:00",
            "Atmospheric Arena, Londres",
            "https://upload.wikimedia.org/wikipedia/commons/2/27/City_of_Manchester_Stadium_2023_cropped.jpg"));

        registrar(new Evento("EVT-002",
            "Midnight Echo Live",
            "Domingo, Oct 25 · 20:00",
            "East Wing Stage",
            "https://lh3.googleusercontent.com/aida-public/AB6AXuCAEhwNnRJOn38tcu2ihvq8cTd5w3ddevAH1N_yRW2W3r4deGKn6AOFN2hZ-NSonHVrMlDjLh0ya4B7oS6mSujiE0DTUeLeRQ3gwa4IkR8cLnNnIYiUdlF8_-xBBcCM5eiloJgPFMVg0HEDBjwPffFYOA_zXAgH5gM2cAWc7QMMNctdWPmkLrVZ2s559cQ7pvp2xfG3B0TT4ttoh5wNIx50ZrxVKxkXg0LYRwOkzHb1lkzPZsoNvHA3ZOOCNBhvltl2GB67Zp0pw9Y"));

        registrar(new Evento("EVT-003",
            "Global E-Sports Finals",
            "Viernes, Oct 30 · 18:30",
            "Arena Central",
                "https://upload.wikimedia.org/wikipedia/commons/c/c9/Dota2_ProSeries_Melbourne_2018.jpg"));

        registrar(new Evento("EVT-004",
            "Autumn Symphony Gala",
            "Martes, Oct 28 · 19:00",
            "Main Hall",
                "https://upload.wikimedia.org/wikipedia/commons/1/19/Dublin_Philharmonic_Orchestra_performing_Tchaikovsky%27s_Symphony_No_4_in_Charlotte%2C_North_Carolina.jpg"));

        registrar(new Evento("EVT-005",
            "Taylor Swift: The Eras Tour",
            "Domingo, Nov 2 · 21:00",
            "East Wing Stage",
            "https://upload.wikimedia.org/wikipedia/commons/e/ee/Taylor_Swift_Eras_Tour_-_Arlington%2C_TX_-_Folklore_act_2.jpg"));

        registrar(new Evento("EVT-006",
            "City Tennis Masters",
            "Miércoles, Nov 5 · 10:00",
            "Court Central",
            "https://upload.wikimedia.org/wikipedia/commons/7/7e/Draper_Wimbledon.jpg"));

        registrar(new Evento("EVT-007",
            "Global Flavor Expo",
            "Viernes, Nov 7 · 12:00",
            "Festival Plaza",
            "https://upload.wikimedia.org/wikipedia/commons/c/c1/Farm_in_For%C3%AAt_%28Trooz%29.jpg"));
    }

    public void registrar(Evento evento) 
    {
        if (eventos.containsKey(evento.getId()))
            throw new IllegalArgumentException("Ya existe un evento con ID: " + evento.getId());
        eventos.put(evento.getId(), evento);
    }

    public Evento getEvento(String id) 
    {
        Evento e = eventos.get(id);
        if (e == null) throw new IllegalArgumentException("Evento no encontrado: " + id);
        return e;
    }

    public List<Evento> getTodos() 
    {
        return Collections.unmodifiableList(new ArrayList<>(eventos.values()));
    }

    public String getTodosComoJson() 
    {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Evento e : eventos.values()) 
        {
            if (!first) sb.append(",");
            sb.append(e.toJson());
            first = false;
        }
        return sb.append("]").toString();
    }
    
    
}
