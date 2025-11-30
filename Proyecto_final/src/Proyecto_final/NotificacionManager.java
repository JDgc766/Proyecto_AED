package Proyecto_final;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificacionManager {

    // Inserta un nuevo aviso en la base de datos
    public static void agregarNotificacion(String tipo, String mensaje) {
        String sql = "INSERT INTO Notificacion (Tipo, Mensaje, Fecha, Leido) VALUES (?, ?, ?, 'N')";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipo);
            ps.setString(2, mensaje);

            // Guardar la fecha y hora actual
            String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ps.setString(3, fechaActual);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ejemplo de uso rápido:
    // NotificacionManager.agregarNotificacion("STOCK_BAJO", "El producto X tiene stock bajo.");
}
