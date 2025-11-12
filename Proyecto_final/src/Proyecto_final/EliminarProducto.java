package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EliminarProducto extends JPanel {

    private final Color COLOR_FONDO = new Color(35, 35, 35);
    private final Color COLOR_BOTON = new Color(55, 55, 55);
    private final Color COLOR_HOVER = new Color(75, 75, 75);
    private final Color COLOR_TEXTO = new Color(230, 230, 230);

    private JFrame frameActual;

    public EliminarProducto(JFrame frameActual) {
        this.frameActual = frameActual;

        setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel panelFormulario = new JPanel(new GridLayout(2, 2, 15, 15));
        panelFormulario.setBackground(COLOR_FONDO);

        JLabel lblId = new JLabel("ID del Producto a eliminar:");
        lblId.setForeground(COLOR_TEXTO);
        JTextField txtId = new JTextField();

        JButton btnEliminar = crearBoton("Eliminar Producto");

        panelFormulario.add(lblId);
        panelFormulario.add(txtId);
        panelFormulario.add(new JLabel()); // espacio
        panelFormulario.add(btnEliminar);

        add(panelFormulario, BorderLayout.CENTER);

        btnEliminar.addActionListener(e -> {
            String idStr = txtId.getText().trim();
            if (idStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el ID del producto.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int idProducto = Integer.parseInt(idStr);

                Connection conn = ConexionDB.obtenerConexion();
                if (conn != null) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Producto WHERE Id_Producto = ?")) {
                        ps.setInt(1, idProducto);
                        int afectadas = ps.executeUpdate();

                        if (afectadas > 0) {
                            JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            txtId.setText("");
                        } else {
                            JOptionPane.showMessageDialog(this, "No se encontró el producto con ese ID.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        }

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error al eliminar producto:\n" + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

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
