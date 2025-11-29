package Proyecto_final;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.*;
import javax.imageio.ImageIO;
import java.awt.geom.Ellipse2D;

public class PanelDetalleEmpleado extends JPanel {

    private int idEmpleado;
    private Connection conn;
    private JLabel lblFoto;
    private byte[] fotoBytes;

    private Font fuente = new Font("Segoe UI", Font.BOLD, 22);
    private Color colorFondo = new Color(225, 245, 254);

    public PanelDetalleEmpleado(Connection conn, int idEmpleado) {
        this.conn = conn;
        this.idEmpleado = idEmpleado;

        setLayout(new BorderLayout());
        setBackground(colorFondo);

        cargarDetalles();
    }

    private void cargarDetalles() {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Empleado WHERE Id_Empleado = ?")) {

            ps.setInt(1, idEmpleado);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    JPanel panelPrincipal = new JPanel();
                    panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
                    panelPrincipal.setBackground(colorFondo);
                    panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                    // ================= TITULO =================
                    JLabel lblTitulo = new JLabel("Detalles del empleado", SwingConstants.CENTER);
                    lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
                    lblTitulo.setForeground(new Color(34, 139, 34)); // verde
                    lblTitulo.setAlignmentX(CENTER_ALIGNMENT);
                    lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
                    panelPrincipal.add(lblTitulo);

                    // ================= FOTO + NOMBRE =================
                    lblFoto = new JLabel();
                    lblFoto.setPreferredSize(new Dimension(250, 250));
                    fotoBytes = rs.getBytes("Foto");
                    if (fotoBytes != null) {
                        mostrarFotoCircular(fotoBytes);
                    } else {
                        try {
                            BufferedImage fallback = ImageIO.read(getClass().getResource("/imagenes/person.png"));
                            lblFoto.setIcon(new ImageIcon(hacerCircular(fallback, 250)));
                        } catch (Exception ex) {
                            lblFoto.setIcon(null);
                        }
                    }

                    JLabel lblNombre = new JLabel(rs.getString("Nombre"));
                    lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 28));
                    lblNombre.setForeground(new Color(30, 30, 30));

                    JPanel panelFotoNombre = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
                    panelFotoNombre.setBackground(colorFondo);
                    panelFotoNombre.add(lblFoto);
                    panelFotoNombre.add(lblNombre);
                    panelPrincipal.add(panelFotoNombre);
                    panelPrincipal.add(Box.createVerticalStrut(20));

                    // ================= CAMPOS =================
                    JPanel panelCampos = new JPanel(new GridBagLayout());
                    panelCampos.setBackground(colorFondo);
                    panelCampos.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(12, 10, 12, 10);
                    gbc.anchor = GridBagConstraints.WEST;

                    String[] labels = {
                            "Correo", "Teléfono", "Dirección",
                            "Usuario", "Contraseña", "Rol",
                            "Activo", "Fecha Contrato",
                            "Fecha Baja", "Identificación"
                    };

                    String[] datos = {
                            rs.getString("Correo"),
                            rs.getString("Telefono"),
                            rs.getString("Direccion"),
                            rs.getString("Usuario"),
                            rs.getString("Contrasenia"),
                            rs.getString("Rol"),
                            rs.getString("Activo"),
                            rs.getString("Fecha_Contrato"),
                            rs.getString("Fecha_Baja") != null ? rs.getString("Fecha_Baja") : "Sin baja",
                            rs.getString("identificacion") != null ? rs.getString("identificacion") : "No asignada"
                    };

                    for (int i = 0; i < labels.length; i++) {
                        gbc.gridx = 0;
                        gbc.gridy = i;

                        JLabel lblTituloCampo = new JLabel(labels[i] + ":");
                        lblTituloCampo.setFont(fuente);
                        lblTituloCampo.setForeground(Color.DARK_GRAY);
                        panelCampos.add(lblTituloCampo, gbc);

                        gbc.gridx = 1;
                        JLabel lblDato = new JLabel(datos[i]);
                        lblDato.setFont(fuente);
                        lblDato.setForeground(Color.BLACK);
                        panelCampos.add(lblDato, gbc);
                    }

                    panelPrincipal.add(panelCampos);

                    // ================= BOTON =================
                    JButton btnEditar = new JButton("Editar campos");
                    btnEditar.setFont(fuente);
                    btnEditar.setBackground(Color.BLACK);
                    btnEditar.setForeground(Color.WHITE);
                    btnEditar.setFocusPainted(false);
                    btnEditar.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                    btnEditar.setAlignmentX(Component.RIGHT_ALIGNMENT);

                    btnEditar.addActionListener(e -> new FrameEditarEmpleado(idEmpleado, this, conn).setVisible(true));

                    panelPrincipal.add(Box.createVerticalStrut(20));
                    panelPrincipal.add(btnEditar);

                    // ================= SCROLL =================
                    JScrollPane scroll = new JScrollPane(panelPrincipal);
                    scroll.setBorder(null);

                    // Aplicar scroll UI personalizado
                    scroll.getVerticalScrollBar().setUI(crearScrollUI());
                    scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, Integer.MAX_VALUE));
                    scroll.getHorizontalScrollBar().setUI(crearScrollUI());
                    scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 8));

                    add(scroll, BorderLayout.CENTER);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarFotoCircular(byte[] bytes) {
        try {
            if (bytes != null) {
                BufferedImage original = ImageIO.read(new ByteArrayInputStream(bytes));
                BufferedImage circular = hacerCircular(original, 250);
                lblFoto.setIcon(new ImageIcon(circular));
            }
        } catch (Exception e) {
            lblFoto.setIcon(null);
        }
    }

    private BufferedImage hacerCircular(BufferedImage imagen, int diametro) {
        BufferedImage mask = new BufferedImage(diametro, diametro, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = mask.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, diametro, diametro));
        g2.drawImage(imagen, 0, 0, diametro, diametro, null);
        g2.dispose();
        return mask;
    }

    public void recargar() {
        removeAll();
        cargarDetalles();
        revalidate();
        repaint();
    }

    // ================= SCROLL UI PERSONALIZADO =================
    private BasicScrollBarUI crearScrollUI() {
        return new BasicScrollBarUI() {
            private final int grosor = 6;

            @Override
            protected JButton createDecreaseButton(int orientation) { return crearZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return crearZeroButton(); }

            private JButton crearZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0,0));
                jbutton.setMinimumSize(new Dimension(0,0));
                jbutton.setMaximumSize(new Dimension(0,0));
                return jbutton;
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(colorFondo);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new Color(100, 149, 237, 180));
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, grosor*2, grosor*2);
                g2.dispose();
            }

            @Override
            protected Dimension getMinimumThumbSize() {
                return new Dimension(grosor, grosor);
            }
        };
    }
}
