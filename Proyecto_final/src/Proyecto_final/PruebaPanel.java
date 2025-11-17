package Proyecto_final;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PruebaPanel extends JFrame {

    private static final String PLACEHOLDER_BUSQUEDA = "Buscar producto...";

    private JTextField cajaBusqueda;
    private JPanel panelResultados;
    private JScrollPane scrollResultados;
    private JTable tablaFactura;
    private DefaultTableModel modeloTabla;
    private JLabel etiquetaSubtotal, etiquetaIVA, etiquetaTotal;
    private List<Producto> listaProductos;

    public PruebaPanel() {
        setTitle("Buscador de Productos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        listaProductos = new ArrayList<>();
        cargarProductos();
        crearInterfaz();
        setVisible(true);
    }

    private void crearInterfaz() {
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        cajaBusqueda = new JTextField(30);
        colocarPlaceholder(cajaBusqueda, PLACEHOLDER_BUSQUEDA);
        JButton botonBuscar = new JButton("Buscar");
        panelSuperior.add(cajaBusqueda);
        panelSuperior.add(botonBuscar);
        add(panelSuperior, BorderLayout.NORTH);
        panelResultados = new JPanel();
        panelResultados.setLayout(new GridLayout(0, 3, 10, 10));
        scrollResultados = new JScrollPane(panelResultados);
        scrollResultados.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollResultados.setPreferredSize(new Dimension(0, 350));
        add(scrollResultados, BorderLayout.CENTER);
        modeloTabla = new DefaultTableModel(new Object[]{"Nombre", "Precio", "Cantidad", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaFactura = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaFactura);
        scrollTabla.setPreferredSize(new Dimension(0, 200));
        JPanel panelTotales = new JPanel(new GridLayout(3, 2, 10, 5));
        etiquetaSubtotal = new JLabel("$0.00");
        etiquetaIVA = new JLabel("$0.00");
        etiquetaTotal = new JLabel("$0.00");
        panelTotales.add(new JLabel("Subtotal:"));
        panelTotales.add(etiquetaSubtotal);
        panelTotales.add(new JLabel("IVA (15%):"));
        panelTotales.add(etiquetaIVA);
        panelTotales.add(new JLabel("Total Neto:"));
        panelTotales.add(etiquetaTotal);
        JPanel panelFactura = new JPanel(new BorderLayout());
        panelFactura.add(scrollTabla, BorderLayout.CENTER);
        panelFactura.add(panelTotales, BorderLayout.SOUTH);
        add(panelFactura, BorderLayout.SOUTH);
        botonBuscar.addActionListener(e -> mostrarResultados(cajaBusqueda.getText()));
        cajaBusqueda.addActionListener(e -> mostrarResultados(cajaBusqueda.getText()));
        mostrarResultados("");
    }

    private void mostrarResultados(String texto) {
        panelResultados.removeAll();
        String criterio = (texto == null) ? "" : texto.trim();
        if (criterio.equals(PLACEHOLDER_BUSQUEDA)) criterio = "";
        List<Producto> productos = buscarProductos(criterio);
        for (Producto p : productos) {
            JPanel panelProd = new JPanel(new BorderLayout());
            panelProd.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            panelProd.setBackground(Color.WHITE);
            JLabel etiquetaFoto = new JLabel();
            etiquetaFoto.setHorizontalAlignment(SwingConstants.CENTER);
            etiquetaFoto.setPreferredSize(new Dimension(100, 100));
            try {
                if (p.foto != null && p.foto.length > 0) {
                    ImageIcon icon = new ImageIcon(p.foto);
                    Image imagen = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    etiquetaFoto.setIcon(new ImageIcon(imagen));
                } else {
                    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = img.createGraphics();
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillRect(0, 0, 100, 100);
                    g2.dispose();
                    etiquetaFoto.setIcon(new ImageIcon(img));
                }
            } catch (Exception ex) {
                BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillRect(0, 0, 100, 100);
                g2.dispose();
                etiquetaFoto.setIcon(new ImageIcon(img));
            }
            panelProd.add(etiquetaFoto, BorderLayout.CENTER);
            JLabel etiquetaDesc = new JLabel("<html><center>" + p.nombre + "<br>" +
                    (p.nombreGenerico != null ? p.nombreGenerico : "") + "<br>" +
                    p.farmaceutica + "<br>" + p.gramaje + "</center></html>");
            etiquetaDesc.setHorizontalAlignment(SwingConstants.CENTER);
            panelProd.add(etiquetaDesc, BorderLayout.SOUTH);
            panelProd.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panelProd.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    mostrarOpciones(p);
                }
            });
            panelResultados.add(panelProd);
        }
        panelResultados.revalidate();
        panelResultados.repaint();
    }

    private void mostrarOpciones(Producto p) {
        Object[] opciones = {"Agregar a factura", "Editar producto", "Cancelar"};
        int res = JOptionPane.showOptionDialog(this, "Selecciona una acción para " + p.nombre, "Opciones",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);
        if (res == 0) agregarAFactura(p);
        else if (res == 1) editarProducto(p);
    }

    private void agregarAFactura(Producto p) {
        int cantidad = 0;
        boolean valido = false;
        while (!valido) {
            String entrada = JOptionPane.showInputDialog(this, "Cantidad de " + p.nombre + " (Stock: " + p.stock + "):");
            if (entrada == null) return;
            try {
                cantidad = Integer.parseInt(entrada);
                if (cantidad <= 0 || cantidad > p.stock) {
                    JOptionPane.showMessageDialog(this, "Cantidad inválida. Debe ser mayor que 0 y menor o igual al stock disponible.");
                } else {
                    valido = true;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingresa un número válido.");
            }
        }
        double total = p.precioVenta * cantidad;
        modeloTabla.addRow(new Object[]{p.nombre, "$" + String.format("%.2f", p.precioVenta), cantidad, "$" + String.format("%.2f", total)});
        actualizarTotales();
        p.stock -= cantidad;
        try (Connection conn = ConexionDB.obtenerConexion()) {
            if (conn != null) {
                String sql = "UPDATE Producto SET Stock = ? WHERE Id_Producto = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, p.stock);
                    ps.setInt(2, p.id);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error actualizando stock: " + ex.getMessage());
        }
    }

    private void editarProducto(Producto p) {
        JTextField campoNombre = new JTextField(p.nombre);
        JTextField campoNombreGen = new JTextField(p.nombreGenerico != null ? p.nombreGenerico : "");
        JTextField campoFarmaceutica = new JTextField(p.farmaceutica);
        JTextField campoGramaje = new JTextField(p.gramaje);
        JTextField campoPrecio = new JTextField(String.valueOf(p.precioVenta));
        JTextField campoStock = new JTextField(String.valueOf(p.stock));
        JButton botonFoto = new JButton("Cambiar foto");
        final byte[][] nuevaFoto = {p.foto};
        botonFoto.addActionListener(e -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {}
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "bmp", "gif"));
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                try {
                    ImageIcon icon = new ImageIcon(chooser.getSelectedFile().getAbsolutePath());
                    int origW = icon.getIconWidth();
                    int origH = icon.getIconHeight();
                    if (origW <= 0 || origH <= 0) {
                        Image tmp = icon.getImage();
                        origW = tmp.getWidth(null);
                        origH = tmp.getHeight(null);
                    }
                    int maxDim = 800;
                    int newW = origW;
                    int newH = origH;
                    if (origW > maxDim || origH > maxDim) {
                        double scale = Math.min((double) maxDim / origW, (double) maxDim / origH);
                        newW = (int) (origW * scale);
                        newH = (int) (origH * scale);
                    }
                    Image img = icon.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                    BufferedImage bi = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = bi.createGraphics();
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, newW, newH);
                    g2.drawImage(img, 0, 0, null);
                    g2.dispose();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(bi, "jpg", baos);
                    nuevaFoto[0] = baos.toByteArray();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al cargar la imagen.");
                }
            }
        });
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre:")); panel.add(campoNombre);
        panel.add(new JLabel("Nombre Genérico:")); panel.add(campoNombreGen);
        panel.add(new JLabel("Farmacéutica:")); panel.add(campoFarmaceutica);
        panel.add(new JLabel("Gramaje:")); panel.add(campoGramaje);
        panel.add(new JLabel("Precio:")); panel.add(campoPrecio);
        panel.add(new JLabel("Stock:")); panel.add(campoStock);
        panel.add(new JLabel("Foto:")); panel.add(botonFoto);
        int opcion = JOptionPane.showConfirmDialog(this, panel, "Editar Producto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                String nombreOriginal = p.nombre;
                p.nombre = campoNombre.getText();
                p.nombreGenerico = campoNombreGen.getText();
                p.farmaceutica = campoFarmaceutica.getText();
                p.gramaje = campoGramaje.getText();
                p.precioVenta = Double.parseDouble(campoPrecio.getText());
                p.stock = Integer.parseInt(campoStock.getText());
                p.foto = nuevaFoto[0];
                try (Connection conn = ConexionDB.obtenerConexion()) {
                    if (conn != null) {
                        String sql = "UPDATE Producto SET Nombre=?, Nombre_Generico=?, Farmaceutica=?, Gramaje=?, Precio_Venta=?, Stock=?, Foto_Producto=? WHERE Id_Producto=?";
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setString(1, p.nombre);
                            ps.setString(2, p.nombreGenerico);
                            ps.setString(3, p.farmaceutica);
                            ps.setString(4, p.gramaje);
                            ps.setDouble(5, p.precioVenta);
                            ps.setInt(6, p.stock);
                            ps.setBytes(7, p.foto);
                            ps.setInt(8, p.id);
                            ps.executeUpdate();
                        }
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error guardando cambios: " + ex.getMessage());
                }
                mostrarResultados(cajaBusqueda.getText());
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    String nombreFila = modeloTabla.getValueAt(i, 0).toString();
                    if (nombreFila.equals(nombreOriginal)) {
                        int cantidad = Integer.parseInt(modeloTabla.getValueAt(i, 2).toString());
                        double total = p.precioVenta * cantidad;
                        modeloTabla.setValueAt(p.nombre, i, 0);
                        modeloTabla.setValueAt("$" + String.format("%.2f", p.precioVenta), i, 1);
                        modeloTabla.setValueAt(cantidad, i, 2);
                        modeloTabla.setValueAt("$" + String.format("%.2f", total), i, 3);
                    }
                }
                actualizarTotales();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio y stock deben ser números válidos.");
            }
        }
    }

    private void actualizarTotales() {
        double subtotal = 0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            try {
                String valor = modeloTabla.getValueAt(i, 3).toString().replace("$", "");
                subtotal += Double.parseDouble(valor);
            } catch (NumberFormatException ex) {
                subtotal += 0;
            }
        }
        double iva = subtotal * 0.15;
        double total = subtotal + iva;
        etiquetaSubtotal.setText("$" + String.format("%.2f", subtotal));
        etiquetaIVA.setText("$" + String.format("%.2f", iva));
        etiquetaTotal.setText("$" + String.format("%.2f", total));
    }

    private void cargarProductos() {
        listaProductos.clear();
        try (Connection conn = ConexionDB.obtenerConexion()) {
            if (conn != null) {
                String sql = "SELECT Id_Producto, Nombre, Nombre_Generico, Farmaceutica, Precio_Venta, Gramaje, Stock, Foto_Producto FROM Producto";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("Id_Producto");
                        String nombre = rs.getString("Nombre");
                        String nombreGen = rs.getString("Nombre_Generico");
                        String farm = rs.getString("Farmaceutica");
                        double precio = rs.getDouble("Precio_Venta");
                        String gram = rs.getString("Gramaje");
                        int stock = rs.getInt("Stock");
                        byte[] foto = rs.getBytes("Foto_Producto");
                        listaProductos.add(new Producto(id, nombre, nombreGen, farm, precio, gram, stock, foto));
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error cargando productos: " + ex.getMessage());
        }
    }

    private List<Producto> buscarProductos(String texto) {
        List<Producto> encontrados = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion()) {
            if (conn != null) {
                String sql;
                PreparedStatement ps;
                if (texto == null || texto.trim().isEmpty()) {
                    sql = "SELECT Id_Producto, Nombre, Nombre_Generico, Farmaceutica, Precio_Venta, Gramaje, Stock, Foto_Producto FROM Producto";
                    ps = conn.prepareStatement(sql);
                } else {
                    sql = "SELECT Id_Producto, Nombre, Nombre_Generico, Farmaceutica, Precio_Venta, Gramaje, Stock, Foto_Producto FROM Producto WHERE Nombre LIKE ? OR Nombre_Generico LIKE ?";
                    ps = conn.prepareStatement(sql);
                    String param = "%" + texto + "%";
                    ps.setString(1, param);
                    ps.setString(2, param);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        encontrados.add(new Producto(
                                rs.getInt("Id_Producto"),
                                rs.getString("Nombre"),
                                rs.getString("Nombre_Generico"),
                                rs.getString("Farmaceutica"),
                                rs.getDouble("Precio_Venta"),
                                rs.getString("Gramaje"),
                                rs.getInt("Stock"),
                                rs.getBytes("Foto_Producto")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al buscar productos: " + e.getMessage());
        }
        return encontrados;
    }

    private void colocarPlaceholder(JTextField campo, String texto) {
        campo.setText(texto);
        campo.setForeground(Color.GRAY);
        campo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (campo.getText().equals(texto)) {
                    campo.setText("");
                    campo.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (campo.getText().isEmpty()) {
                    campo.setText(texto);
                    campo.setForeground(Color.GRAY);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PruebaPanel::new);
    }

    private static class Producto {
        int id;
        String nombre, nombreGenerico, farmaceutica, gramaje;
        double precioVenta;
        int stock;
        byte[] foto;

        public Producto(int id, String nombre, String nombreGenerico, String farmaceutica, double precioVenta, String gramaje, int stock, byte[] foto) {
            this.id = id;
            this.nombre = nombre;
            this.nombreGenerico = nombreGenerico;
            this.farmaceutica = farmaceutica;
            this.precioVenta = precioVenta;
            this.gramaje = gramaje;
            this.stock = stock;
            this.foto = foto;
        }
    }
}
