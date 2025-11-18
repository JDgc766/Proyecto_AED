package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Interfaz extends JFrame {

    public Interfaz() {
        setUndecorated(true);
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        ImageIcon icon = new ImageIcon(getClass().getResource("/imagenes/f.jpg"));
        Image img = icon.getImage();
        Image imgEscalada = img.getScaledInstance(400, 450, Image.SCALE_SMOOTH);

        setIconImage(img);

        JPanel panelLogin = new JPanel();
        panelLogin.setBackground(new Color(225, 245, 254));
        panelLogin.setPreferredSize(new Dimension(300, getHeight()));
        panelLogin.setLayout(null);

        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setForeground(new Color(0, 77, 64));
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUsuario.setBounds(40, 80, 100, 25);

        JTextField txtUsuario = new JTextField();
        txtUsuario.setBounds(40, 110, 220, 30);
        txtUsuario.setBorder(null);
        txtUsuario.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblContrasena = new JLabel("Contraseña:");
        lblContrasena.setForeground(new Color(0, 77, 64));
        lblContrasena.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblContrasena.setBounds(40, 160, 100, 25);

        JPasswordField txtContrasena = new JPasswordField();
        txtContrasena.setBounds(40, 190, 220, 30);
        txtContrasena.setBorder(null);
        txtContrasena.setEchoChar('●');
        txtContrasena.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnMostrar = new JButton();
        btnMostrar.setBounds(265, 190, 30, 30);
        btnMostrar.setBorderPainted(false);
        btnMostrar.setFocusPainted(false);
        btnMostrar.setContentAreaFilled(false);
        btnMostrar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon iconTrue = new ImageIcon(getClass().getResource("/imagenes/open.png"));
        ImageIcon iconClose = new ImageIcon(getClass().getResource("/imagenes/close.png"));

        Image imgTrue = iconTrue.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon iconTrueEscalado = new ImageIcon(imgTrue);

        Image imgClose = iconClose.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon iconCloseEscalado = new ImageIcon(imgClose);

        btnMostrar.setIcon(iconCloseEscalado);
        txtContrasena.setEchoChar('●');

        final boolean[] mostrar = {false};
        btnMostrar.addActionListener(e -> {
            mostrar[0] = !mostrar[0];
            if (mostrar[0]) {
                txtContrasena.setEchoChar((char) 0);
                btnMostrar.setIcon(iconTrueEscalado);
            } else {
                txtContrasena.setEchoChar('●');
                btnMostrar.setIcon(iconCloseEscalado);
            }
        });

        Color botonNegro = new Color(23, 23, 23);
        Font fuenteBoton = new Font("Segoe UI", Font.BOLD, 14);

        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setBounds(20, 250, 140, 35);
        btnLogin.setBackground(botonNegro);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setFont(fuenteBoton);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(170, 250, 120, 35);
        btnCancelar.setBackground(botonNegro);
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.setFont(fuenteBoton);
        btnCancelar.addActionListener(e -> dispose());

        btnLogin.addActionListener(e -> {
            String user = txtUsuario.getText().trim();
            String pass = new String(txtContrasena.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Debe ingresar usuario y contraseña");
                return;
            }

            Connection conn = ConexionDB.obtenerConexion();
            if (conn == null) return;

            try {
                String sql = "SELECT Id_Empleado, Nombre, Rol, Activo FROM Empleado WHERE Usuario = ? AND Contrasenia = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, user);
                ps.setString(2, pass);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    int idEmpleado = rs.getInt("Id_Empleado");
                    String nombreEmpleado = rs.getString("Nombre");
                    String rol = rs.getString("Rol");
                    String activo = rs.getString("Activo");

                    if (!activo.equals("S")) {
                        JOptionPane.showMessageDialog(null, "Este usuario está inactivo.");
                        return;
                    }

                    if (rol.equals("GERENTE")) {
                        // Mostrar mensaje y luego abrir GerenteUIModerno
                        Interfaz.this.setEnabled(false);
                        Runnable abrirVentanaGerente = () -> {
                            GerenteUIModerno gerente = new GerenteUIModerno();
                            gerente.setVisible(true);
                            Interfaz.this.dispose();
                        };
                        new Mensajes(Interfaz.this, "¡Bienvenido Don Jairo!", abrirVentanaGerente);

                    } else if (rol.equals("VENDEDOR")) {
                        new Mensajes(Interfaz.this, "¡Bienvenido, " + nombreEmpleado + "!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Rol no reconocido.");
                        return;
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "Error, usuario no encontrado o registrado");
                }

                rs.close();
                ps.close();
                conn.close();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al consultar la base de datos");
            }
        });


        panelLogin.add(lblUsuario);
        panelLogin.add(txtUsuario);
        panelLogin.add(lblContrasena);
        panelLogin.add(txtContrasena);
        panelLogin.add(btnMostrar);
        panelLogin.add(btnLogin);
        panelLogin.add(btnCancelar);
        add(panelLogin, BorderLayout.WEST);

        JPanel panelImagen = new JPanel();
        panelImagen.setBackground(new Color(225, 245, 254));
        panelImagen.setLayout(new BorderLayout());

        JLabel etiquetaImagen = new JLabel(new ImageIcon(imgEscalada));
        etiquetaImagen.setHorizontalAlignment(SwingConstants.CENTER);
        panelImagen.add(etiquetaImagen, BorderLayout.CENTER);

        add(panelImagen, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        new Interfaz().setVisible(true);
    }
}
