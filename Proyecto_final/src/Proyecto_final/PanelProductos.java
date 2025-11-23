package Proyecto_final;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PanelVentaProductos
 * -------------------
 * Un JPanel modular y moderno para la venta en mostrador.
 * - Vista de productos en tarjetas con imagen (scrollable)
 * - Carrito/Factura en tabla sin columna ID
 * - Botones sin borde y con símbolos (unicode)
 * - Scrollbar personalizada (redondeada, verde pastel)
 * - Única fuente de verdad para productos (lista) y carrito (map id->cantidad)
 *
 * Requiere: ConexionDB.obtenerConexion() para SQLite
 */
public class PanelProductos extends JPanel {

    // === MODELO DE DATOS (única fuente de verdad) ===
    // productosMap: id -> Producto (cargado desde BD)
    private final Map<Integer, Producto> productosMap = new LinkedHashMap<>();
    // carritoMap: id -> cantidad (se refiere a los mismos objetos en productosMap)
    private final Map<Integer, Integer> carritoMap = new LinkedHashMap<>();

    // === COMPONENTES UI ===
    private final JPanel panelCatalogo = new JPanel();
    private final JScrollPane scrollCatalogo;

    private final DefaultTableModel modeloFactura;
    private final JTable tablaFactura;
    private final JLabel lblTotal = new JLabel("Total: $0.00");

