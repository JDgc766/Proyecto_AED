package Proyecto_final;

import java.sql.*;
import javax.swing.*;

public class ConexionDB {

    // Agregamos busy_timeout para evitar SQLITE_BUSY
    private static final String DB_URL =
            "jdbc:sqlite:DaBe/Farmacia.db?busy_timeout=5000";

    public static Connection obtenerConexion() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            return conn;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al conectar a la base de datos: " + e.getMessage());
            return null;
        }
    }
}
