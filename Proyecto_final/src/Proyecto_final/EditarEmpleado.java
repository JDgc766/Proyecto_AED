package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EditarEmpleado extends JDialog {

    public EditarEmpleado(PanelEmpleado padrePanel, int idEmpleado) {
        super((Frame) null, "Editar Empleado", true);
        setLayout(new GridLayout(0, 2, 5, 5));
        setBackground(new Color(225, 245, 254));

        Connection conn = ConexionDB.obtenerConexion();

        JTextField txtNombre = new JTextField();
        JTextField txtCorreo = new JTextField();
        JTextField txtTelefono = new JTextField();
        JTextField txtDireccion = new JTextField();
        JTextField txtUsuario = new JTextField();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Empleado WHERE Id_Empleado=?");
            ps.setInt(1, idEmpleado);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txtNombre.setText(rs.getString("Nombre"));
                txtCorreo.setText(rs.getString("Correo"));
                txtTelefono.setText(rs.getString("Telefono"));
                txtDireccion.setText(rs.getString("Direccion"));
                txtUsuario.setText(rs.getString("Usuario"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar empleado: " + e.getMessage());
        }

        add(new JLabel("Nombre:")); add(txtNombre);
        add(new JLabel("Correo:")); add(txtCorreo);
        add(new JLabel("Teléfono:")); add(txtTelefono);
        add(new JLabel("Dirección:")); add(txtDireccion);
        add(new JLabel("Usuario:")); add(txtUsuario);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> {
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Empleado SET Nombre=?, Correo=?, Telefono=?, Direccion=?, Usuario=? WHERE Id_Empleado=?"
                );
                ps.setString(1, txtNombre.getText());
                ps.setString(2, txtCorreo.getText());
                ps.setString(3, txtTelefono.getText());
                ps.setString(4, txtDireccion.getText());
                ps.setString(5, txtUsuario.getText());
                ps.setInt(6, idEmpleado);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Empleado actualizado.");
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            }
        });

        add(btnGuardar);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
