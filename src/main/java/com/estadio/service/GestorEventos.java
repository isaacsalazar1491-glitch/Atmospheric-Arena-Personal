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
            "La prueba final: Toluca vs Chivas",
            "Sábado, Mayo 27 · 19:00",
            "Estadio Nemesio Diez, Toluca",
            "https://th.bing.com/th/id/R.bf8ab8a28c949f6bd629663f7467faf8?rik=ZmLkAw8Qq2ZswQ&riu=http%3a%2f%2fstadiumdb.com%2fpictures%2fstadiums%2fmex%2festadio_nemesio_diez%2festadio_nemesio_diez22.jpg&ehk=7GDi%2f9cVovVgRM%2fVbLGeXApFjntmOMZIgVYwfqaunXo%3d&risl=&pid=ImgRaw&r=0"));

        registrar(new Evento("EVT-002",
            "BTS Arirang Tour",
            "Jueves, Mayo 07 · 20:00",
            "Estadio GNP, CDMX",
            "https://pbs.twimg.com/media/HEPOiRvb0AAhjnr?format=jpg&name=medium"));

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
