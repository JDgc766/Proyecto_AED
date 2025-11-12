package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AgregarProducto extends JPanel {

    // --- COLORES ---
    private final Color COLOR_FONDO = new Color(35, 35, 35);
    private final Color COLOR_BOTON = new Color(55, 55, 55);
    private final Color COLOR_HOVER = new Color(75, 75, 75);
    private final Color COLOR_TEXTO = new Color(230, 230, 230);

    private JFrame frameActual;

    public AgregarProducto(JFrame frameActual) {
        this.frameActual = frameActual;

        setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // --- PANEL DE FORMULARIO ---
        JPanel panelFormulario = new JPanel(new GridLayout(7, 2, 15, 15));
        panelFormulario.setBackground(COLOR_FONDO);

        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setForeground(COLOR_TEXTO);
        JTextField txtNombre = new JTextField();

        JLabel lblNombreGenerico = new JLabel("Nombre Genérico:");
        lblNombreGenerico.setForeground(COLOR_TEXTO);
        JTextField txtNombreGenerico = new JTextField();

        JLabel lblPrecioCompra = new JLabel("Precio Compra:");
        lblPrecioCompra.setForeground(COLOR_TEXTO);
        JTextField txtPrecioCompra = new JTextField();

        JLabel lblPrecioVenta = new JLabel("Precio Venta:");
        lblPrecioVenta.setForeground(COLOR_TEXTO);
        JTextField txtPrecioVenta = new JTextField();

        JLabel lblFechaCaducidad = new JLabel("Fecha Caducidad (YYYY-MM-DD):");
        lblFechaCaducidad.setForeground(COLOR_TEXTO);
        JTextField txtFechaCaducidad = new JTextField();

        JLabel lblStock = new JLabel("Stock:");
        lblStock.setForeground(COLOR_TEXTO);
        JTextField txtStock = new JTextField();

        JButton btnAgregar = crearBoton("Agregar Producto");

        panelFormulario.add(lblNombre);
        panelFormulario.add(txtNombre);
        panelFormulario.add(lblNombreGenerico);
        panelFormulario.add(txtNombreGenerico);
        panelFormulario.add(lblPrecioCompra);
        panelFormulario.add(txtPrecioCompra);
        panelFormulario.add(lblPrecioVenta);
        panelFormulario.add(txtPrecioVenta);
        panelFormulario.add(lblFechaCaducidad);
        panelFormulario.add(txtFechaCaducidad);
        panelFormulario.add(lblStock);
        panelFormulario.add(txtStock);
        panelFormulario.add(new JLabel()); // espacio
        panelFormulario.add(btnAgregar);

        add(panelFormulario, BorderLayout.CENTER);

        // --- ACCIÓN DEL BOTÓN ---
        btnAgregar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String nombreGen = txtNombreGenerico.getText().trim();
            String precioCompraStr = txtPrecioCompra.getText().trim();
            String precioVentaStr = txtPrecioVenta.getText().trim();
            String fechaCaducidad = txtFechaCaducidad.getText().trim();
            String stockStr = txtStock.getText().trim();

            if (nombre.isEmpty() || precioCompraStr.isEmpty() || precioVentaStr.isEmpty()
                    || fechaCaducidad.isEmpty() || stockStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double precioCompra = Double.parseDouble(precioCompraStr);
                double precioVenta = Double.parseDouble(precioVentaStr);
                int stock = Integer.parseInt(stockStr);

                if (precioCompra < 0 || precioVenta < 0 || stock < 0) {
                    JOptionPane.showMessageDialog(this, "Precio y stock deben ser valores positivos.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // --- INSERTAR EN BASE DE DATOS USANDO ConexionDB ---
                Connection conn = ConexionDB.obtenerConexion();
                if (conn != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO Producto (Nombre, Nombre_Generico, Precio_Compra, Precio_Venta, Fecha_Caducidad, Stock) VALUES (?, ?, ?, ?, ?, ?)")) {
                        ps.setString(1, nombre);
                        ps.setString(2, nombreGen.isEmpty() ? null : nombreGen);
                        ps.setDouble(3, precioCompra);
                        ps.setDouble(4, precioVenta);
                        ps.setString(5, fechaCaducidad);
                        ps.setInt(6, stock);

                        ps.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Producto agregado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                        // Limpiar campos
                        txtNombre.setText("");
                        txtNombreGenerico.setText("");
                        txtPrecioCompra.setText("");
                        txtPrecioVenta.setText("");
                        txtFechaCaducidad.setText("");
                        txtStock.setText("");

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error al insertar producto:\n" + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio y Stock deben ser números válidos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // --- BOTÓN ESTILO EXTRA ---
    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(COLOR_BOTON);
        boton.setForeground(COLOR_TEXTO);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        boton.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { boton.setBackground(COLOR_HOVER); }
            @Override public void mouseExited(MouseEvent e) { boton.setBackground(COLOR_BOTON); }
        });
        return boton;
    }
}
