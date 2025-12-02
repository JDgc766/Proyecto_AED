package Proyecto_final;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


/**
 * PanelDetalleProducto
 * --------------------
 * JPanel dedicado a administrar productos (crear, editar, activar/desactivar).
 * - Muestra catálogo (incluye inactivos)
 * - Búsqueda en tiempo real
 * - Doble click en tarjeta para editar
 * - Crear nuevo producto con selección de imagen
 *
 * Integración: desde PanelProductos se puede abrir en un JDialog
 */
public class PanelDetalleProducto extends JPanel {

    private final Map<Integer, Producto> productosMap = new LinkedHashMap<>();
    private Map<String, Integer> categoriasMap = new LinkedHashMap<>();
    private final JPanel panelCatalogo = new JPanel();
    private final JScrollPane scrollCatalogo;
    private final JTextField txtBuscar = new JTextField(30);
    private JComboBox<String> comboFiltroCat;
    private static String mensaje;


    private final Color COLOR_FONDO = new Color(225, 245, 254);
    private static final Font FUENTE_GLOBAL = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FUENTE_TITULO = FUENTE_GLOBAL.deriveFont(Font.BOLD, 24f);
    private static final Font FUENTE_NEGRITA = FUENTE_GLOBAL.deriveFont(Font.BOLD, 16f);

    public PanelDetalleProducto() {
        setLayout(new BorderLayout(12,12));
        setBorder(new EmptyBorder(12,12,12,12));
        setBackground(COLOR_FONDO);
        setOpaque(true);

        // ---------- TOP: título + barra de búsqueda + botón nuevo ----------
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(12, 12, 20, 12));

        JLabel titulo = new JLabel("<html>Administración de<br> Productos");
        titulo.setFont(FUENTE_TITULO);
        titulo.setForeground(new Color(0, 100, 0));
        top.add(titulo, BorderLayout.WEST);