    // Tamaños/estilos
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    public PanelProductos() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(250, 252, 253));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ---------- Top: búsqueda y controles ----------
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);
        JPanel busquedaPane = crearPanelBusqueda();
        top.add(busquedaPane, BorderLayout.WEST);

        JPanel botonesPane = crearPanelAccionesRapidas();
        top.add(botonesPane, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ---------- Center: catálogo de productos (grid con scroll) ----------
        panelCatalogo.setLayout(new ModifiedWrapLayout(FlowLayout.LEFT, 12, 12));
        panelCatalogo.setOpaque(false);

        scrollCatalogo = new JScrollPane(panelCatalogo,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCatalogo.setBorder(BorderFactory.createEmptyBorder());
        scrollCatalogo.getVerticalScrollBar().setUnitIncrement(16);
        scrollCatalogo.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        add(scrollCatalogo, BorderLayout.CENTER);

        // ---------- South: factura/carrito ----------
        modeloFactura = new DefaultTableModel(new Object[]{"Nombre", "Precio", "Cant.", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaFactura = new JTable(modeloFactura);
        tablaFactura.setFont(FONT_NORMAL);
        tablaFactura.setRowHeight(28);
        tablaFactura.getTableHeader().setFont(FONT_BOLD);

        JScrollPane scrollFactura = new JScrollPane(tablaFactura);
        scrollFactura.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        scrollFactura.setPreferredSize(new Dimension(85, 250));

        JPanel south = new JPanel(new BorderLayout(8,8));
        south.setOpaque(false);
        south.add(scrollFactura, BorderLayout.CENTER);

        JPanel rightSouth = new JPanel(new BorderLayout(8,8));
        rightSouth.setOpaque(false);
        lblTotal.setFont(FONT_BOLD);
        rightSouth.add(lblTotal, BorderLayout.NORTH);

        JPanel botonesFactura = new JPanel(new GridLayout(3,1,8,8));
        botonesFactura.setOpaque(false);
        botonesFactura.add(makeIconButton("🛒 " , e -> agregarSeleccionadoAlCarrito()));
        botonesFactura.add(makeIconButton("❌ " , e -> quitarSeleccionadoDelCarrito()));
        botonesFactura.add(makeIconButton("💰 "  , e -> ejecutarVenta()));
        rightSouth.add(botonesFactura, BorderLayout.CENTER);

        south.add(rightSouth, BorderLayout.EAST);

        add(south, BorderLayout.SOUTH);

        // Carga inicial
        cargarProductosDesdeBD();
        refrescarCatalogo();
    }

    // ---------------------- CREACIÓN DE UI ----------------------
    private JPanel crearPanelBusqueda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);

        JTextField txtBuscar = new JTextField(30);
        txtBuscar.setFont(FONT_NORMAL);
        txtBuscar.setToolTipText("Buscar por nombre o genérico...");
        p.add(txtBuscar);

        JButton btnBuscar = makeIconButton("🔍", e -> {
            String q = txtBuscar.getText().trim().toLowerCase();
            aplicarFiltro(q);
        });
        btnBuscar.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
        p.add(btnBuscar);

        // Buscar en cada pulsación de tecla
        txtBuscar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String q = txtBuscar.getText().trim().toLowerCase();
                aplicarFiltro(q);
            }
        });

        return p;
    }

    private JPanel crearPanelAccionesRapidas() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);

        p.add(makeIconButton("🧾 Ver Factura", e -> mostrarVentanaFactura()));
        p.add(makeIconButton("🔄 Refresecar", e -> {
            cargarProductosDesdeBD();
            refrescarCatalogo();
        }));
        return p;
    }

    // Botones sin borde y con texto/icono (unicode). Se usan en todo el panel.
    private JButton makeIconButton(String text, ActionListener listener) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(listener);
        return b;
    }

    // ---------------------- CARGA Y MODELO ----------------------
    private void cargarProductosDesdeBD() {
        productosMap.clear();
        String sql = "SELECT Id_Producto, Nombre, Nombre_Generico, Farmaceutica, Gramaje, Precio_Venta, Stock, Foto_Producto, Activo FROM Producto WHERE Activo = 'S' OR Activo IS NULL";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("Id_Producto");
                String nombre = rs.getString("Nombre");
                String nombreGen = rs.getString("Nombre_Generico");
                String farm = rs.getString("Farmaceutica");
                String gram = rs.getString("Gramaje");
                double precio = rs.getDouble("Precio_Venta");
                int stock = rs.getInt("Stock");
                byte[] foto = rs.getBytes("Foto_Producto");
                boolean activo = "S".equalsIgnoreCase(rs.getString("Activo"));

                Producto p = new Producto(id, nombre, nombreGen, farm, gram, precio, stock, foto, activo);
                productosMap.put(id, p);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + ex.getMessage());
        }
    }

    // ---------------------- INTERACCIÓN CATALOGO ----------------------
    // Refiltra productos por texto (nombre o nombreGen). Vacío = mostrar todos.
    private void aplicarFiltro(String q) {
        panelCatalogo.removeAll();
        for (Producto p : productosMap.values()) {
            if (q.isEmpty() || p.nombre.toLowerCase().contains(q) || (p.nombreGenerico != null && p.nombreGenerico.toLowerCase().contains(q))) {
                panelCatalogo.add(new ProductoCard(p));
            }
        }
        panelCatalogo.revalidate();
        panelCatalogo.repaint();
    }

    private void refrescarCatalogo() {
        aplicarFiltro("");
    }

    // Agrega el producto seleccionado por foco (el ultimo ProductCard con foco)
    // Para simplificar, usamos la selección visual: cada ProductCard tiene un botón "Agregar".
    private void agregarSeleccionadoAlCarrito() {
        // método auxiliar: no se usa directamente porque cada tarjeta ya ofrece agregar.
    }

    // ---------------------- CARRITO / FACTURA ----------------------
    private void agregarAlCarritoSeguro(int id, int cantidadToAdd) {
        Producto p = productosMap.get(id);
        if (p == null) return;
        int current = carritoMap.getOrDefault(id, 0);
        int nuevo = current + cantidadToAdd;
        if (nuevo > p.stock) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock disponible. Stock: " + p.stock);
            return;
        }
        carritoMap.put(id, nuevo);
        sincronizarTablaFactura();
    }

    private void quitarDelCarritoPorFilaSeleccionada() {
        int fila = tablaFactura.getSelectedRow();
        if (fila == -1) { JOptionPane.showMessageDialog(this, "Seleccione un item en la factura."); return; }
        String nombre = (String) modeloFactura.getValueAt(fila, 0);
        // Buscar el id por nombre (no ideal si nombres duplicados; alternativa: extender modelo para guardar id oculto)
        Integer idEncontrado = null;
        for (Map.Entry<Integer, Producto> e : productosMap.entrySet()) {
            if (e.getValue().nombre.equals(nombre)) { idEncontrado = e.getKey(); break; }
        }
        if (idEncontrado != null) {
            carritoMap.remove(idEncontrado);
            sincronizarTablaFactura();
        }
    }

    private void quitarSeleccionadoDelCarrito() {
        quitarDelCarritoPorFilaSeleccionada();
    }

    private void sincronizarTablaFactura() {
        // Mantener una sola verdad: carritoMap
        modeloFactura.setRowCount(0);
        double subtotal = 0;
        for (Map.Entry<Integer,Integer> e : carritoMap.entrySet()) {
            Producto p = productosMap.get(e.getKey());
            int qty = e.getValue();
            double total = p.precioVenta * qty;
            modeloFactura.addRow(new Object[]{p.nombre, p.precioVenta, qty, total});
            subtotal += total;
        }
        lblTotal.setText(String.format("Total: $%.2f", subtotal));
    }

    private void mostrarVentanaFactura() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== FACTURA =====\n");
        double total = 0;
        for (Map.Entry<Integer,Integer> e : carritoMap.entrySet()) {
            Producto p = productosMap.get(e.getKey());
            int qty = e.getValue();
            sb.append(p.nombre).append("	").append(qty).append("	$").append(p.precioVenta * qty).append("\n");
            total += p.precioVenta * qty;
        }
        sb.append("\nTOTAL: $").append(total).append("\n=================");
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Factura", JOptionPane.INFORMATION_MESSAGE);
    }

    private void ejecutarVenta() {
        if (carritoMap.isEmpty()) { JOptionPane.showMessageDialog(this, "El carrito está vacío"); return; }

        try (Connection conn = ConexionDB.obtenerConexion()) {
            if (conn == null) return;
            conn.setAutoCommit(false);

            // Insertar venta
            PreparedStatement psVenta = conn.prepareStatement(
                    "INSERT INTO Venta (Fecha, Id_Empleado) VALUES (DATE('now'), ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            psVenta.setInt(1, 1); // OJO: en producción pasar id empleado real
            psVenta.executeUpdate();
            ResultSet rsKeys = psVenta.getGeneratedKeys();
            int idVenta = rsKeys.next() ? rsKeys.getInt(1) : 0;
            psVenta.close();

            PreparedStatement psDetalle = conn.prepareStatement(
                    "INSERT INTO Detalle_Venta (Id_Venta, Id_Producto, Cantidad, Precio_Unitario) VALUES (?,?,?,?)"
            );

            for (Map.Entry<Integer,Integer> e : carritoMap.entrySet()) {
                int idProd = e.getKey();
                int cant = e.getValue();
                Producto p = productosMap.get(idProd);

                psDetalle.setInt(1, idVenta);
                psDetalle.setInt(2, idProd);
                psDetalle.setInt(3, cant);
                psDetalle.setDouble(4, p.precioVenta);
                psDetalle.addBatch();

                PreparedStatement psStock = conn.prepareStatement("UPDATE Producto SET Stock = Stock - ? WHERE Id_Producto = ?");
                psStock.setInt(1, cant);
                psStock.setInt(2, idProd);
                psStock.executeUpdate();
                psStock.close();
            }

            psDetalle.executeBatch();
            psDetalle.close();
            conn.commit();

            // Mostrar factura simple
            mostrarVentanaFactura();

            // limpiar carrito y refrescar datos
            carritoMap.clear();
            sincronizarTablaFactura();
            cargarProductosDesdeBD();
            refrescarCatalogo();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al registrar venta: " + ex.getMessage());
        }
    }

    // ---------------------- COMPONENTES AUXILIARES ----------------------
    // Tarjeta de producto con imagen, descripción, stock y botón "Agregar".
    private class ProductoCard extends JPanel {
        Producto producto;

        public ProductoCard(Producto p) {
            this.producto = p;
            setPreferredSize(new Dimension(180, 220));
            setLayout(new BorderLayout(6,6));
            setOpaque(true);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230,230,230)),
                    BorderFactory.createEmptyBorder(8,8,8,8)));

            // Imagen
            JLabel lblImg = new JLabel();
            lblImg.setHorizontalAlignment(SwingConstants.CENTER);
            lblImg.setPreferredSize(new Dimension(160, 100));
            if (p.foto != null) {
                try {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(p.foto));
                    Image scaled = img.getScaledInstance(140, 90, Image.SCALE_SMOOTH);
                    lblImg.setIcon(new ImageIcon(scaled));
                } catch (Exception ex) {
                    lblImg.setIcon(defaultPlaceholderIcon());
                }
            } else {
                lblImg.setIcon(defaultPlaceholderIcon());
            }
            add(lblImg, BorderLayout.NORTH);

            // Info
            String html = String.format("<html><div style='text-align:center'><b>%s</b><br><small>%s</small><br><i>%s</i></div></html>",
                    escapeHtml(p.nombre), p.nombreGenerico == null ? "" : escapeHtml(p.nombreGenerico), p.gramaje == null ? "" : escapeHtml(p.gramaje));
            JLabel lblInfo = new JLabel(html);
            lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
            lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            add(lblInfo, BorderLayout.CENTER);

            // Pie con precio, stock y botón
            JPanel pie = new JPanel(new BorderLayout());
            pie.setOpaque(false);

            JLabel lblPrecio = new JLabel(String.format("$%.2f", p.precioVenta));
            lblPrecio.setFont(FONT_BOLD);
            pie.add(lblPrecio, BorderLayout.WEST);

            JLabel lblStock = new JLabel("Stock: " + p.stock);
            lblStock.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            pie.add(lblStock, BorderLayout.CENTER);

            JButton btnAdd = makeIconButton("➕", e -> {
                String entrada = JOptionPane.showInputDialog(PanelProductos.this, "Cantidad:", "1");
                if (entrada == null) return;
                try {
                    int qty = Integer.parseInt(entrada);
                    if (qty <= 0) { JOptionPane.showMessageDialog(this, "Cantidad inválida"); return; }
                    agregarAlCarritoSeguro(p.id, qty);
                } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Ingrese un número válido"); }
            });
            btnAdd.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
            btnAdd.setToolTipText("Agregar al carrito");
            pie.add(btnAdd, BorderLayout.EAST);

            add(pie, BorderLayout.SOUTH);

            // Hover effect
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(new Color(245, 250, 255)); }
                public void mouseExited(MouseEvent e) { setBackground(Color.WHITE); }
            });
        }
    }

    private static Icon defaultPlaceholderIcon() {
        BufferedImage img = new BufferedImage(140, 90, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(245,245,245));
        g.fillRect(0,0,140,90);
        g.setColor(new Color(200,200,200));
        g.drawRect(10,10,120,70);
        g.dispose();
        return new ImageIcon(img);
    }

    // ---------------------- MODELO PRODUCTO SIMPLE ----------------------
    private static class Producto {
        int id;
        String nombre;
        String nombreGenerico;
        String farmaceutica;
        String gramaje;
        double precioVenta;
        int stock;
        byte[] foto;
        boolean activo;

        Producto(int id, String nombre, String nombreGenerico, String farmaceutica, String gramaje, double precioVenta, int stock, byte[] foto, boolean activo) {
            this.id = id; this.nombre = nombre; this.nombreGenerico = nombreGenerico; this.farmaceutica = farmaceutica; this.gramaje = gramaje; this.precioVenta = precioVenta; this.stock = stock; this.foto = foto; this.activo = activo;
        }
    }

    // ---------------------- SCROLLBAR UI MODERNA ----------------------
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        private final Color thumbColor = new Color(179, 215, 168); // verde pastel

      //  @Override protected void configureScrollBarColors() { thumb = thumbColor; }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            int arc = 14;
            RoundRectangle2D round = new RoundRectangle2D.Float(thumbBounds.x, thumbBounds.y, thumbBounds.width-2, thumbBounds.height, arc, arc);
            g2.fill(round);
            g2.dispose();
        }

        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(240,240,240));
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); return b; }
    }

    // ---------------------- UTIL ----------------------
    private static String escapeHtml(String s) { return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }

    // Layout 'wrap' sencillo que funciona mejor que GridLayout para tarjetas reponsive
    // (Minimal implementación - no dependencias externas)
    private static class ModifiedWrapLayout extends FlowLayout {
        public ModifiedWrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        public Dimension minimumLayoutSize(Container target) { return layoutSize(target, false); }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap*2);
                int x = 0, y = insets.top + vgap;
                int rowHeight = 0;
                for (Component comp : target.getComponents()) {
                    if (!comp.isVisible()) continue;
                    Dimension d = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                    if (x == 0 || x + d.width <= maxWidth) {
                        if (x > 0) x += hgap;
                        x += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    } else {
                        y += rowHeight + vgap;
                        x = d.width;
                        rowHeight = d.height;
                    }
                }
                y += rowHeight + vgap + insets.bottom;
                return new Dimension(targetWidth, y);
            }
        }
    }
}
