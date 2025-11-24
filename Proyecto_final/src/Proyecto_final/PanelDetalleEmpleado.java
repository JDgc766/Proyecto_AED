package Proyecto_final;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import javax.imageio.ImageIO;

public class PanelDetalleEmpleado extends JPanel {

    private int idEmpleado;
    private Connection conn;
    private Vendedores vendedoresPanel;

    // Campos editables
    private JTextField txtNombre, txtCorreo, txtTelefono, txtDireccion, txtUsuario, txtContrasenia, txtIdentificacion, txtFechaContrato, txtFechaBaja;
    private JComboBox<String> cbRol, cbActivo;
    private JLabel lblFoto;
    private byte[] fotoBytes; // Foto actual

    public PanelDetalleEmpleado(Vendedores vendedoresPanel, Connection conn, int idEmpleado) {
        this.vendedoresPanel = vendedoresPanel;
        this.conn = conn;
        this.idEmpleado = idEmpleado;

        setLayout(new BorderLayout());
        setBackground(new Color(225, 245, 254));

        cargarDetalles();
    }

    private void cargarDetalles() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM Empleado WHERE Id_Empleado = ?"
            );
            ps.setInt(1, idEmpleado);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Panel de campos
                JPanel panelCampos = new JPanel(new GridBagLayout());
                panelCampos.setBackground(new Color(225, 245, 254));
                panelCampos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.WEST;

                // Campos
                txtNombre = new JTextField(rs.getString("Nombre"));
                txtCorreo = new JTextField(rs.getString("Correo"));
                txtTelefono = new JTextField(rs.getString("Telefono"));
                txtDireccion = new JTextField(rs.getString("Direccion"));
                txtUsuario = new JTextField(rs.getString("Usuario"));
                txtContrasenia = new JTextField(rs.getString("Contrasenia"));
                txtIdentificacion = new JTextField(rs.getString("identificacion") != null ? rs.getString("identificacion") : "");
                txtFechaContrato = new JTextField(rs.getString("Fecha_Contrato"));
                txtFechaBaja = new JTextField(rs.getString("Fecha_Baja") != null ? rs.getString("Fecha_Baja") : "");

                cbRol = new JComboBox<>(new String[]{"GERENTE", "VENDEDOR"});
                cbRol.setSelectedItem(rs.getString("Rol"));

                cbActivo = new JComboBox<>(new String[]{"S", "N"});
                cbActivo.setSelectedItem(rs.getString("Activo"));

                // Foto
                lblFoto = new JLabel();
                lblFoto.setPreferredSize(new Dimension(150, 150));
                lblFoto.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                fotoBytes = rs.getBytes("Foto");
                mostrarFoto(fotoBytes);

                JButton btnCambiarFoto = new JButton("Cambiar Foto");
                btnCambiarFoto.addActionListener(e -> seleccionarFoto());

                // Labels y campos
                String[] labels = {"Nombre", "Correo", "Teléfono", "Dirección", "Usuario", "Contraseña",
                        "Rol", "Activo", "Fecha Contrato", "Fecha Baja", "Identificación"};
                JComponent[] fields = {txtNombre, txtCorreo, txtTelefono, txtDireccion, txtUsuario, txtContrasenia,
                        cbRol, cbActivo, txtFechaContrato, txtFechaBaja, txtIdentificacion};

                for (int i = 0; i < labels.length; i++) {
                    gbc.gridx = 0;
                    gbc.gridy = i;
                    panelCampos.add(new JLabel(labels[i] + ":"), gbc);
                    gbc.gridx = 1;
                    panelCampos.add(fields[i], gbc);
                }

                // Panel foto a la derecha
                JPanel panelFoto = new JPanel();
                panelFoto.setLayout(new BoxLayout(panelFoto, BoxLayout.Y_AXIS));
                panelFoto.setBackground(new Color(225, 245, 254));
                panelFoto.add(lblFoto);
                panelFoto.add(Box.createVerticalStrut(10));
                panelFoto.add(btnCambiarFoto);

                // Panel principal
                JPanel panelCentral = new JPanel(new BorderLayout());
                panelCentral.add(panelCampos, BorderLayout.CENTER);
                panelCentral.add(panelFoto, BorderLayout.EAST);

                // Botones
                JPanel panelBotones = new JPanel();
                JButton btnGuardar = new JButton("Guardar");
                btnGuardar.addActionListener(e -> guardarCambios());

                JButton btnVolver = new JButton("Volver");
                btnVolver.addActionListener(e -> vendedoresPanel.volverLista());

                panelBotones.add(btnGuardar);
                panelBotones.add(btnVolver);

                add(panelCentral, BorderLayout.CENTER);
                add(panelBotones, BorderLayout.SOUTH);
            } else {
                add(new JLabel("Empleado no encontrado"), BorderLayout.CENTER);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar detalles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarFoto(byte[] bytes) {
        try {
            if (bytes != null) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                ImageIcon icon = new ImageIcon(img.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
                lblFoto.setIcon(icon);
            } else {
                lblFoto.setIcon(null);
            }
        } catch (Exception e) {
            lblFoto.setIcon(null);
        }
    }

    private void seleccionarFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            try {
                FileInputStream fis = new FileInputStream(archivo);
                fotoBytes = fis.readAllBytes();
                fis.close();
                mostrarFoto(fotoBytes);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar la foto: " + ex.getMessage());
            }
        }
    }

    private void guardarCambios() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Empleado SET Nombre=?, Correo=?, Telefono=?, Direccion=?, Usuario=?, Contrasenia=?, Rol=?, Activo=?, Fecha_Contrato=?, Fecha_Baja=?, identificacion=?, Foto=? WHERE Id_Empleado=?"
            );
            ps.setString(1, txtNombre.getText());
            ps.setString(2, txtCorreo.getText());
            ps.setString(3, txtTelefono.getText());
            ps.setString(4, txtDireccion.getText());
            ps.setString(5, txtUsuario.getText());
            ps.setString(6, txtContrasenia.getText());
            ps.setString(7, (String) cbRol.getSelectedItem());
            ps.setString(8, (String) cbActivo.getSelectedItem());
            ps.setString(9, txtFechaContrato.getText());
            ps.setString(10, txtFechaBaja.getText().isEmpty() ? null : txtFechaBaja.getText());
            ps.setString(11, txtIdentificacion.getText());
            if (fotoBytes != null) {
                ps.setBytes(12, fotoBytes);
            } else {
                ps.setNull(12, Types.BLOB);
            }
            ps.setInt(13, idEmpleado);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Datos guardados correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
