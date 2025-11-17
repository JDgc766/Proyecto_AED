package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PanelContratarEmpleado extends JPanel {

    private JTextField txtNombre, txtCorreo, txtTelefono, txtDireccion, txtUsuario, txtContrasenia;
    private Connection conn;
    private Vendedores padre;

    public PanelContratarEmpleado(Connection conn, Vendedores padre){
        this.conn = conn;
        this.padre = padre;

        setLayout(new GridLayout(7,2,5,5));

        txtNombre = new JTextField();
        txtCorreo = new JTextField();
        txtTelefono = new JTextField();
        txtDireccion = new JTextField();
        txtUsuario = new JTextField();
        txtContrasenia = new JTextField();

        add(new JLabel("Nombre:")); add(txtNombre);
        add(new JLabel("Correo:")); add(txtCorreo);
        add(new JLabel("Teléfono:")); add(txtTelefono);
        add(new JLabel("Dirección:")); add(txtDireccion);
        add(new JLabel("Usuario:")); add(txtUsuario);
        add(new JLabel("Contraseña:")); add(txtContrasenia);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardar());
        add(btnGuardar);

        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
        add(btnVolver);
    }

    private void guardar(){
        if(txtNombre.getText().isEmpty() || txtCorreo.getText().isEmpty() || txtTelefono.getText().isEmpty()
                || txtDireccion.getText().isEmpty() || txtUsuario.getText().isEmpty() || txtContrasenia.getText().isEmpty()){
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios");
            return;
        }

        try{
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Empleado (Nombre, Correo, Telefono, Direccion, Usuario, Contrasenia, Rol, Activo, Fecha_Contrato) " +
                            "VALUES (?, ?, ?, ?, ?, ?, 'VENDEDOR', 'S', DATE('now'))");
            ps.setString(1, txtNombre.getText());
            ps.setString(2, txtCorreo.getText());
            ps.setString(3, txtTelefono.getText());
            ps.setString(4, txtDireccion.getText());
            ps.setString(5, txtUsuario.getText());
            ps.setString(6, txtContrasenia.getText());
            ps.executeUpdate();

            padre.refrescarLista();  // Usamos un método seguro para refrescar
            JOptionPane.showMessageDialog(this, "Empleado contratado correctamente");
            SwingUtilities.getWindowAncestor(this).dispose();
        } catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Error al guardar empleado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