        // Panel derecha: búsqueda + botón nuevo dentro de un contenedor "moderno"
        JPanel panelDerechaTop = new JPanel();
        panelDerechaTop.setOpaque(false);
        panelDerechaTop.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        // Contenedor visual para la barra de búsqueda (parece un campo moderno)
        JPanel contBusqueda = new JPanel(new BorderLayout(6,0));
        contBusqueda.setOpaque(true);
        contBusqueda.setBackground(new Color(255,255,255,230));
        contBusqueda.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(170,170,170), 1, 14),
                new EmptyBorder(4,8,4,8)
        ));

        JLabel iconSearch = new JLabel("🔍");
        iconSearch.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        contBusqueda.add(iconSearch, BorderLayout.WEST);

        txtBuscar.setFont(FUENTE_GLOBAL);
        txtBuscar.setOpaque(false);
        txtBuscar.setBorder(null);
        txtBuscar.setPreferredSize(new Dimension(260, 28));
        txtBuscar.setToolTipText("Buscar producto (por nombre o nombre genérico)");
        colocarPlaceholder(txtBuscar, "Buscar producto...");
        contBusqueda.add(txtBuscar, BorderLayout.CENTER);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        JButton btnNuevo = new JButton("➕ Nuevo");
        btnNuevo.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        btnNuevo.setFocusPainted(false);
        btnNuevo.setBorderPainted(false);
        btnNuevo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNuevo.setBackground(new Color(0, 150, 136));
        btnNuevo.setForeground(Color.WHITE);
        btnNuevo.setOpaque(true);
        btnNuevo.setToolTipText("Nuevo(agregar produco)");
        btnNuevo.setBorder(new EmptyBorder(6,16,6,16));
        btnNuevo.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnNuevo.setBackground(new Color(0, 180, 160)); }
            @Override public void mouseExited(MouseEvent e) { btnNuevo.setBackground(new Color(0, 150, 136)); }
        });
        btnNuevo.addActionListener(e -> abrirEditorNuevo());

        panelDerechaTop.add(contBusqueda);
        
        comboFiltroCat = new JComboBox<>();
        comboFiltroCat.setFont(FUENTE_GLOBAL);
        comboFiltroCat.setUI(new ModernComboBoxUI());

        // cargar categorías existentes
        cargarCategorias();

        comboFiltroCat.addItem("Todas");  // opción por defecto

        for (String nombre : categoriasMap.keySet()) {
            comboFiltroCat.addItem(nombre);
        }

        comboFiltroCat.addActionListener(e -> filtrar());

        // agregar al panel
        panelDerechaTop.add(comboFiltroCat);
        
        panelDerechaTop.add(btnNuevo);

        top.add(panelDerechaTop, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ---------- CENTRO: catálogo de productos con scroll ----------
        panelCatalogo.setLayout(new ModifiedWrapLayout(FlowLayout.LEFT, 12, 12));
        panelCatalogo.setOpaque(false);

        scrollCatalogo = new JScrollPane(panelCatalogo,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCatalogo.setOpaque(false);
        scrollCatalogo.getViewport().setOpaque(false);
        scrollCatalogo.setBorder(BorderFactory.createEmptyBorder());
        scrollCatalogo.getVerticalScrollBar().setUnitIncrement(16);
        scrollCatalogo.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        add(scrollCatalogo, BorderLayout.CENTER);

        // Cargar datos iniciales
        cargarProductosDesdeBD(true); // incluyendo inactivos
        refrescarCatalogo();
    }
    
    
    // ESTILIECAR CATEGORIA
    
    private static class RoundedTextField extends JTextField {
        private final int arc = 14; // redondeado suave

        public RoundedTextField(String text, int columns) {
            super(text, columns);
            setOpaque(false);
            setBorder(new EmptyBorder(4, 8, 4, 8));  // borde fino
            setFont(FUENTE_GLOBAL);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fondo blanco suave
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(180, 180, 180));  // borde DELGADO
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

            g2.dispose();
        }
    }

    
    private static class ModernComboBoxUI extends javax.swing.plaf.basic.BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton arrow = new JButton("▼");
            arrow.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
            arrow.setBorder(null);
            arrow.setForeground(new Color(90, 90, 90));
            arrow.setBackground(Color.WHITE);
            arrow.setOpaque(true);
            arrow.setFocusPainted(false);
            return arrow;
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);

            JComboBox<?> combo = (JComboBox<?>) c;

            combo.setBackground(Color.WHITE);
            combo.setForeground(Color.BLACK);
            combo.setFocusable(false);

            combo.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(new Color(170,170,170), 1, 12),
                    new EmptyBorder(1,4,1,4)
            ));

            combo.setRenderer(new ModernComboRenderer());
        }

        @Override
        protected javax.swing.plaf.basic.ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane scroll = new JScrollPane(list);
                    scroll.setBorder(null);
                    scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
                    return scroll;
                }
            };
            popup.setBorder(new RoundedLineBorder(new Color(200,200,200), 1, 14));
            return popup;
        }
    }

    
    private static class ModernComboRenderer extends JLabel implements ListCellRenderer<Object> {

        ModernComboRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setBorder(new EmptyBorder(6,10,6,10));
        }

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            setText(value == null ? "" : value.toString());

            if (isSelected) {
                setBackground(new Color(0, 150, 136));   // turquesa del sistema
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
            }

            return this;
        }
    }

    
    /*private static class ModernScrollBarUI extends BasicScrollBarUI {

        private final Color thumbColor = new Color(179,215,168);

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(235,235,235));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0,0));
            b.setVisible(false);
            return b;
        }
    }*/


    
    // ======================= CARGAR CATEGORIAS ====================
    
    private void cargarCategorias() {
        categoriasMap.clear();
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement("SELECT Id_Categoria, Nombre FROM Categoria ORDER BY Nombre ASC");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categoriasMap.put(rs.getString("Nombre"), rs.getInt("Id_Categoria"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar categorías: " + ex.getMessage());
        }
    }

    // ======================= CARGA DE DATOS =======================
    private void cargarProductosDesdeBD(boolean includeInactivos) {
        productosMap.clear();
        String sql = "SELECT Id_Producto, Nombre, Nombre_Generico, Farmaceutica, Gramaje, Fecha_Caducidad, " +
                "Precio_Compra, Precio_Venta, Stock, Foto_Producto, Activo, StockMin, Id_Categoria FROM Producto" +
                (includeInactivos ? "" : " WHERE Activo='S'");
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("Id_Producto");
                String nombre = rs.getString("Nombre");
                String nombreGen = rs.getString("Nombre_Generico");
                String farm = rs.getString("Farmaceutica");
                String gram = rs.getString("Gramaje");
                String cad = rs.getString("Fecha_Caducidad");
                double precioC = rs.getDouble("Precio_Compra");
                double precio = rs.getDouble("Precio_Venta");
                int stock = rs.getInt("Stock");
                byte[] foto = rs.getBytes("Foto_Producto");
                boolean activo = "S".equalsIgnoreCase(rs.getString("Activo"));
                int stockmin = rs.getInt("StockMin");
                int idCat = rs.getInt("Id_Categoria");

                Producto p = new Producto(id,nombre,nombreGen,farm,gram,cad,precioC,precio,stock,foto,activo, stockmin, idCat);
                productosMap.put(id,p);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + ex.getMessage());
        }
    }

    private void refrescarCatalogo() {
    	panelCatalogo.removeAll();

        String q = ""; // filtro en blanco
        String categoria = "Todas"; 

        if (comboFiltroCat != null)
            comboFiltroCat.setSelectedItem("Todas");

        for (Producto p : productosMap.values()) {
            panelCatalogo.add(new ProductoCardEditable(p));
        }
        panelCatalogo.revalidate();
        panelCatalogo.repaint();
    }

    private void filtrar() {
    	String q = txtBuscar.getText().trim().toLowerCase();
        String categoriaSel = comboFiltroCat.getSelectedItem().toString();

        panelCatalogo.removeAll();

        for (Producto p : productosMap.values()) {

            boolean coincideTexto =
                    q.isEmpty()
                    || p.nombre.toLowerCase().contains(q)
                    || (p.nombreGenerico != null && p.nombreGenerico.toLowerCase().contains(q));

            boolean coincideCategoria =
                    categoriaSel.equals("Todas")
                    || categoriasMap.get(categoriaSel) == p.idCategoria;

            if (coincideTexto && coincideCategoria) {
                panelCatalogo.add(new ProductoCardEditable(p));
            }
        }

        panelCatalogo.revalidate();
        panelCatalogo.repaint();
        
     // ***** FIX DEL BUG DE DESAPARICIÓN *****
        SwingUtilities.invokeLater(() -> {
            scrollCatalogo.getViewport().revalidate();
            scrollCatalogo.getViewport().repaint();
        });
    }

    // ======================= EDITOR / NUEVO PRODUCTO =======================
    private void abrirEditorNuevo() {
        Producto nuevo = new Producto(0,"","", "", "","", 0.0, 0.0, 0, null, true, 0, 0);
        abrirEditor(nuevo, true);
    }

    private void abrirEditor(Producto p, boolean isNew) {
        Window owner = SwingUtilities.getWindowAncestor(this);

        JDialog dlg = new JDialog(
                owner,
                isNew ? "Nuevo Producto" : "Editar Producto",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dlg.setUndecorated(true);
        dlg.setSize(950, 765);
        dlg.setLocationRelativeTo(null);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout(10,10));
        content.setBorder(new EmptyBorder(15,15,15,15));
        content.setBackground(COLOR_FONDO);

        // Título grande tipo PanelContratarEmpleado
        /*JLabel lblTitulo = new JLabel(isNew ? "Nuevo producto" : "Editar producto", SwingConstants.CENTER);
        lblTitulo.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 20f));
        lblTitulo.setForeground(new Color(0, 100, 0));
        content.add(lblTitulo, BorderLayout.NORTH);*/

        // Formulario principal en panel redondeado
        JPanel panelFormWrap = new RoundedPanel(18, new Color(255,255,255,240));
        panelFormWrap.setLayout(new BorderLayout(10,10));
        panelFormWrap.setBorder(new EmptyBorder(12,12,12,12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        int row = 0;
        
        cargarCategorias();

        // Helpers de label y campo
        JLabel lblNombre = crearLabel("Nombre:");
        JTextField txtNombre = crearTextField(p.nombre);
        agregarCampo(form, gbc, row++, lblNombre, txtNombre);

        JLabel lblGen = crearLabel("Nombre genérico:");
        JTextField txtGen = crearTextField(p.nombreGenerico == null ? "" : p.nombreGenerico);
        agregarCampo(form, gbc, row++, lblGen, txtGen);

        JLabel lblFarm = crearLabel("Farmacéutica:");
        JTextField txtFarm = crearTextField(p.farmaceutica == null ? "" : p.farmaceutica);
        agregarCampo(form, gbc, row++, lblFarm, txtFarm);

        JLabel lblGram = crearLabel("Gramaje:");
        JTextField txtGram = crearTextField(p.gramaje == null ? "" : p.gramaje);
        agregarCampo(form, gbc, row++, lblGram, txtGram);
        
        // ** CATEGORIAS **
        
        JLabel lblCategoria = crearLabel("Categoría:");

        String[] nombresCat = new String[categoriasMap.size() + 1];
        int i = 0;
        for (String nombre : categoriasMap.keySet()) nombresCat[i++] = nombre;
        nombresCat[i] = "➕ Crear nueva categoría";

        JComboBox<String> comboCategoria = new JComboBox<>(nombresCat);
        comboCategoria.setFont(FUENTE_GLOBAL);
        comboCategoria.setUI(new ModernComboBoxUI());
        comboCategoria.setPreferredSize(new Dimension(260, 28));

        // Si está editando producto, seleccionar categoría actual
        if (!isNew && p.idCategoria != 0) {
            for (String nombre : categoriasMap.keySet()) {
                if (categoriasMap.get(nombre) == p.idCategoria) {
                    comboCategoria.setSelectedItem(nombre);
                    break;
                }
            }
        }

        agregarCampo(form, gbc, row++, lblCategoria, comboCategoria);

        // Evento para crear categorías nuevas
        comboCategoria.addActionListener(e -> {
            if (comboCategoria.getSelectedItem().equals("+ Crear nueva categoría")) {

                String nueva = JOptionPane.showInputDialog(dlg, "Nombre de la nueva categoría:");
                if (nueva == null || nueva.trim().isEmpty()) {
                    comboCategoria.setSelectedIndex(0);
                    return;
                }

                nueva = nueva.trim();

                try (Connection conn = ConexionDB.obtenerConexion();
                     PreparedStatement ps = conn.prepareStatement("INSERT INTO Categoria (Nombre) VALUES (?)")) {
                    ps.setString(1, nueva);
                    ps.executeUpdate();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "No se pudo crear la categoría: " + ex.getMessage());
                    return;
                }

                cargarCategorias();

                // reconstruir combo
                String[] nuevas = new String[categoriasMap.size() + 1];
                int k=0;
                for (String nombre : categoriasMap.keySet()) nuevas[k++] = nombre;
                nuevas[k] = "+ Crear nueva categoría";

                comboCategoria.setModel(new DefaultComboBoxModel<>(nuevas));
                comboCategoria.setSelectedItem(nueva);
            }
        });
        
        // ** FIN CATEGORIA **
        
        JLabel lblCad = crearLabel("Fecha de caducidad:");
        JTextField txtCad = crearTextField(p.gramaje == null ? "" : p.caducidad);
        agregarCampo(form, gbc, row++, lblCad, txtCad);
        
        JLabel lblPrecioC = crearLabel("Precio Compra:");
        JTextField txtPrecioC = crearTextField(String.valueOf(p.precioCompra));
        agregarCampo(form, gbc, row++, lblPrecioC, txtPrecioC);

        JLabel lblPrecio = crearLabel("Precio:");
        JTextField txtPrecio = crearTextField(String.valueOf(p.precioVenta));
        agregarCampo(form, gbc, row++, lblPrecio, txtPrecio);
        
        JLabel lblStockMin = crearLabel("Stock Minimo:");
        JTextField txtStockMin = crearTextField(String.valueOf(p.stockmin));
        agregarCampo(form, gbc, row++, lblStockMin, txtStockMin);

        JLabel lblStock = crearLabel("Stock:");
        JTextField txtStock = crearTextField(String.valueOf(p.stock));
        agregarCampo(form, gbc, row++, lblStock, txtStock);

        JLabel lblActivo = crearLabel("Estado:");
        JCheckBox chkActivo = new JCheckBox("Activo");
        chkActivo.setSelected(p.activo);
        chkActivo.setOpaque(false);
        chkActivo.setFont(FUENTE_GLOBAL);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(lblActivo, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(chkActivo, gbc);
        row++;

        // Imagen
        JLabel lblImgTitle = crearLabel("Imagen:");
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(lblImgTitle, gbc);

        JPanel imgPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        imgPane.setOpaque(false);
        JLabel lblImg = new JLabel();
        lblImg.setPreferredSize(new Dimension(140,90));

        if (p.foto != null) {
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(p.foto));
                lblImg.setIcon(new ImageIcon(img.getScaledInstance(140,90,Image.SCALE_SMOOTH)));
            } catch (Exception ex) {
                lblImg.setIcon(defaultPlaceholderIcon());
            }
        } else {
            lblImg.setIcon(defaultPlaceholderIcon());
        }

        JButton btnCambiar = new JButton("Seleccionar foto");
        btnCambiar.setFont(FUENTE_GLOBAL);
        btnCambiar.setFocusPainted(false);
        btnCambiar.setBorderPainted(false);
        btnCambiar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCambiar.setBackground(new Color(0, 150, 136));
        btnCambiar.setForeground(Color.WHITE);
        btnCambiar.setOpaque(true);
        btnCambiar.setBorder(new EmptyBorder(6,12,6,12));

        final byte[][] imgBytes = new byte[1][];
        btnCambiar.addActionListener(ev -> {
        	 try {
        	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	    } catch (Exception ex) {}

        	    JFileChooser chooser = new JFileChooser();
        	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        	    chooser.setAcceptAllFileFilterUsed(false);
        	    chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "bmp", "gif"));

        	    int res = chooser.showOpenDialog(this);
        	    if (res == JFileChooser.APPROVE_OPTION) {

        	        File archivo = chooser.getSelectedFile();

        	        try {
        	            BufferedImage imgOriginal = ImageIO.read(archivo);

        	            // === ESCALAR IMAGEN SI ES MUY GRANDE ===
        	            int maxDim = 800;
        	            int newW = imgOriginal.getWidth();
        	            int newH = imgOriginal.getHeight();

        	            if (newW > maxDim || newH > maxDim) {
        	                double scale = Math.min((double) maxDim / newW, (double) maxDim / newH);
        	                newW = (int)(newW * scale);
        	                newH = (int)(newH * scale);
        	            }

        	            BufferedImage imgEscalada = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        	            Graphics2D g2 = imgEscalada.createGraphics();
        	            g2.setColor(Color.WHITE);
        	            g2.fillRect(0, 0, newW, newH);
        	            g2.drawImage(imgOriginal.getScaledInstance(newW, newH, Image.SCALE_SMOOTH), 0, 0, null);
        	            g2.dispose();

        	            // === GUARDAR COMO BYTE[] ===
        	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	            ImageIO.write(imgEscalada, "png", baos);
        	            baos.flush();
        	            imgBytes[0] = baos.toByteArray();
        	            baos.close();

        	            // === PREVIEW MODERNO ===
        	            lblImg.setIcon(new ImageIcon(imgEscalada.getScaledInstance(140, 90, Image.SCALE_SMOOTH)));

        	        } catch (Exception ex) {
        	            JOptionPane.showMessageDialog(this, "Error al cargar la imagen.");
        	        }
        	    }
        });

        imgPane.add(lblImg);
        imgPane.add(btnCambiar);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        form.add(imgPane, gbc);

        panelFormWrap.add(form, BorderLayout.CENTER);
        content.add(panelFormWrap, BorderLayout.CENTER);

        // Botones inferior
        JPanel buts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        buts.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        estilizarBotonSecundario(btnCancelar);
        btnCancelar.addActionListener(ev -> dlg.dispose());

        JButton btnGuardar = new JButton(isNew ? "Crear" : "Guardar");
        estilizarBotonPrincipal(btnGuardar);
        btnGuardar.addActionListener(ev -> {
            try {
                String nombreN = txtNombre.getText().trim();
                double precioCN = Double.parseDouble(txtPrecioC.getText().trim());
                double precioN = Double.parseDouble(txtPrecio.getText().trim());
                int stockN = Integer.parseInt(txtStock.getText().trim());
                int stockminN = Integer.parseInt(txtStockMin.getText().trim()); 
                String genN = txtGen.getText().trim();
                String farmN = txtFarm.getText().trim();
                String gramN = txtGram.getText().trim();
                String cadN = txtCad.getText().trim();
                boolean activoN = chkActivo.isSelected();

                if (nombreN.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "El nombre es requerido");
                    return;
                }
                
             // === VALIDAR FECHA DE CADUCIDAD ===
                try {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    LocalDate fechaCad = LocalDate.parse(cadN, fmt);
                    LocalDate hoy = LocalDate.now();

                    if (fechaCad.isBefore(hoy)) {
                        JOptionPane.showMessageDialog(dlg,
                                "La fecha de caducidad no puede ser pasada.\nIngrese una fecha válida.",
                                "Fecha inválida",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(dlg,
                            "Formato de fecha inválido.\nUse formato: yyyy-MM-dd",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
             // === VALIDAR PRECIO DE COMPRA < PRECIO DE VENTA ===
                if (precioN < precioCN) {
                    JOptionPane.showMessageDialog(dlg,
                            "El precio de venta no puede ser menor al precio de compra.",
                            "Precio inválido",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
             // === VALIDAR STOCK NO PUEDA SER MENOR AL STOCK MÍNIMO ===
                if (stockN < stockminN) {
                    JOptionPane.showMessageDialog(dlg,
                            "El stock no puede ser menor al stock mínimo.",
                            "Stock inválido",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                String catNombre = comboCategoria.getSelectedItem().toString();
                int idCategoria = categoriasMap.getOrDefault(catNombre, 0);
                
                boolean act = p.activo;

                if (isNew) {
                    try (Connection conn = ConexionDB.obtenerConexion();
                         PreparedStatement ps = conn.prepareStatement(
                                 "INSERT INTO Producto (Nombre, Nombre_Generico, Farmaceutica, Gramaje, Fecha_Caducidad, Precio_Compra, Precio_Venta, Stock, Foto_Producto, Activo, StockMin, Id_Categoria) " +
                                         "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")) {
                        ps.setString(1, nombreN);
                        ps.setString(2, genN);
                        ps.setString(3, farmN);
                        ps.setString(4, gramN);
                        ps.setString(5, cadN);
                        ps.setDouble(6, precioCN);
                        ps.setDouble(7, precioN);
                        ps.setInt(8, stockN);
                        if (imgBytes[0] != null) ps.setBytes(9, imgBytes[0]); else ps.setNull(9, java.sql.Types.BLOB);
                        ps.setString(10, activoN ? "S" : "N");
                        ps.setInt(11, stockminN);
                        ps.setInt(12, idCategoria);
                        ps.executeUpdate();
                        
                        mensaje = "Se ha añadido el producto " + nombreN;
                        NotificacionManager.agregarNotificacion("PRODUCTO", mensaje);
                        
                        if(activoN == true) {
                        	mensaje = "Se ha habilitado el producto " + nombreN;
                        	NotificacionManager.agregarNotificacion("PRODUCTO", mensaje);
                        }else {
                        	mensaje = "Se ha deshabilitado el producto " + nombreN;
                        	NotificacionManager.agregarNotificacion("PRODUCTO", mensaje);
                        }
                    }
                } else {
                    try (Connection conn = ConexionDB.obtenerConexion();
                         PreparedStatement ps = conn.prepareStatement(
                                 "UPDATE Producto SET Nombre=?, Nombre_Generico=?, Farmaceutica=?, Gramaje=?, Fecha_Caducidad=?, Precio_Compra=?, Precio_Venta=?, Stock=?, Activo=?, StockMin=?, Id_Categoria=? WHERE Id_Producto=?")) {
                        ps.setString(1, nombreN);
                        ps.setString(2, genN);
                        ps.setString(3, farmN);
                        ps.setString(4, gramN);
                        ps.setString(5, cadN);
                        ps.setDouble(6, precioCN);
                        ps.setDouble(7, precioN);
                        ps.setInt(8, stockN);
                        ps.setString(9, activoN ? "S" : "N");
                        ps.setInt(10, stockminN);
                        ps.setInt(11, idCategoria);
                        ps.setInt(12, p.id);
                        ps.executeUpdate();
                        
                        if (activoN != act) {
                            if (activoN) {
                                mensaje = "Se ha habilitado el producto " + nombreN;
                            } else {
                                mensaje = "Se ha deshabilitado el producto " + nombreN;
                            }
                            NotificacionManager.agregarNotificacion("PRODUCTO", mensaje);
                        }
                    }
                    if (imgBytes[0] != null) {
                        try (Connection conn = ConexionDB.obtenerConexion();
                             PreparedStatement ps2 = conn.prepareStatement("UPDATE Producto SET Foto_Producto = ? WHERE Id_Producto = ?")) {
                            ps2.setBytes(1, imgBytes[0]);
                            ps2.setInt(2, p.id);
                            ps2.executeUpdate();
                        }
                    }
                }

                cargarProductosDesdeBD(true);
                refrescarCatalogo();
                dlg.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Precio o stock inválido");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error al guardar: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        buts.add(btnCancelar);
        buts.add(btnGuardar);
        content.add(buts, BorderLayout.SOUTH);

        dlg.getContentPane().add(content);
        dlg.setVisible(true);
        
        cargarProductosDesdeBD(true); // incluyendo inactivos
        refrescarCatalogo();
    }

    // ======================= TARJETA EDITABLE =======================
    private class ProductoCardEditable extends JPanel {
        Producto producto;
        public ProductoCardEditable(Producto p) {
            this.producto = p;
            setPreferredSize(new Dimension(190,230));
            setLayout(new BorderLayout(6,6));
            setOpaque(false);
            setBorder(new EmptyBorder(4,4,4,4));

            // Panel interno redondeado
            JPanel inner = new RoundedPanel(14, new Color(255,255,255,240));
            inner.setLayout(new BorderLayout(6,6));
            inner.setBorder(new EmptyBorder(8,8,8,8));

            JLabel lblImg = new JLabel();
            lblImg.setHorizontalAlignment(SwingConstants.CENTER);
            lblImg.setPreferredSize(new Dimension(160,100));
            if (p.foto != null) {
                try {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(p.foto));
                    lblImg.setIcon(new ImageIcon(img.getScaledInstance(140,90,Image.SCALE_SMOOTH)));
                } catch (Exception ex) {
                    lblImg.setIcon(defaultPlaceholderIcon());
                }
            } else {
                lblImg.setIcon(defaultPlaceholderIcon());
            }
            inner.add(lblImg, BorderLayout.NORTH);

            String html = String.format(
                    "<html><div style='text-align:center'><b>%s</b><br><small>%s</small><br><i>%s</i></div></html>",
                    escapeHtml(p.nombre),
                    p.nombreGenerico == null ? "" : escapeHtml(p.nombreGenerico),
                    p.gramaje == null ? "" : escapeHtml(p.gramaje)
            );
            JLabel lblInfo = new JLabel(html);
            lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
            lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            inner.add(lblInfo, BorderLayout.CENTER);

            JPanel pie = new JPanel(new BorderLayout());
            pie.setOpaque(false);
            JLabel lblPrecio = new JLabel(String.format("$%.2f", p.precioVenta));
            lblPrecio.setFont(FUENTE_NEGRITA);
            pie.add(lblPrecio, BorderLayout.WEST);
            JLabel lblStock = new JLabel("Stock: "+p.stock);
            lblStock.setFont(FUENTE_GLOBAL.deriveFont(13f));
            pie.add(lblStock, BorderLayout.CENTER);

            JButton btnToggle = new JButton(p.activo ? "Desactivar" : "Activar");
            btnToggle.setFont(FUENTE_GLOBAL.deriveFont(12f));
            btnToggle.setFocusPainted(false);
            btnToggle.setBorderPainted(false);
            btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnToggle.setOpaque(false);
            btnToggle.setContentAreaFilled(false);
            btnToggle.addActionListener(e -> toggleActivo(producto));
            pie.add(btnToggle, BorderLayout.EAST);

            inner.add(pie, BorderLayout.SOUTH);
            add(inner, BorderLayout.CENTER);

            // doble click para editar
            inner.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) abrirEditor(producto, false);
                }
                @Override public void mouseEntered(MouseEvent e) { inner.setBackground(new Color(245, 250, 255)); }
                @Override public void mouseExited(MouseEvent e) { inner.setBackground(new Color(255,255,255,240)); }
            });
        }
    }

    private void toggleActivo(Producto p) {
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement("UPDATE Producto SET Activo = ? WHERE Id_Producto = ?")) {
            ps.setString(1, p.activo ? "N" : "S");
            ps.setInt(2, p.id);
            ps.executeUpdate();
            p.activo = !p.activo;
            cargarProductosDesdeBD(true);
            refrescarCatalogo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cambiar estado: " + ex.getMessage());
        }
    }

    // ======================= UTILIDADES VISUALES Y MODELO =======================
    private static Icon defaultPlaceholderIcon() {
        BufferedImage img = new BufferedImage(140,90,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(245,245,245));
        g.fillRect(0,0,140,90);
        g.setColor(new Color(200,200,200));
        g.drawRect(10,10,120,70);
        g.dispose();
        return new ImageIcon(img);
    }

    private static String escapeHtml(String s) {
        return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private static class Producto {
        int id;
        String nombre;
        String nombreGenerico;
        String farmaceutica;
        String gramaje;
        String caducidad;
        double precioCompra;
        double precioVenta;
        int stock;
        byte[] foto;
        boolean activo;
        int stockmin;
        int idCategoria;
        Producto(int id,String n,String ng,String f,String g,String cad,double pc,double p,int s,byte[] ft,boolean a, int smin, int idCategoria){
            this.id=id; this.nombre=n; this.nombreGenerico=ng; this.farmaceutica=f;
            this.gramaje=g;this.caducidad=cad; this.precioCompra = pc; this.precioVenta=p; this.stock=s; this.foto=ft; this.activo=a; this.stockmin = smin; this.idCategoria = idCategoria;
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        private final Color thumbColor = new Color(179,215,168);
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            int arc=12;
            RoundRectangle2D r=new RoundRectangle2D.Float(
                    thumbBounds.x, thumbBounds.y, thumbBounds.width-2, thumbBounds.height, arc, arc);
            g2.fill(r);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g,JComponent c,Rectangle trackBounds) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setColor(new Color(240,240,240));
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
        @Override protected JButton createDecreaseButton(int orientation){ return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation){ return createZeroButton(); }
        private JButton createZeroButton(){
            JButton b=new JButton();
            b.setPreferredSize(new Dimension(0,0));
            b.setMinimumSize(new Dimension(0,0));
            b.setMaximumSize(new Dimension(0,0));
            return b;
        }
    }

    private static class ModifiedWrapLayout extends FlowLayout {
        public ModifiedWrapLayout(int align,int hgap,int vgap){ super(align,hgap,vgap);}
        public Dimension preferredLayoutSize(Container target){ return layoutSize(target,true);}
        public Dimension minimumLayoutSize(Container target){ return layoutSize(target,false);}
        private Dimension layoutSize(Container target, boolean preferred){
            synchronized(target.getTreeLock()){
                int targetWidth = target.getWidth();
                if (targetWidth<=0){
                    Container parent=target.getParent();
                    if (parent!=null) targetWidth=parent.getWidth();
                    if (targetWidth<=0) targetWidth=Integer.MAX_VALUE/2;
                }
                int hgap=getHgap(), vgap=getVgap();
                Insets insets=target.getInsets();
                int maxWidth=targetWidth - (insets.left + insets.right + hgap*2);
                int x=0, y=insets.top + vgap;
                int rowHeight=0;
                for (Component comp: target.getComponents()){
                    if (!comp.isVisible()) continue;
                    Dimension d = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                    if (x==0 || x + d.width <= maxWidth){
                        if (x>0) x += hgap;
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

    // Panel redondeado genérico
    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color bg;
        RoundedPanel(int arc, Color bg) {
            this.arc = arc;
            this.bg = bg;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Borde redondeado simple para el contenedor de búsqueda
    private static class RoundedLineBorder extends javax.swing.border.LineBorder {
        private final int radius;
        public RoundedLineBorder(Color color, int thickness, int radius) {
            super(color, thickness, true);
            this.radius = radius;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(lineColor);
            g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
            g2.dispose();
        }
    }

    // Helpers visuales
    private static void colocarPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(new Color(150,150,150));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(150,150,150));
                }
            }
        });
    }

    private static JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(FUENTE_NEGRITA);
        return lbl;
    }

    private static JTextField crearTextField(String inicial) {
        RoundedTextField tf = new RoundedTextField(inicial, 25);
        tf.setPreferredSize(new Dimension(260, 30));
        return tf;
    }


    private static void agregarCampo(JPanel form, GridBagConstraints gbc, int row, JComponent label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(field, gbc);
    }
    
 // ------------------------ ESTILOS DE BOTONES ------------------------
    private void estilizarBotonPrincipal(JButton btn) {
        btn.setFont(FUENTE_GLOBAL.deriveFont(Font.BOLD, 15f));
        btn.setBackground(new Color(0, 150, 136));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0, 180, 160));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0, 150, 136));
            }
        });
    }
    
 

    
    

    private void estilizarBotonSecundario(JButton btn) {
        btn.setFont(FUENTE_GLOBAL.deriveFont(15f));
        btn.setBackground(new Color(240,240,240));
        btn.setForeground(Color.BLACK);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(225,225,225));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(240,240,240));
            }
        });
    }

}
