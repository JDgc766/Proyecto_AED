package Proyecto_final;

import java.sql.*;
import javax.swing.*;
import java.text.DecimalFormat;
import java.io.File;

public class BusquedaProductos {

    // Ruta absoluta de tu base de datos
    private static final String DB_URL = "jdbc:sqlite:C:\\SqlLite\\Databases\\Farmacia.db";

    public static void main(String[] args) {
        // Crear base de datos y tabla si no existen
        crearBaseDeDatos();

        while(true) {
            String opcion = JOptionPane.showInputDialog(
                    "Seleccione una opción:\n" +
                    "1. Listar productos\n" +
                    "2. Agregar producto\n" +
                    "3. Salir");

            if(opcion == null || opcion.equals("3")) {
                break;
            }

            switch(opcion) {
                case "1":
                    listarProductos();
                    break;
                case "2":
                    agregarProducto();
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Opción no válida");
            }
        }
    }

    private static void crearBaseDeDatos() {
        try {
            // Crear carpeta si no existe
            File dbFile = new File("C:\\SqlLite\\Databases");
            if (!dbFile.exists()) {
                dbFile.mkdirs();
            }

            // Conexión crea automáticamente el archivo si no existe
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt = conn.createStatement()) {

                // Crear tabla si no existe
                String sql = "CREATE TABLE IF NOT EXISTS Productos (" +
                        "id_producto INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nombre TEXT NOT NULL," +
                        "nombre_generico TEXT NOT NULL," +
                        "precio_compra REAL NOT NULL," +
                        "precio_venta REAL NOT NULL," +
                        "fecha_caducidad TEXT NOT NULL," +
                        "ventas INTEGER DEFAULT 0" +
                        ")";
                stmt.execute(sql);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al crear base de datos: " + e.getMessage());
        }
    }

    private static void listarProductos() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Productos")) {

            StringBuilder sb = new StringBuilder();
            DecimalFormat df = new DecimalFormat("#0.00");

            while(rs.next()) {
                sb.append("ID: ").append(rs.getInt("id_producto"))
                  .append(", Nombre: ").append(rs.getString("nombre"))
                  .append(", Genérico: ").append(rs.getString("nombre_generico"))
                  .append(", Compra: $").append(df.format(rs.getDouble("precio_compra")))
                  .append(", Venta: $").append(df.format(rs.getDouble("precio_venta")))
                  .append(", Caduca: ").append(rs.getString("fecha_caducidad"))
                  .append(", Ventas: ").append(rs.getInt("ventas"))
                  .append("\n");
            }

            JOptionPane.showMessageDialog(null, sb.length() > 0 ? sb.toString() : "No hay productos");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private static void agregarProducto() {
        String nombre = JOptionPane.showInputDialog("Nombre del producto:");
        String nombreGen = JOptionPane.showInputDialog("Nombre genérico:");
        String precioCompraStr = JOptionPane.showInputDialog("Precio de compra:");
        String precioVentaStr = JOptionPane.showInputDialog("Precio de venta:");
        String fechaCad = JOptionPane.showInputDialog("Fecha de caducidad (YYYY-MM-DD):");

        try {
            double precioCompra = Double.parseDouble(precioCompraStr);
            double precioVenta = Double.parseDouble(precioVentaStr);

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO Productos(nombre, nombre_generico, precio_compra, precio_venta, fecha_caducidad) VALUES (?, ?, ?, ?, ?)")) {

                ps.setString(1, nombre);
                ps.setString(2, nombreGen);
                ps.setDouble(3, precioCompra);
                ps.setDouble(4, precioVenta);
                ps.setString(5, fechaCad);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(null, "Producto agregado exitosamente!");

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error al agregar producto: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Precio inválido");
        }
    }
}