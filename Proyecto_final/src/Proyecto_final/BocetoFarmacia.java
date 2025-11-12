package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BocetoFarmacia extends JFrame {

    private JPanel panelPrincipal;
    private JPanel panelLista;
    private JPanel panelDetalle;

    public BocetoFarmacia() {
        setTitle("Farmacia - Boceto de Gestión de Trabajadores");
        setSize(650, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new CardLayout());

        panelLista = new JPanel();
        panelLista.setLayout(null);
        panelLista.setBackground(new Color(230, 255, 230)); // verde claro

        JLabel lblTitulo = new JLabel("Trabajadores de la Farmacia", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setBounds(0, 20, 650, 30);
        lblTitulo.setForeground(new Color(0, 100, 0));
        panelLista.add(lblTitulo);

        String[][] trabajadores = {
            {"Ana López", "Vendedora"},
            {"Carlos Gómez", "Vendedor"},
            {"María Torres", "Vendedora"},
            {"Luis Martínez", "Vendedor"}
        };

        int y = 80;
        for (String[] t : trabajadores) {
            JButton btn = crearBotonTrabajador(t[0], t[1]);
            btn.setBounds(100, y, 450, 70);
            panelLista.add(btn);

            btn.addActionListener(e -> mostrarDetalle(t[0], t[1]));
            y += 85;
        }

        panelDetalle = new JPanel();
        panelDetalle.setLayout(null);
        panelDetalle.setBackground(new Color(245, 255, 245));

        add(panelLista, "lista");
        add(panelDetalle, "detalle");

        panelPrincipal = panelLista;
    }

    private JButton crearBotonTrabajador(String nombre, String cargo) {
        JButton btn = new JButton();
        btn.setLayout(null);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(180, 220, 180)));
        btn.setFocusPainted(false);

        JPanel foto = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(220, 220, 220));
                g.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        foto.setBounds(10, 10, 50, 50);
        btn.add(foto);

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNombre.setBounds(80, 10, 300, 20);
        btn.add(lblNombre);

        JLabel lblCargo = new JLabel(cargo);
        lblCargo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCargo.setForeground(new Color(60, 90, 60));
        lblCargo.setBounds(80, 35, 200, 20);
        btn.add(lblCargo);

        return btn;
    }

    private void mostrarDetalle(String nombre, String cargo) {
        panelDetalle.removeAll();

        JLabel lblTitulo = new JLabel("Detalles del Trabajador", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(0, 100, 0));
        lblTitulo.setBounds(0, 20, 650, 30);
        panelDetalle.add(lblTitulo);

        JPanel foto = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(220, 220, 220));
                g.fillOval(0, 0, getWidth(), getHeight());
            }
        };
        foto.setBounds(50, 80, 100, 100);
        panelDetalle.add(foto);

        JButton btnCambiarFoto = new JButton("Cambiar Foto");
        btnCambiarFoto.setBounds(40, 190, 120, 25);
        panelDetalle.add(btnCambiarFoto);

        // Campos de datos
        JLabel lblNombre = new JLabel("Nombre completo:");
        lblNombre.setBounds(200, 90, 130, 25);
        panelDetalle.add(lblNombre);

        JTextField txtNombre = new JTextField(nombre);
        txtNombre.setBounds(340, 90, 250, 25);
        panelDetalle.add(txtNombre);

        JLabel lblID = new JLabel("Identificación:");
        lblID.setBounds(200, 130, 130, 25);
        panelDetalle.add(lblID);

        JTextField txtID = new JTextField();
        txtID.setBounds(340, 130, 250, 25);
        panelDetalle.add(txtID);

        JLabel lblDir = new JLabel("Dirección:");
        lblDir.setBounds(200, 170, 130, 25);
        panelDetalle.add(lblDir);

        JTextField txtDir = new JTextField();
        txtDir.setBounds(340, 170, 250, 25);
        panelDetalle.add(txtDir);

        JCheckBox chkActivo = new JCheckBox("Trabajador activo", true);
        chkActivo.setBounds(340, 210, 200, 25);
        panelDetalle.add(chkActivo);

        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.setBackground(new Color(150, 230, 150));
        btnGuardar.setBounds(200, 260, 170, 30);
        panelDetalle.add(btnGuardar);

        JButton btnVolver = new JButton("Volver");
        btnVolver.setBounds(390, 260, 120, 30);
        panelDetalle.add(btnVolver);

        btnVolver.addActionListener(e -> {
            getContentPane().removeAll();
            getContentPane().add(panelLista);
            revalidate();
            repaint();
        });

        getContentPane().removeAll();
        getContentPane().add(panelDetalle);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BocetoFarmacia().setVisible(true);
        });
    }
}
