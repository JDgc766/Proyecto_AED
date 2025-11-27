package Proyecto_final;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import javax.imageio.ImageIO;

public class FrameEditarEmpleado extends JFrame {

    private int idEmpleado;
    private PanelDetalleEmpleado detallePanel;
    private Connection conn;  // <-- conexión compartida
    private JTextField txtNombre, txtCorreo, txtTelefono, txtDireccion,
            txtUsuario, txtContrasenia, txtActivo,
            txtFechaContrato, txtFechaBaja, txtIdentificacion;
    private JLabel lblRol, lblFoto;
    private byte[] fotoBytes;

    public FrameEditarEmpleado(int idEmpleado, PanelDetalleEmpleado detallePanel, Connection conn) {
        this.idEmpleado = idEmpleado;
        this.detallePanel = detallePanel;
        this.conn = conn;  // <-- usamos la conexión del panel

        setTitle("Editar Empleado");
        setSize(550,650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(225,245,254));

        cargarDatos();
    }

    private void cargarDatos() {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Empleado WHERE Id_Empleado=?")) {

            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    JPanel panel = new JPanel(new GridBagLayout());
                    panel.setBackground(new Color(225,245,254));
                    panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(6,6,6,6);
                    gbc.fill = GridBagConstraints.HORIZONTAL;

                    txtNombre = campo(rs.getString("Nombre"));
                    txtCorreo = campo(rs.getString("Correo"));
                    txtTelefono = campo(rs.getString("Telefono"));
                    txtDireccion = campo(rs.getString("Direccion"));
                    txtUsuario = campo(rs.getString("Usuario"));
                    txtContrasenia = campo(rs.getString("Contrasenia"));
                    txtActivo = campo(rs.getString("Activo"));
                    txtFechaContrato = campo(rs.getString("Fecha_Contrato"));
                    txtFechaBaja = campo(rs.getString("Fecha_Baja"));
                    txtIdentificacion = campo(rs.getString("Identificacion"));

                    lblRol = new JLabel(rs.getString("Rol"));
                    lblRol.setOpaque(true);
                    lblRol.setBackground(new Color(200,220,240));
                    lblRol.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

                    lblFoto = new JLabel();
                    lblFoto.setPreferredSize(new Dimension(150,150));
                    fotoBytes = rs.getBytes("Foto");
                    mostrarFoto(fotoBytes);

                    String[] labels = {"Nombre","Correo","Teléfono","Dirección","Usuario","Contraseña",
                            "Rol","Activo","Fecha Contrato","Fecha Baja","Identificación"};
                    JComponent[] campos = {txtNombre, txtCorreo, txtTelefono, txtDireccion, txtUsuario,
                            txtContrasenia, lblRol, txtActivo, txtFechaContrato, txtFechaBaja, txtIdentificacion};

                    for (int i=0;i<labels.length;i++){
                        gbc.gridx=0; gbc.gridy=i;
                        panel.add(new JLabel(labels[i]+":"), gbc);
                        gbc.gridx=1; panel.add(campos[i], gbc);
                    }

                    JButton btnFoto = new JButton("Cambiar Foto");
                    btnFoto.addActionListener(e -> seleccionarFoto());

                    JButton btnGuardar = new JButton("Guardar cambios");
                    btnGuardar.addActionListener(e -> guardar());

                    JPanel panelFoto = new JPanel();
                    panelFoto.setBackground(new Color(225,245,254));
                    panelFoto.add(lblFoto);
                    panelFoto.add(btnFoto);

                    JPanel panelBotones = new JPanel();
                    panelBotones.setBackground(new Color(225,245,254));
                    panelBotones.add(btnGuardar);

                    add(panel, BorderLayout.CENTER);
                    add(panelFoto, BorderLayout.EAST);
                    add(panelBotones, BorderLayout.SOUTH);
                }
            }

        } catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private JTextField campo(String texto){
        JTextField f = new JTextField(texto);
        f.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(150,180,210)));
        f.setBackground(new Color(225,245,254));
        return f;
    }

    private void mostrarFoto(byte[] bytes){
        try{
            if (bytes!=null){
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                lblFoto.setIcon(new ImageIcon(img.getScaledInstance(150,150,Image.SCALE_SMOOTH)));
            }
        }catch(Exception e){ lblFoto.setIcon(null); }
    }

    private void seleccionarFoto(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes","jpg","jpeg","png"));
        int res = chooser.showOpenDialog(this);
        if (res==JFileChooser.APPROVE_OPTION){
            try{
                File f = chooser.getSelectedFile();
                FileInputStream fis = new FileInputStream(f);
                fotoBytes = fis.readAllBytes();
                fis.close();
                mostrarFoto(fotoBytes);
            }catch(Exception e){ JOptionPane.showMessageDialog(this,"Error: "+e.getMessage()); }
        }
    }

    private void guardar(){
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Empleado SET Nombre=?,Correo=?,Telefono=?,Direccion=?,Usuario=?,Contrasenia=?," +
                        "Activo=?,Fecha_Contrato=?,Fecha_Baja=?,Identificacion=?,Foto=? WHERE Id_Empleado=?")) {

            ps.setString(1, txtNombre.getText());
            ps.setString(2, txtCorreo.getText());
            ps.setString(3, txtTelefono.getText());
            ps.setString(4, txtDireccion.getText());
            ps.setString(5, txtUsuario.getText());
            ps.setString(6, txtContrasenia.getText());
            ps.setString(7, txtActivo.getText());
            ps.setString(8, txtFechaContrato.getText());
            ps.setString(9, txtFechaBaja.getText().isEmpty()?null:txtFechaBaja.getText());
            ps.setString(10, txtIdentificacion.getText());
            if (fotoBytes!=null) ps.setBytes(11,fotoBytes); else ps.setNull(11,Types.BLOB);
            ps.setInt(12,idEmpleado);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,"Actualizado correctamente");
            detallePanel.recargar(); // recarga panel
            dispose();

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Error al guardar: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
