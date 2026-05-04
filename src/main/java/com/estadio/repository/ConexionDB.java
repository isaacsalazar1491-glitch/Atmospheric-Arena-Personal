/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.estadio.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Motor de conexión único para todo el sistema.
 * @author isaacgalvez
 */
public class ConexionDB {
    
    private static final String URL = "jdbc:mysql://localhost:3306/estadio_boletos";
    private static final String USER = "root";
    private static final String PASS = ""; // Ajusta si tienes contraseña

    /**
     * Método central de conexión (usado por ReporteRepository)
     */
    public static Connection conectar() {
        try {
            // Asegúrate de tener el driver de MySQL en tus librerías
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            System.out.println("Error Crítico de Conexión: " + e.getMessage());
            return null;
        }
    }

    /**
     * Carga el catálogo de precios desde SQL (usado por GestorBoletos)
     */
    public static HashMap<String, Double> cargarPrecios() {
        HashMap<String, Double> mapa = new HashMap<>();
        String sql = "SELECT nombre, precio FROM Categorias";
        
        try (Connection conn = conectar(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (conn != null) {
                while (rs.next()) {
                    // Guardamos en mayúsculas para que coincida con Categoria.name()
                    mapa.put(rs.getString("nombre").toUpperCase(), rs.getDouble("precio"));
                }
            }
        } catch (Exception e) {
            System.out.println("Error al recuperar precios de BD: " + e.getMessage());
        }
        return mapa;
    }
}