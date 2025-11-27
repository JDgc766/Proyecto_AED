package Proyecto_final;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

public class Vendedores extends JPanel {

    private JPanel panelLista;
    private JPanel panelDetalle;
    private JScrollPane scrollLista;
    //Skibidi
    private Connection conn;

    private Color colorFondo = new Color(225, 245, 254);
    private Color colorEmpleado = new Color(220, 255, 220);
    private Color colorContratar = Color.WHITE;

    public Vendedores() {
        setLayout(new CardLayout());

        conn = ConexionDB.obtenerConexion();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "No se pudo conectar a la base de datos.");
            return;
        }

        panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setBackground(colorFondo);

        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBackground(colorFondo);
        contenedor.add(Box.createVerticalGlue());

        JLabel lblTitulo = new JLabel("Gestión de Vendedores", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 100, 0));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedor.add(lblTitulo);
        contenedor.add(Box.createVerticalStrut(20));

        JButton btnContratar = crearBotonRedondeado("Contratar Empleado", colorContratar, new Dimension(300,50));
        btnContratar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnContratar.addActionListener(e -> {
            CardLayout cl = (CardLayout) getLayout();
            cl.show(this, "contratar");
        });

        contenedor.add(btnContratar);
        contenedor.add(Box.createVerticalStrut(20));
        contenedor.add(Box.createVerticalGlue());

        panelLista.add(contenedor);

        scrollLista = new JScrollPane(panelLista);
        scrollLista.setBorder(null);
        scrollLista.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLista.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollLista.getVerticalScrollBar().setUnitIncrement(16);

        scrollLista.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
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
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
                g2.dispose();
            }
        });

        scrollLista.getVerticalScrollBar().setPreferredSize(new Dimension(10, Integer.MAX_VALUE));

        panelDetalle = new JPanel();
        panelDetalle.setLayout(new BorderLayout());
        panelDetalle.setBackground(colorFondo);

        refrescarLista(); // Carga inicial

        add(scrollLista, "lista");
        add(panelDetalle, "detalle");
        
        JPanel panelContratar = new PanelContratarEmpleado(conn, this);
        panelContratar.setBackground(colorFondo);
        add(panelContratar, "contratar");

    }

    private JButton crearBotonRedondeado(String texto, Color fondo, Dimension tamaño) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g2);
                g2.dispose();
            }
            @Override
            public void setBorder(Border border) {}
        };
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setPreferredSize(tamaño);
        btn.setMaximumSize(tamaño);
        btn.setMinimumSize(tamaño);
        return btn;
    }

    public void refrescarLista() {
        panelLista.removeAll();
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBackground(colorFondo);
        contenedor.add(Box.createVerticalGlue());

        JLabel lblTitulo = new JLabel("Gestión de Vendedores", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(0, 100, 0));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenedor.add(lblTitulo);
        contenedor.add(Box.createVerticalStrut(20));

        JButton btnContratar = crearBotonRedondeado("Contratar Empleado", colorContratar, new Dimension(300,50));
        btnContratar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnContratar.addActionListener(e -> {
            CardLayout cl = (CardLayout) getLayout();
            cl.show(this, "contratar");
        });
        contenedor.add(btnContratar);
        contenedor.add(Box.createVerticalStrut(20));

        JPanel empleadosContenedor = new JPanel();
        empleadosContenedor.setLayout(new BoxLayout(empleadosContenedor, BoxLayout.Y_AXIS));
        empleadosContenedor.setBackground(colorFondo);

        int fotoSize = 100;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Id_Empleado, Nombre, Rol, Foto FROM Empleado")) {

            while (rs.next()) {
                int id = rs.getInt("Id_Empleado");
                String nombre = rs.getString("Nombre");
                String rol = rs.getString("Rol");
                byte[] fotoBytes = rs.getBytes("Foto");

                ImageIcon icon;
                if (fotoBytes != null) {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(fotoBytes));
                    BufferedImage rounded = new BufferedImage(fotoSize, fotoSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = rounded.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0,0,0,0));
                    g2.fillRoundRect(0,0,fotoSize,fotoSize,15,15);
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0,0,fotoSize,fotoSize,15,15));
                    g2.drawImage(img.getScaledInstance(fotoSize,fotoSize,Image.SCALE_SMOOTH),0,0,null);
                    g2.dispose();
                    icon = new ImageIcon(rounded);
                } else {
                    java.net.URL imgURL = GerenteUIModerno.class.getResource("/imagenes/person.png");
                    if (imgURL != null) {
                        icon = new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(fotoSize,fotoSize,Image.SCALE_SMOOTH));
                    } else {
                        icon = new ImageIcon();
                    }
                }

                JPanel tarjeta = new JPanel(new BorderLayout());
                tarjeta.setPreferredSize(new Dimension(620, 130));
                tarjeta.setMaximumSize(new Dimension(620, 130));
                tarjeta.setBackground(colorEmpleado);
                tarjeta.setBorder(new EmptyBorder(10,10,10,10));

                JLabel lblFoto = new JLabel(icon);
                lblFoto.setPreferredSize(new Dimension(fotoSize, fotoSize));
                tarjeta.add(lblFoto, BorderLayout.WEST);

                JPanel textoPanel = new JPanel(new GridBagLayout());
                textoPanel.setBackground(colorEmpleado);

                JPanel textos = new JPanel();
                textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
                textos.setBackground(colorEmpleado);

                JLabel lblNombre = new JLabel(nombre);
                lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 20));
                lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lblRol = new JLabel(rol);
                lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblRol.setForeground(new Color(80,80,80));
                lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);

                textos.add(lblNombre);
                textos.add(lblRol);
                textoPanel.add(textos);
                tarjeta.add(textoPanel, BorderLayout.CENTER);

                JButton btnEmpleado = crearBotonRedondeado("", colorEmpleado, new Dimension(620,130));
                btnEmpleado.setLayout(new BorderLayout());
                btnEmpleado.add(tarjeta, BorderLayout.CENTER);
                btnEmpleado.addActionListener(e -> {
                    panelDetalle.removeAll();
                    panelDetalle.add(new PanelDetalleEmpleado(conn, id), BorderLayout.CENTER);
                    CardLayout cl = (CardLayout) getLayout();
                    cl.show(this, "detalle");
                    panelDetalle.revalidate();
                    panelDetalle.repaint();
                });
                JPanel btnWrapper = new JPanel();
                btnWrapper.setLayout(new BoxLayout(btnWrapper, BoxLayout.X_AXIS));
                btnWrapper.setBackground(colorFondo);
                btnWrapper.add(Box.createHorizontalGlue());
                btnWrapper.add(btnEmpleado);
                btnWrapper.add(Box.createHorizontalGlue());

                empleadosContenedor.add(btnWrapper);
                empleadosContenedor.add(Box.createVerticalStrut(20));
            }
        } catch(Exception e){
            JOptionPane.showMessageDialog(this, "Error al cargar vendedores: " + e.getMessage());
            e.printStackTrace();
        }

        contenedor.add(empleadosContenedor);
        panelLista.add(contenedor);
        revalidate();
        repaint();
    }

    public void volverLista() {
        CardLayout cl = (CardLayout) getLayout();
        cl.show(this, "lista");
    }
}
