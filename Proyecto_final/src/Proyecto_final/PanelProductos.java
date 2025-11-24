package Proyecto_final;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
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

    // === MODELO DE DATOS ===
    private final Map<Integer, Producto> productosMap = new LinkedHashMap<>();
    private final Map<Integer, Integer> carritoMap = new LinkedHashMap<>();

    // === COMPONENTES UI ===
    private final JPanel panelCatalogo = new JPanel();
    private final JScrollPane scrollCatalogo;

    private final DefaultTableModel modeloFactura;
    private final JTable tablaFactura;
    private final JLabel lblTotal = new JLabel("Total: $0.00");

    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    // para hover en tabla
    private int hoverRow = -1;

    public PanelProductos() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(250, 252, 253));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);
        JPanel busquedaPane = crearPanelBusqueda();
        busquedaPane.setOpaque(false);
        top.add(busquedaPane, BorderLayout.WEST);

        JPanel botonesPane = crearPanelAccionesRapidas();
        botonesPane.setOpaque(false);
        top.add(botonesPane, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // catálogo: usar ModifiedWrapLayout corregido
        panelCatalogo.setLayout(new ModifiedWrapLayout(FlowLayout.LEFT, 12, 12));
        panelCatalogo.setOpaque(false);

        scrollCatalogo = new JScrollPane(panelCatalogo,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        scrollCatalogo.setOpaque(false);
        scrollCatalogo.getViewport().setOpaque(false);
        scrollCatalogo.getVerticalScrollBar().setOpaque(false);
        scrollCatalogo.getHorizontalScrollBar().setOpaque(false);
        
        scrollCatalogo.setBorder(BorderFactory.createEmptyBorder());
        scrollCatalogo.getVerticalScrollBar().setUnitIncrement(16);
        scrollCatalogo.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        // evitar que el catalogo se expanda horizontalmente
        panelCatalogo.setPreferredSize(null);
        add(scrollCatalogo, BorderLayout.CENTER);

        // tabla factura
        modeloFactura = new DefaultTableModel(new Object[]{"Nombre", "Precio", "Cant.", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaFactura = new JTable(modeloFactura);
        tablaFactura.setFont(FONT_NORMAL);
        tablaFactura.setRowHeight(30);
        tablaFactura.getTableHeader().setFont(FONT_BOLD);

        estilizarTablaFactura();

        JScrollPane scrollFactura = new JScrollPane(tablaFactura);
        scrollFactura.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        scrollFactura.setPreferredSize(new Dimension(100, 140)); // altura reducida y fija

        // panel sur con tabla en panel redondeado
        JPanel south = new JPanel(new BorderLayout(8,8));
        south.setOpaque(false);

        JPanel tablaPanelRedondeada = new RoundedPanel(16, Color.WHITE);
        tablaPanelRedondeada.setLayout(new BorderLayout());
        tablaPanelRedondeada.setBorder(new EmptyBorder(8,8,8,8));
        tablaPanelRedondeada.add(scrollFactura, BorderLayout.CENTER);

        south.add(tablaPanelRedondeada, BorderLayout.CENTER);

        JPanel rightSouth = new JPanel(new BorderLayout(8,8));
        rightSouth.setOpaque(false);
        lblTotal.setFont(FONT_BOLD);
        rightSouth.add(lblTotal, BorderLayout.NORTH);

        JPanel botonesFactura = new JPanel();
        botonesFactura.setOpaque(false);
        botonesFactura.setLayout(new GridLayout(3,1,10,10));
        botonesFactura.add(makeBigButton("🛒", "Agregar", e -> agregarSeleccionadoAlCarrito()));
        botonesFactura.add(makeBigButton("❌", "Quitar", e -> quitarSeleccionadoDelCarrito()));
        botonesFactura.add(makeBigButton("💰", "Vender", e -> ejecutarVenta()));
        rightSouth.add(botonesFactura, BorderLayout.CENTER);

        south.add(rightSouth, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        // Carga inicial
        cargarProductosDesdeBD();
        refrescarCatalogo();

        // listeners para hover en tabla
        tablaFactura.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tablaFactura.rowAtPoint(e.getPoint());
                if (row != hoverRow) { hoverRow = row; tablaFactura.repaint(); }
            }
        });
        tablaFactura.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) { hoverRow = -1; tablaFactura.repaint(); }
        });
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

    // botón grande y estilizado para la derecha
    private JButton makeBigButton(String icon, String tooltip, ActionListener l) {
        JButton b = new JButton(icon);
        b.setToolTipText(tooltip);
        b.setFont(new Font("Segoe UI Symbol", Font.BOLD, 34));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBackground(new Color(232, 247, 233));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(88, 64));
        b.addActionListener(l);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(200,230,200)); }
            public void mouseExited(MouseEvent e) { b.setBackground(new Color(232,247,233)); }
        });
        // rounded appearance
        b.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
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

    private void agregarSeleccionadoAlCarrito() {
        // helper no usado: las tarjetas tienen su propio botón agregado
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
        Integer idEncontrado = null;
        for (Map.Entry<Integer, Producto> e : productosMap.entrySet()) {
            if (e.getValue().nombre.equals(nombre)) { idEncontrado = e.getKey(); break; }
        }
        if (idEncontrado != null) {
            carritoMap.remove(idEncontrado);
            sincronizarTablaFactura();
        }
    }

    private void quitarSeleccionadoDelCarrito() { quitarDelCarritoPorFilaSeleccionada(); }

    private void sincronizarTablaFactura() {
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
            sb.append(p.nombre).append("	").append(qty).append("	$").append(p.precioVenta * qty).append("");
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

            PreparedStatement psVenta = conn.prepareStatement(
                    "INSERT INTO Venta (Fecha, Id_Empleado) VALUES (DATE('now'), ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            psVenta.setInt(1, 1);
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

            mostrarVentanaFactura();

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

            // sombra ligera
            setOpaque(false);

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
            btnAdd.setBackground(new Color(232,247,233));
            btnAdd.setOpaque(true);
            btnAdd.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
            pie.add(btnAdd, BorderLayout.EAST);

            add(pie, BorderLayout.SOUTH);

            // Hover effect
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBackground(new Color(245, 250, 255)); }
                public void mouseExited(MouseEvent e) { setBackground(Color.WHITE); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            // dibujar fondo redondeado con sombra ligera
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 12;
            g2.setColor(new Color(255,255,255));
            g2.fillRoundRect(0, 4, getWidth(), getHeight()-6, arc, arc);
            g2.setColor(new Color(230,230,230));
            g2.drawRoundRect(0, 4, getWidth()-1, getHeight()-7, arc, arc);
            g2.dispose();
            super.paintComponent(g);
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

    // Panel redondeado reusable
    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color backgroundColor;
        RoundedPanel(int arc, Color bg) { this.arc = arc; this.backgroundColor = bg; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------------------- UTIL - ESTILO TABLA ----------------------
    private void estilizarTablaFactura() {
        tablaFactura.setShowGrid(false);
        tablaFactura.setIntercellSpacing(new Dimension(0,0));
        tablaFactura.setFillsViewportHeight(true);
        tablaFactura.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // header
        JTableHeader header = tablaFactura.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer(header.getDefaultRenderer()));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setOpaque(false);

        // renderer para filas
        tablaFactura.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(200, 230, 200));
                    c.setForeground(Color.BLACK);
                } else if (row == hoverRow) {
                    c.setBackground(new Color(240, 255, 245));
                    c.setForeground(Color.BLACK);
                } else {
                    if (row % 2 == 0) c.setBackground(new Color(250, 255, 250));
                    else c.setBackground(new Color(244, 250, 244));
                    c.setForeground(new Color(30,60,40));
                }
                setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
                return c;
            }
        });
    }

    private static class HeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer delegate;
        HeaderRenderer(TableCellRenderer del) { delegate = del; }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setBackground(new Color(235,250,235));
            c.setForeground(new Color(30,80,40));
            c.setFont(FONT_BOLD);
            if (c instanceof JComponent) ((JComponent)c).setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
            return c;
        }
    }

    private static String escapeHtml(String s) { return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }

    // Layout 'wrap' corregido que funciona mejor en JScrollPane
    private static class ModifiedWrapLayout extends FlowLayout {
        public ModifiedWrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
        public Dimension minimumLayoutSize(Container target) { return layoutSize(target, false); }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth <= 0) {
                    Container parent = target.getParent();
                    if (parent != null) targetWidth = parent.getWidth();
                    if (targetWidth <= 0) targetWidth = Integer.MAX_VALUE/2;
                }
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
