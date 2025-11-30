package Proyecto_final;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.imageio.ImageIO;

public class PanelContratarEmpleado extends JPanel {

    private JTextField txtNombre, txtCorreo, txtTelefono, txtDireccion, txtUsuario, txtContrasenia, txtIdentificacion;
    private JLabel lblFoto;
    private Connection conn;
    private Vendedores padre;
    private Color colorFondo = new Color(225, 245, 254);
    private File archivoFoto = null;
    private byte[] nuevaFoto = null;
    private final Font FUENTE_GLOBAL = new Font("Segoe UI", Font.PLAIN, 16);

    public PanelContratarEmpleado(Connection conn, Vendedores padre){
        this.conn = conn;
        this.padre = padre;

        setBackground(colorFondo);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel("Contratación", SwingConstants.CENTER);
        lblTitulo.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 28f));
        lblTitulo.setForeground(new Color(0, 100, 0));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lblTitulo);
        add(Box.createVerticalStrut(20));

        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBackground(colorFondo);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtNombre = crearTextField();
        txtCorreo = crearTextField();
        txtTelefono = crearTextField();
        txtDireccion = crearTextField();
        txtUsuario = crearTextField();
        txtContrasenia = crearTextField();
        txtIdentificacion = crearTextField();

        JLabel[] labels = {
                crearLabel("Nombre:"),
                crearLabel("Correo:"),
                crearLabel("Teléfono:"),
                crearLabel("Dirección:"),
                crearLabel("Usuario:"),
                crearLabel("Contraseña:"),
                crearLabel("Identificación:")
        };

        JTextField[] fields = {
                txtNombre, txtCorreo, txtTelefono, txtDireccion,
                txtUsuario, txtContrasenia, txtIdentificacion
        };

        for(int i = 0; i < labels.length; i++){
            gbc.gridx = 0; 
            gbc.gridy = i;
            gbc.insets = new Insets(12, 5, 12, 5);
            panelCampos.add(labels[i], gbc);

            gbc.gridx = 1;
            panelCampos.add(fields[i], gbc);
        }

        ImageIcon iconPerfil = new ImageIcon(getClass().getResource("/imagenes/person.png"));
        lblFoto = new JLabel();
        lblFoto.setPreferredSize(new Dimension(350, 350));
        lblFoto.setMaximumSize(new Dimension(350, 350));
        lblFoto.setMinimumSize(new Dimension(350, 350));
        lblFoto.setIcon(redondearImagen(iconPerfil, 350));
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = labels.length;
        gbc.insets = new Insets(0, 50, 0, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panelCampos.add(lblFoto, gbc);

        JButton btnSeleccionarFoto = new JButton("Seleccionar foto");
        btnSeleccionarFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSeleccionarFoto.setFocusPainted(false);
        btnSeleccionarFoto.setBorderPainted(false);
        btnSeleccionarFoto.setBackground(new Color(0, 150, 136));
        btnSeleccionarFoto.setForeground(Color.WHITE);
        btnSeleccionarFoto.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 16f));
        btnSeleccionarFoto.addActionListener(e -> seleccionarFoto());
        gbc.gridy = labels.length;
        gbc.gridx = 2;
        gbc.gridheight = 1;
        panelCampos.add(Box.createVerticalStrut(10), gbc);
        panelCampos.add(btnSeleccionarFoto, gbc);

        add(panelCampos);
        add(Box.createVerticalStrut(20));

        JPanel panelBotones = new JPanel();
        panelBotones.setBackground(colorFondo);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 16f));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.addActionListener(e -> guardar());

        JButton btnVolver = new JButton("Volver");
        btnVolver.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 16f));
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.addActionListener(e -> padre.volverLista());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnVolver);

        add(panelBotones);
    }

    private JTextField crearTextField(){
        JTextField tf = new JTextField();
        tf.setFont(FUENTE_GLOBAL.deriveFont(Font.PLAIN, 16f));
        tf.setPreferredSize(new Dimension(250, 28));
        return tf;
    }

    private JLabel crearLabel(String texto){
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 16f));
        return lbl;
    }

    private Icon redondearImagen(BufferedImage bi, int size){
        Image img = bi.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        BufferedImage biRedondeada = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = biRedondeada.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return new ImageIcon(biRedondeada);
    }

    private Icon redondearImagen(ImageIcon icon, int size){
        Image img = icon.getImage();
        BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return redondearImagen(bi, size);
    }

    private void seleccionarFoto(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {}
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "bmp", "gif"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            archivoFoto = chooser.getSelectedFile();
            try {
                BufferedImage imgOriginal = ImageIO.read(archivoFoto);
                int maxDim = 800;
                int newW = imgOriginal.getWidth();
                int newH = imgOriginal.getHeight();
                if (newW > maxDim || newH > maxDim) {
                    double scale = Math.min((double) maxDim / newW, (double) maxDim / newH);
                    newW = (int)(newW * scale);
                    newH = (int)(newH * scale);
                }
                BufferedImage imgEscalada = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = imgEscalada.createGraphics();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, newW, newH);
                g2.drawImage(imgOriginal.getScaledInstance(newW, newH, Image.SCALE_SMOOTH), 0, 0, null);
                g2.dispose();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(imgEscalada, "jpg", baos);
                nuevaFoto = baos.toByteArray();
                lblFoto.setIcon(redondearImagen(imgEscalada, 350));
                lblFoto.revalidate();
                lblFoto.repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen.");
                ex.printStackTrace();
            }
        }
    }

    private void guardar() {
        if(txtNombre.getText().isEmpty() || txtCorreo.getText().isEmpty() || txtTelefono.getText().isEmpty()
                || txtDireccion.getText().isEmpty() || txtUsuario.getText().isEmpty() || txtContrasenia.getText().isEmpty()
                || txtIdentificacion.getText().isEmpty()){
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios (excepto foto)");
            return;
        }
        try{
            PreparedStatement ps;
            if(nuevaFoto != null){
                ps = conn.prepareStatement(
                        "INSERT INTO Empleado (Nombre, Correo, Telefono, Direccion, Usuario, Contrasenia, Identificacion, Foto, Rol, Activo, Fecha_Contrato) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'VENDEDOR', 'S', DATE('now'))");
                ps.setString(1, txtNombre.getText());
                ps.setString(2, txtCorreo.getText());
                ps.setString(3, txtTelefono.getText());
                ps.setString(4, txtDireccion.getText());
                ps.setString(5, txtUsuario.getText());
                ps.setString(6, txtContrasenia.getText());
                ps.setString(7, txtIdentificacion.getText());
                ps.setBytes(8, nuevaFoto);
            } else {
                ps = conn.prepareStatement(
                        "INSERT INTO Empleado (Nombre, Correo, Telefono, Direccion, Usuario, Contrasenia, Identificacion, Rol, Activo, Fecha_Contrato) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, 'VENDEDOR', 'S', DATE('now'))");
                ps.setString(1, txtNombre.getText());
                ps.setString(2, txtCorreo.getText());
                ps.setString(3, txtTelefono.getText());
                ps.setString(4, txtDireccion.getText());
                ps.setString(5, txtUsuario.getText());
                ps.setString(6, txtContrasenia.getText());
                ps.setString(7, txtIdentificacion.getText());
            }
            ps.executeUpdate();

            // -------------------- NUEVO --------------------
            // Agregar notificación de contratación
            String mensaje = "Se ha contratado al empleado " + txtNombre.getText();
            NotificacionManager.agregarNotificacion("CONTRATACION", mensaje);
            // ------------------------------------------------

            padre.refrescarLista();
            JOptionPane.showMessageDialog(this, "Empleado contratado correctamente");
            padre.volverLista();
        } catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Error al guardar empleado: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
