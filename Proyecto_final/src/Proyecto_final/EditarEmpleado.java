package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class EditarEmpleado extends JDialog {

    public EditarEmpleado(PanelEmpleado padrePanel, int idEmpleado) {
        super((Frame) null, "Editar Empleado", true);
        setLayout(new GridLayout(0, 2, 5, 5));
        setBackground(new Color(225, 245, 254));

        Connection conn = ConexionDB.obtenerConexion();
        PreparedStatement ps = null;
        ResultSet rs = null;

        JTextField txtNombre = new JTextField();
        JTextField txtCorreo = new JTextField();
        JTextField txtTelefono = new JTextField();
        JTextField txtDireccion = new JTextField();
        JTextField txtUsuario = new JTextField();

        try {
            ps = conn.prepareStatement("SELECT * FROM Empleado WHERE Id_Empleado=?");
            ps.setInt(1, idEmpleado);
            rs = ps.executeQuery();

            if (rs.next()) {
                txtNombre.setText(rs.getString("Nombre"));
                txtCorreo.setText(rs.getString("Correo"));
                txtTelefono.setText(rs.getString("Telefono"));
                txtDireccion.setText(rs.getString("Direccion"));
                txtUsuario.setText(rs.getString("Usuario"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar empleado: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
        }

        add(new JLabel("Nombre:")); add(txtNombre);
        add(new JLabel("Correo:")); add(txtCorreo);
        add(new JLabel("Teléfono:")); add(txtTelefono);
        add(new JLabel("Dirección:")); add(txtDireccion);
        add(new JLabel("Usuario:")); add(txtUsuario);

        // ============================
        //         BOTÓN GUARDAR
        // ============================
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> {

            PreparedStatement psUpdate = null;

            try {
                psUpdate = conn.prepareStatement(
                    "UPDATE Empleado SET Nombre=?, Correo=?, Telefono=?, Direccion=?, Usuario=? WHERE Id_Empleado=?"
                );

                psUpdate.setString(1, txtNombre.getText());
                psUpdate.setString(2, txtCorreo.getText());
                psUpdate.setString(3, txtTelefono.getText());
                psUpdate.setString(4, txtDireccion.getText());
                psUpdate.setString(5, txtUsuario.getText());
                psUpdate.setInt(6, idEmpleado);

                psUpdate.executeUpdate();

                JOptionPane.showMessageDialog(this, "Empleado actualizado.");
                dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());

            } finally {
                try { if (psUpdate != null) psUpdate.close(); } catch (SQLException ignored) {}
                try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
            }

        });

        add(btnGuardar);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
