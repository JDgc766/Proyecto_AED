package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class PanelAnuncios extends JPanel {

    private final Color COLOR_FONDO = new Color(225, 245, 254);
    private final Color COLOR_AVISO = Color.WHITE;
    private final Color COLOR_NO_LEIDO = new Color(255, 230, 230);
    private final Font FUENTE = new Font("Segoe UI", Font.PLAIN, 16);

    public PanelAnuncios() {
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Anuncios", SwingConstants.CENTER);
        titulo.setFont(FUENTE.deriveFont(Font.BOLD, 22f));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(titulo, BorderLayout.NORTH);

        JPanel panelAvisos = new JPanel();
        panelAvisos.setLayout(new BoxLayout(panelAvisos, BoxLayout.Y_AXIS));
        panelAvisos.setBackground(COLOR_FONDO);

        JScrollPane scroll = new JScrollPane(panelAvisos);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        ArrayList<Notificacion> lista = obtenerNotificaciones();

        for (Notificacion n : lista) {
            JPanel aviso = new JPanel();
            aviso.setLayout(null); // layout nulo
            aviso.setBackground(n.leido.equals("N") ? COLOR_NO_LEIDO : COLOR_AVISO);
            aviso.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            aviso.setPreferredSize(new Dimension(700, 120));
            aviso.setMaximumSize(new Dimension(700, 120));
            aviso.setMinimumSize(new Dimension(700, 120));

            // Tipo
            JLabel lblTipo = new JLabel(n.tipo);
            lblTipo.setFont(FUENTE.deriveFont(Font.BOLD, 16f));
            lblTipo.setBounds(10, 10, 200, 25);
            aviso.add(lblTipo);

            // Mensaje
            JTextArea lblMensaje = new JTextArea(n.mensaje);
            lblMensaje.setFont(FUENTE);
            lblMensaje.setLineWrap(true);
            lblMensaje.setWrapStyleWord(true);
            lblMensaje.setEditable(false);
            lblMensaje.setBackground(aviso.getBackground());
            lblMensaje.setBounds(10, 40, 680, 50);
            aviso.add(lblMensaje);

            // Fecha
            JLabel lblFecha = new JLabel(n.fecha);
            lblFecha.setFont(FUENTE.deriveFont(Font.ITALIC, 12f));
            lblFecha.setHorizontalAlignment(SwingConstants.RIGHT);
            lblFecha.setBounds(500, 10, 180, 25);
            aviso.add(lblFecha);

            panelAvisos.add(aviso);
            panelAvisos.add(Box.createVerticalStrut(15));
        }
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
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar notificaciones", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return lista;
    }

    // Clase interna para representar cada aviso
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
