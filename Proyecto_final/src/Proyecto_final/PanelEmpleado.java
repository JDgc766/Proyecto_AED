package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PanelEmpleado extends JPanel {

    private Vendedores padre;
    private int idEmpleado;
    private Connection conn;

    public PanelEmpleado(Vendedores padre, int idEmpleado) {
        this.padre = padre;
        this.idEmpleado = idEmpleado;
        this.conn = ConexionDB.obtenerConexion();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(225, 245, 254));

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Empleado WHERE Id_Empleado=?");
            ps.setInt(1, idEmpleado);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JLabel lblNombre = new JLabel("Nombre: " + rs.getString("Nombre"));
                JLabel lblCorreo = new JLabel("Correo: " + rs.getString("Correo"));
                JLabel lblTelefono = new JLabel("Teléfono: " + rs.getString("Telefono"));
                JLabel lblDireccion = new JLabel("Dirección: " + rs.getString("Direccion"));
                JLabel lblUsuario = new JLabel("Usuario: " + rs.getString("Usuario"));
                JLabel lblRol = new JLabel("Rol: " + rs.getString("Rol"));

                lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblCorreo.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblTelefono.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblDireccion.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);

                add(Box.createVerticalStrut(20));
                add(lblNombre);
                add(lblCorreo);
                add(lblTelefono);
                add(lblDireccion);
                add(lblUsuario);
                add(lblRol);
                add(Box.createVerticalStrut(20));

                JButton btnEditar = new JButton("Editar");
                btnEditar.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnEditar.addActionListener(e -> {
                    new EditarEmpleado(this, idEmpleado);
                });

                JButton btnVolver = new JButton("Volver");
                btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnVolver.addActionListener(e -> padre.volverLista());

                add(btnEditar);
                add(Box.createVerticalStrut(10));
                add(btnVolver);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar empleado: " + e.getMessage());
        }
    }
}
