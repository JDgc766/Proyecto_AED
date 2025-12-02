package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class PanelAnuncios extends JPanel {

    private final Color COLOR_FONDO = new Color(225, 245, 254);
    private final Color COLOR_AVISO = Color.WHITE;
    private final Color COLOR_NO_LEIDO = new Color(255, 100, 100);
    private final Font FUENTE = new Font("Segoe UI", Font.PLAIN, 16);

    public PanelAnuncios() {
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);

        // Título (tu diseño exacto estilo vendedores)
        JLabel titulo = new JLabel("Anuncios", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(new Color(0, 100, 0));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(titulo, BorderLayout.NORTH);

        JPanel panelAvisos = new JPanel();
        panelAvisos.setLayout(new BoxLayout(panelAvisos, BoxLayout.Y_AXIS));
        panelAvisos.setBackground(COLOR_FONDO);

        // ============================================================
        //  SCROLL MODERNO IGUAL AL DE CLASE VENDEDORES
        // ============================================================
        JScrollPane scroll = new JScrollPane(panelAvisos);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        scroll.setBorder(BorderFactory.createEmptyBorder());

        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {

            private final Dimension d = new Dimension();

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return new JButton() {
                    @Override public Dimension getPreferredSize() { return d; }
                };
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return new JButton() {
                    @Override public Dimension getPreferredSize() { return d; }
                };
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color colorThumb = new Color(152, 251, 152); // verde pastel
                g2.setPaint(colorThumb);

                int arc = 10;
                g2.fillRoundRect(r.x, r.y, r.width, r.height, arc, arc);

                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(new Color(230, 230, 230));
                g.fillRect(r.x, r.y, r.width, r.height);
            }

            @Override
            protected void setThumbBounds(int x, int y, int width, int height) {
                super.setThumbBounds(x, y, width, height);
                scroll.repaint();
            }
        });

        add(scroll, BorderLayout.CENTER);

        // ============================================================
        //             LÓGICA ORIGINAL DE NOTIFICACIONES
        // ============================================================
        ArrayList<Notificacion> lista = obtenerNotificaciones();

        for (Notificacion n : lista) {

            JButton aviso = new JButton();
            aviso.setLayout(null);
            aviso.setPreferredSize(new Dimension(700, 120));
            aviso.setMaximumSize(new Dimension(700, 120));
            aviso.setMinimumSize(new Dimension(700, 120));
            aviso.setFocusPainted(false);
            aviso.setOpaque(true);
            aviso.setContentAreaFilled(true);
            aviso.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

            JLabel lblTipo = new JLabel(n.tipo);
            lblTipo.setFont(FUENTE.deriveFont(Font.BOLD, 16f));
            lblTipo.setBounds(10, 10, 200, 25);
            aviso.add(lblTipo);

            JTextArea lblMensaje = new JTextArea(n.mensaje);
            lblMensaje.setFont(FUENTE);
            lblMensaje.setLineWrap(true);
            lblMensaje.setWrapStyleWord(true);
            lblMensaje.setEditable(false);
            lblMensaje.setBounds(10, 40, 680, 50);
            aviso.add(lblMensaje);

            JLabel lblFecha = new JLabel(n.fecha);
            lblFecha.setFont(FUENTE.deriveFont(Font.ITALIC, 12f));
            lblFecha.setHorizontalAlignment(SwingConstants.RIGHT);
            lblFecha.setBounds(500, 10, 180, 25);
            aviso.add(lblFecha);

            actualizarColor(aviso, lblMensaje, n.leido);

            aviso.addActionListener(e -> {
                if (n.leido.equals("N")) {
                    String sqlUpdate = "UPDATE Notificacion SET Leido = 'S' WHERE Id_Notificacion = ?";
                    try (Connection conn = ConexionDB.obtenerConexion();
                         PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {

                        ps.setInt(1, n.id);
                        ps.executeUpdate();

                        n.leido = "S";
                        actualizarColor(aviso, lblMensaje, n.leido);

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error al marcar como leído", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            JPanel contenedor = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            contenedor.setBackground(COLOR_FONDO);
            contenedor.add(aviso);

            panelAvisos.add(contenedor);
            panelAvisos.add(Box.createVerticalStrut(15));
        }
    }

    private void actualizarColor(JButton btn, JTextArea txt, String leido) {
        Color color = leido.equals("N") ? COLOR_NO_LEIDO : COLOR_AVISO;
        btn.setBackground(color);
        txt.setBackground(color);
    }

    private ArrayList<Notificacion> obtenerNotificaciones() {
        ArrayList<Notificacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM Notificacion ORDER BY Fecha DESC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Notificacion(
                        rs.getInt("Id_Notificacion"),
                        rs.getString("Tipo"),
                        rs.getString("Mensaje"),
                        rs.getString("Fecha"),
                        rs.getString("Leido")
                ));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar notificaciones", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return lista;
    }

    private static class Notificacion {
        int id;
        String tipo;
        String mensaje;
        String fecha;
        String leido;

        public Notificacion(int id, String tipo, String mensaje, String fecha, String leido) {
            this.id = id;
            this.tipo = tipo;
            this.mensaje = mensaje;
            this.fecha = fecha;
            this.leido = leido;
        }
    }
}
