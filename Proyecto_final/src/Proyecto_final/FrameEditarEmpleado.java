package Proyecto_final;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;

public class FrameEditarEmpleado extends JFrame {

    private int idEmpleado;
    private PanelDetalleEmpleado detallePanel;
    private Connection conn;

    private JTextField txtNombre, txtCorreo, txtTelefono, txtDireccion,
            txtUsuario, txtContrasenia, txtFechaContrato, txtFechaBaja, txtIdentificacion;
    private JLabel lblRol, lblFoto;
    private JCheckBox chkActivo;
    private byte[] fotoBytes;

    private final Color COLOR_FONDO = new Color(200, 255, 200); // verde pastel
    private final Font FUENTE_GLOBAL = new Font("Segoe UI", Font.PLAIN, 16);
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FrameEditarEmpleado(int idEmpleado, PanelDetalleEmpleado detallePanel, Connection conn) {
        this.idEmpleado = idEmpleado;
        this.detallePanel = detallePanel;
        this.conn = conn;

        setUndecorated(true); // quita barra de título
        setTitle("Editar Empleado");
        setSize(1000, 600); // ventana más pequeña
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(COLOR_FONDO);
        add(panel);

        // Título
        JLabel lblTitulo = new JLabel("Editar Empleado", SwingConstants.CENTER);
        lblTitulo.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 28f));
        lblTitulo.setForeground(new Color(0, 100, 0));
        lblTitulo.setBounds(0, 10, 1000, 40);
        panel.add(lblTitulo);

        cargarDatos(panel);
    }

    private void cargarDatos(JPanel panel) {
        String sql = "SELECT * FROM Empleado WHERE Id_Empleado=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    txtNombre = campo(rs.getString("Nombre"));
                    txtCorreo = campo(rs.getString("Correo"));
                    txtTelefono = campo(rs.getString("Telefono"));
                    txtDireccion = campo(rs.getString("Direccion"));
                    txtUsuario = campo(rs.getString("Usuario"));
                    txtContrasenia = campo(rs.getString("Contrasenia"));
                    txtFechaContrato = campo(rs.getString("Fecha_Contrato"));
                    txtFechaBaja = campo(rs.getString("Fecha_Baja"));
                    txtFechaBaja.setEditable(false);
                    txtIdentificacion = campo(rs.getString("Identificacion"));

                    // Previsualizar desde el inicio
                    txtDireccion.setCaretPosition(0);

                    lblRol = new JLabel(rs.getString("Rol"));
                    lblRol.setOpaque(true);
                    lblRol.setBackground(new Color(200, 220, 240));
                    lblRol.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                    lblRol.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 16f));

                    chkActivo = new JCheckBox();
                    chkActivo.setSelected("S".equals(rs.getString("Activo")));
                    chkActivo.setBackground(COLOR_FONDO);
                    if ("Gerente".equalsIgnoreCase(rs.getString("Rol"))) {
                        chkActivo.setEnabled(false);
                    }

                    lblFoto = new JLabel();
                    lblFoto.setHorizontalAlignment(SwingConstants.CENTER);
                    fotoBytes = rs.getBytes("Foto");
                    mostrarFoto(fotoBytes);

                    // Posiciones estáticas
                    int xLabel = 20, xCampo = 180, widthCampo = 500, altoCampo = 30;
                    int yStart = 70, yIncrement = 40;

                    JLabel[] etiquetas = new JLabel[11];
                    JComponent[] campos = {txtNombre, txtCorreo, txtTelefono, txtDireccion, txtUsuario,
                            txtContrasenia, lblRol, chkActivo, txtFechaContrato, txtFechaBaja, txtIdentificacion};
                    String[] nombres = {"Nombre", "Correo", "Teléfono", "Dirección", "Usuario",
                            "Contraseña", "Rol", "Activo", "Fecha Contrato", "Fecha Baja", "Identificación"};

                    for (int i = 0; i < nombres.length; i++) {
                        etiquetas[i] = new JLabel(nombres[i] + ":");
                        etiquetas[i].setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 16f));
                        etiquetas[i].setBounds(xLabel, yStart + i * yIncrement, 150, altoCampo);
                        panel.add(etiquetas[i]);

                        campos[i].setBounds(xCampo, yStart + i * yIncrement, widthCampo, altoCampo);
                        panel.add(campos[i]);
                    }

                    // Foto
                    lblFoto.setBounds(700, 70, 250, 250);
                    panel.add(lblFoto);

                    // Botón cambiar foto
                    JButton btnFoto = new JButton("Cambiar Foto");
                    btnFoto.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 16f));
                    btnFoto.setBackground(new Color(0, 150, 136));
                    btnFoto.setForeground(Color.WHITE);
                    btnFoto.setBorderPainted(false);
                    btnFoto.setFocusPainted(false);
                    btnFoto.setBounds(700, 330, 150, 35);
                    btnFoto.addActionListener(e -> seleccionarFoto());
                    panel.add(btnFoto);

                    // Botón Guardar
                    JButton btnGuardar = new JButton("Guardar");
                    btnGuardar.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 16f));
                    btnGuardar.setBackground(new Color(0, 150, 136));
                    btnGuardar.setForeground(Color.WHITE);
                    btnGuardar.setBorderPainted(false);
                    btnGuardar.setFocusPainted(false);
                    btnGuardar.setBounds(250, yStart + nombres.length * yIncrement + 20, 160, 40);
                    btnGuardar.addActionListener(e -> guardar());
                    panel.add(btnGuardar);

                    // Botón Cancelar
                    JButton btnCancelar = new JButton("Cancelar");
                    btnCancelar.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 16f));
                    btnCancelar.setBackground(new Color(200, 50, 50));
                    btnCancelar.setForeground(Color.WHITE);
                    btnCancelar.setBorderPainted(false);
                    btnCancelar.setFocusPainted(false);
                    btnCancelar.setBounds(450, yStart + nombres.length * yIncrement + 20, 120, 40);
                    btnCancelar.addActionListener(e -> dispose());
                    panel.add(btnCancelar);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JTextField campo(String texto) {
        JTextField f = new JTextField(texto);
        f.setFont(FUENTE_GLOBAL.deriveFont(16f));
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(150, 180, 210)));
        return f;
    }

    private void mostrarFoto(byte[] bytes) {
        try {
            if (bytes != null) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                lblFoto.setIcon(redondearImagen(img, 250));
            } else {
                lblFoto.setIcon(null);
            }
        } catch (IOException e) {
            lblFoto.setIcon(null);
        }
    }

    private Icon redondearImagen(BufferedImage bi, int size) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        int dim = Math.min(w, h);
        BufferedImage biRed = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = biRed.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        g2.drawImage(bi.getSubimage((w - dim) / 2, (h - dim) / 2, dim, dim)
                .getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
        g2.dispose();
        return new ImageIcon(biRed);
    }
    private void seleccionarFoto() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) { }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png"));
        int res = chooser.showOpenDialog(this); // ABRE EL NAVEGADOR DE ARCHIVOS

        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                File f = chooser.getSelectedFile();
                fotoBytes = new FileInputStream(f).readAllBytes(); // guarda bytes
                mostrarFoto(fotoBytes); // previsualiza en lblFoto
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al cargar la foto: " + e.getMessage());
            }
        }
    }


    private void guardar() {
        try {
            boolean activo = chkActivo.isSelected();
            LocalDate hoy = LocalDate.now();

            String fechaContrato = txtFechaContrato.getText();
            String fechaBaja = txtFechaBaja.getText();

            if (!activo) {
                fechaBaja = dtf.format(hoy);
            } else {
                fechaBaja = null;
            }

            String sql = "UPDATE Empleado SET Nombre=?, Correo=?, Telefono=?, Direccion=?, Usuario=?, Contrasenia=?," +
                    "Activo=?, Fecha_Contrato=?, Fecha_Baja=?, Identificacion=?, Foto=? WHERE Id_Empleado=?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, txtNombre.getText());
                ps.setString(2, txtCorreo.getText());
                ps.setString(3, txtTelefono.getText());
                ps.setString(4, txtDireccion.getText());
                ps.setString(5, txtUsuario.getText());
                ps.setString(6, txtContrasenia.getText());
                ps.setString(7, activo ? "S" : "N");
                ps.setString(8, fechaContrato);
                if (fechaBaja != null) ps.setString(9, fechaBaja);
                else ps.setNull(9, Types.VARCHAR);
                ps.setString(10, txtIdentificacion.getText());
                if (fotoBytes != null) ps.setBytes(11, fotoBytes);
                else ps.setNull(11, Types.BLOB);
                ps.setInt(12, idEmpleado);

                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Actualizado correctamente");
            detallePanel.recargar();
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error SQL al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
