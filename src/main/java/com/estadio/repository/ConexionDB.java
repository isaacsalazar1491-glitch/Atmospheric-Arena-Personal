package com.estadio.repository;  // ← Esta línea debe ser la primera

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/ventaboletos";
    private static final String USER = "root";
    private static final String PASSWORD = "admin123";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}