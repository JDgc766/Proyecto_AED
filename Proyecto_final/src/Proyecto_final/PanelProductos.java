package Proyecto_final;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PanelProductos extends JPanel {

    private List<Producto> listaProductos;

    private BusquedaPanel busquedaPanel;
    private ResultadosPanel resultadosPanel;
    private FacturaPanel facturaPanel;

    public PanelProductos() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(225, 245, 254));

        listaProductos = new ArrayList<>();
        cargarProductos(); // Aquí carga la lista, después puedes conectar a DB

        busquedaPanel = new BusquedaPanel();
        resultadosPanel = new ResultadosPanel();
        facturaPanel = new FacturaPanel();

        add(busquedaPanel, BorderLayout.NORTH);
        add(resultadosPanel, BorderLayout.CENTER);
        add(facturaPanel, BorderLayout.SOUTH);

        busquedaPanel.setBuscarListener(e -> mostrarResultados(busquedaPanel.getTextoBusqueda()));
        mostrarResultados("");
    }

    private void mostrarResultados(String texto) {
        List<Producto> filtrados = new ArrayList<>();
        for (Producto p : listaProductos) {
            if (texto.isEmpty() || texto.equals("Buscar producto...") ||
                    p.nombre.toLowerCase().contains(texto.toLowerCase()) ||
                    (p.nombreGenerico != null && p.nombreGenerico.toLowerCase().contains(texto.toLowerCase()))) {
                filtrados.add(p);
            }
        }
        resultadosPanel.mostrarProductos(filtrados);
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

        facturaPanel.agregarProducto(p, cantidad);
    }

    private void editarProducto(Producto p) {
        JTextField campoNombre = new JTextField(p.nombre);
        JTextField campoNombreGen = new JTextField(p.nombreGenerico != null ? p.nombreGenerico : "");
        JTextField campoFarmaceutica = new JTextField(p.farmaceutica);
        JTextField campoGramaje = new JTextField(p.gramaje);
        JTextField campoPrecio = new JTextField(String.valueOf(p.precioVenta));
        JTextField campoStock = new JTextField(String.valueOf(p.stock));

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre:")); panel.add(campoNombre);
        panel.add(new JLabel("Nombre Genérico:")); panel.add(campoNombreGen);
        panel.add(new JLabel("Farmacéutica:")); panel.add(campoFarmaceutica);
        panel.add(new JLabel("Gramaje:")); panel.add(campoGramaje);
        panel.add(new JLabel("Precio:")); panel.add(campoPrecio);
        panel.add(new JLabel("Stock:")); panel.add(campoStock);

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

                resultadosPanel.refreshProducto(p);
                facturaPanel.actualizarProducto(p, nombreOriginal);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio y stock deben ser números válidos.");
            }
        }
    }

    // -------------------- SUBCOMPONENTES --------------------

    private class BusquedaPanel extends JPanel {
        private JTextField cajaBusqueda;
        private JButton botonBuscar;

        public BusquedaPanel() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
            cajaBusqueda = new JTextField(30);
            colocarPlaceholder(cajaBusqueda, "Buscar producto...");
            botonBuscar = new JButton("Buscar");
            add(cajaBusqueda);
            add(botonBuscar);
        }

        public String getTextoBusqueda() {
            return cajaBusqueda.getText();
        }

        public void setBuscarListener(ActionListener l) {
            botonBuscar.addActionListener(l);
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
    }

    private class ResultadosPanel extends JPanel {
        public ResultadosPanel() {
            setLayout(new GridLayout(0, 3, 10, 10));
            setBackground(Color.WHITE);
        }

        public void mostrarProductos(List<Producto> productos) {
            removeAll();
            for (Producto p : productos) {
                add(new ProductoItemPanel(p));
            }
            revalidate();
            repaint();
        }

        public void refreshProducto(Producto p) {
            // simple refresh
            mostrarProductos(listaProductos);
        }
    }

    private class ProductoItemPanel extends JPanel {
        public ProductoItemPanel(Producto p) {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setBackground(Color.WHITE);

            JLabel foto = new JLabel();
            foto.setHorizontalAlignment(SwingConstants.CENTER);
            foto.setPreferredSize(new Dimension(100, 100));

            try {
                if (p.foto != null && p.foto.length > 0) {
                    ImageIcon icon = new ImageIcon(p.foto);
                    Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    foto.setIcon(new ImageIcon(img));
                } else {
                    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = img.createGraphics();
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillRect(0, 0, 100, 100);
                    g2.dispose();
                    foto.setIcon(new ImageIcon(img));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            JLabel desc = new JLabel("<html><center>" + p.nombre + "<br>" +
                    (p.nombreGenerico != null ? p.nombreGenerico : "") + "<br>" +
                    p.farmaceutica + "<br>" + p.gramaje + "</center></html>");
            desc.setHorizontalAlignment(SwingConstants.CENTER);

            add(foto, BorderLayout.CENTER);
            add(desc, BorderLayout.SOUTH);

            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Object[] opciones = {"Agregar a factura", "Editar producto", "Cancelar"};
                    int res = JOptionPane.showOptionDialog(PanelProductos.this, "Selecciona una acción para " + p.nombre,
                            "Opciones", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);
                    if (res == 0) agregarAFactura(p);
                    else if (res == 1) editarProducto(p);
                }
            });
        }
    }

    private class FacturaPanel extends JPanel {
        private DefaultTableModel modeloTabla;
        private JTable tablaFactura;
        private JLabel etiquetaSubtotal, etiquetaIVA, etiquetaTotal;

        public FacturaPanel() {
            setLayout(new BorderLayout());
            tablaFactura = new JTable();
            modeloTabla = new DefaultTableModel(new Object[]{"Nombre", "Precio", "Cantidad", "Total"}, 0);
            tablaFactura.setModel(modeloTabla);

            JScrollPane scrollTabla = new JScrollPane(tablaFactura);
            scrollTabla.setPreferredSize(new Dimension(0, 200));

            JPanel panelTotales = new JPanel(new GridLayout(3, 2, 10, 5));
            etiquetaSubtotal = new JLabel("$0.00");
            etiquetaIVA = new JLabel("$0.00");
            etiquetaTotal = new JLabel("$0.00");

            panelTotales.add(new JLabel("Subtotal:")); panelTotales.add(etiquetaSubtotal);
            panelTotales.add(new JLabel("IVA (15%):")); panelTotales.add(etiquetaIVA);
            panelTotales.add(new JLabel("Total Neto:")); panelTotales.add(etiquetaTotal);

            add(scrollTabla, BorderLayout.CENTER);
            add(panelTotales, BorderLayout.SOUTH);
        }

        public void agregarProducto(Producto p, int cantidad) {
            double total = p.precioVenta * cantidad;
            modeloTabla.addRow(new Object[]{p.nombre, "$" + String.format("%.2f", p.precioVenta), cantidad, "$" + String.format("%.2f", total)});
            actualizarTotales();
        }

        public void actualizarProducto(Producto p, String nombreOriginal) {
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                String nombreFila = modeloTabla.getValueAt(i, 0).toString();
                if (nombreFila.equals(nombreOriginal)) {
                    int cantidad = Integer.parseInt(modeloTabla.getValueAt(i, 2).toString());
                    double total = p.precioVenta * cantidad;
                    modeloTabla.setValueAt(p.nombre, i, 0);
                    modeloTabla.setValueAt("$" + String.format("%.2f", p.precioVenta), i, 1);
                    modeloTabla.setValueAt("$" + String.format("%.2f", total), i, 3);
                }
            }
            actualizarTotales();
        }

        private void actualizarTotales() {
            double subtotal = 0;
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                String valor = modeloTabla.getValueAt(i, 3).toString().replace("$", "");
                subtotal += Double.parseDouble(valor);
            }
            double iva = subtotal * 0.15;
            double total = subtotal + iva;

            etiquetaSubtotal.setText("$" + String.format("%.2f", subtotal));
            etiquetaIVA.setText("$" + String.format("%.2f", iva));
            etiquetaTotal.setText("$" + String.format("%.2f", total));
        }
    }

    // -------------------- MODELO --------------------
    private static class Producto {
        int id;
        String nombre, nombreGenerico, farmaceutica, gramaje;
        double precioVenta;
        int stock;
        byte[] foto;

        public Producto(int id, String nombre, String nombreGenerico, String farmaceutica,
                        double precioVenta, String gramaje, int stock, byte[] foto) {
            this.id = id;
            this.nombre = nombre;
            this.nombreGenerico = nombreGenerico;
            this.farmaceutica = farmaceutica;
            this.gramaje = gramaje;
            this.precioVenta = precioVenta;
            this.stock = stock;
            this.foto = foto;
        }
    }

    // -------------------- CARGA DE DATOS --------------------
    private void cargarProductos() {
        // Por ahora ejemplo dummy
        listaProductos.clear();
        for (int i = 1; i <= 12; i++) {
            listaProductos.add(new Producto(i, "Producto " + i, "Genérico " + i, "Farmacéutica " + i,
                    10 + i, "10mg", 20 + i, null));
        }
    }
}
