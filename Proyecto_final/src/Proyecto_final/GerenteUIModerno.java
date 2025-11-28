package Proyecto_final;

import javax.swing.*;
import java.awt.*;

public class GerenteUIModerno extends JFrame {

    private final Color COLOR_FONDO = new Color(225, 245, 254);
    private final Color COLOR_PANEL = Color.WHITE;
    private final Color COLOR_BOTON = Color.WHITE;
    private final Color COLOR_TEXTO_BOTON = Color.BLACK;
    private final Color COLOR_HOVER = new Color(200, 200, 200);
    private final Font FUENTE_GLOBAL = new Font("Segoe UI", Font.PLAIN, 16);

    private JPanel panelDerecho;

    // Constructor adaptado para rol, nombre y foto del usuario
    public GerenteUIModerno(String rol, String nombreEmpleado, byte[] fotoEmpleado, int idEmpleado) {
        setUndecorated(true);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // PANEL LATERAL
        JPanel panelMenu = new JPanel();
        panelMenu.setBackground(COLOR_PANEL);
        panelMenu.setPreferredSize(new Dimension(250, getHeight()));
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        // PANEL PERFIL
        JPanel panelPerfil = new JPanel();
        panelPerfil.setBackground(COLOR_PANEL);
        panelPerfil.setLayout(new BoxLayout(panelPerfil, BoxLayout.Y_AXIS));

        // Tamaño de la foto: máximo 150 px, o lo que permita el ancho del panel
        int anchoMaxPanel = 230;
        int tamañoFoto = Math.min(anchoMaxPanel, 150);

        // Imagen de perfil
        Image imgPerfil;
        if (fotoEmpleado != null) {
            try {
                imgPerfil = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(fotoEmpleado));
            } catch (Exception e) {
                ImageIcon iconDefault = new ImageIcon(getClass().getResource("/imagenes/person.png"));
                imgPerfil = iconDefault.getImage();
            }
        } else {
            ImageIcon iconDefault = new ImageIcon(getClass().getResource("/imagenes/person.png"));
            imgPerfil = iconDefault.getImage();
        }

        imgPerfil = imgPerfil.getScaledInstance(tamañoFoto, tamañoFoto, Image.SCALE_SMOOTH);
        JLabel lblImagenPerfil = new JLabel(new ImageIcon(imgPerfil));
        lblImagenPerfil.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nombre de usuario
        JLabel lblUsuario = new JLabel(nombreEmpleado, SwingConstants.CENTER);
        lblUsuario.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 18f));
        lblUsuario.setForeground(Color.BLACK);
        lblUsuario.setOpaque(true);
        lblUsuario.setBackground(Color.WHITE);
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblUsuario.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        panelPerfil.add(Box.createVerticalStrut(10));
        panelPerfil.add(lblImagenPerfil);
        panelPerfil.add(Box.createVerticalStrut(10));
        panelPerfil.add(lblUsuario);

        JSeparator separador = new JSeparator(SwingConstants.HORIZONTAL);
        separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        separador.setForeground(Color.BLACK);
        separador.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // PANEL BOTONES
        JPanel panelBotones = new JPanel();
        panelBotones.setBackground(COLOR_PANEL);
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        String[] opciones = {"Productos", "Vendedores", "Reportes", "Anuncios", "Cerrar sesión"};
        String[] iconos = {"/imagenes/pill.png", "/imagenes/person.png", "/imagenes/Report.png", "/imagenes/advice.png", "/imagenes/off.png"};

        panelMenu.add(panelPerfil);
        panelMenu.add(Box.createVerticalStrut(10));
        panelMenu.add(separador);
        panelMenu.add(panelBotones);

        add(panelMenu, BorderLayout.WEST);

        // PANEL DERECHO
        panelDerecho = new JPanel();
        panelDerecho.setLayout(new BorderLayout());
        panelDerecho.setBackground(COLOR_FONDO);
        panelDerecho.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel panelInicio = new JPanel();
        panelInicio.setLayout(new BoxLayout(panelInicio, BoxLayout.Y_AXIS));
        panelInicio.setBackground(COLOR_FONDO);

        ImageIcon iconFar = new ImageIcon(getClass().getResource("/imagenes/Far.png"));
        Image imgFar = iconFar.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
        JLabel lblImagenFar = new JLabel(new ImageIcon(imgFar));
        lblImagenFar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTexto = new JLabel("<html><div style='text-align:center;'>"
                + "Sistema de control de inventario y facturación<br>"
                + "Jalinas 2 sector Mayoreo</div></html>", SwingConstants.CENTER);
        lblTexto.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 18f));
        lblTexto.setForeground(Color.DARK_GRAY);
        lblTexto.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTexto.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        panelInicio.add(lblImagenFar);
        panelInicio.add(lblTexto);

        panelDerecho.add(panelInicio, BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.CENTER);

        // CREAR BOTONES DEL MENU
        for (int i = 0; i < opciones.length; i++) {
            String texto = opciones[i];
            final String textoFinal = texto;

            JButton boton = new JButton(texto) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (getModel().isArmed()) {
                        g.setColor(COLOR_HOVER.darker());
                    } else if (getModel().isRollover()) {
                        g.setColor(COLOR_HOVER);
                    } else {
                        g.setColor(getBackground());
                    }
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                    super.paintComponent(g);
                }

                @Override
                protected void paintBorder(Graphics g) { }

                @Override
                public boolean isContentAreaFilled() { return false; }
            };

            ImageIcon icon = new ImageIcon(getClass().getResource(iconos[i]));
            Image imgIcon = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
            boton.setIcon(new ImageIcon(imgIcon));
            boton.setHorizontalTextPosition(SwingConstants.RIGHT);
            boton.setIconTextGap(15);

            boton.setAlignmentX(Component.CENTER_ALIGNMENT);
            boton.setMaximumSize(new Dimension(230, 60));
            boton.setBackground(COLOR_BOTON);
            boton.setForeground(COLOR_TEXTO_BOTON);
            boton.setFocusPainted(false);
            boton.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 18f));
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Deshabilitar botones según rol
            if (rol.equals("VENDEDOR")) {
                if (textoFinal.equals("Vendedores") || textoFinal.equals("Anuncios")) {
                    boton.setEnabled(false);
                }
            }

            boton.addActionListener(e -> {
                if (textoFinal.equals("Cerrar sesión")) {
                    new Interfaz().setVisible(true);
                    dispose();
                    return;
                }

                panelDerecho.removeAll();

                switch (textoFinal) {
                    case "Productos":
                        panelDerecho.add(new PanelProductos(), BorderLayout.CENTER);
                        break;
                    case "Vendedores":
                        Vendedores vPanel = new Vendedores();
                        panelDerecho.add(vPanel, BorderLayout.CENTER);
                        break;
                    case "Reportes":
                        break;
                    case "Anuncios":
                        break;
                }

                panelDerecho.revalidate();
                panelDerecho.repaint();
            });

            panelBotones.add(Box.createVerticalStrut(15));
            panelBotones.add(boton);
        }

        setVisible(true);
    }
}
