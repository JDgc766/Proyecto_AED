package Proyecto_final;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/*import Proyecto_final.PanelDetalleProducto.ModernComboRenderer;
import Proyecto_final.PanelDetalleProducto.ModernScrollBarUI;
import Proyecto_final.PanelDetalleProducto.RoundedLineBorder;

/*import Proyecto_final.PanelDetalleProducto.ModernComboBoxUI;

import Proyecto_final.PanelDetalleProducto.ModernComboRenderer;
import Proyecto_final.PanelDetalleProducto.ModernScrollBarUI;
import Proyecto_final.PanelDetalleProducto.RoundedLineBorder;*/

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class PanelProductos extends JPanel {
	
	private static final String NOMBRE_FARMACIA = "Farmacia Jalinas 2";
	private static final String DIRECCION_FARMACIA = "Sector del Mayoreo, Managua, Nicaragua";
	private static final String TELEFONO_FARMACIA = "Tel: 8888-8888";
	private static final String RUC_FARMACIA = "RUC: 001-160998-0000X";
	private static final String LOGO_PATH = "/imagenes/f.jpg";


    // === MODELO DE DATOS ===
    private final Map<Integer, Producto> productosMap = new LinkedHashMap<>();
    private final Map<Integer, Integer> carritoMap = new LinkedHashMap<>();
    private final Map<String, Integer> categoriasMap = new LinkedHashMap<>();
    private JComboBox<String> comboFiltroCat;


    // === COMPONENTES UI ===
    private final JPanel panelCatalogo = new JPanel();
    private final JScrollPane scrollCatalogo;
    
    private static String mensaje;

    private final DefaultTableModel modeloFactura;
    private final JTable tablaFactura;
    private final JLabel lblTotal = new JLabel("Total: $0.00");
    private final JTextField txtBuscar = new JTextField(30);

    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    // para hover en tabla
    private int hoverRow = -1;
    
    private String rolUsuario;
    private int idEmpleado;
    private String nombreEmpleado;
    
    private static boolean bloqueoFiltro = false;
    


    public PanelProductos(String rol, int idempleado, String nombre) {
    	this.rolUsuario = rol;
    	this.idEmpleado = idempleado;
    	this.nombreEmpleado = nombre;
    	
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
        //botonesFactura.add(makeBigButton("🛒", "Agregar", e -> agregarSeleccionadoAlCarrito()));
        botonesFactura.add(makeBigButton("❌", "Quitar", e -> quitarSeleccionadoDelCarrito()));
        botonesFactura.add(makeBigButton("💰", "Vender", e -> ejecutarVenta()));
        rightSouth.add(botonesFactura, BorderLayout.CENTER);

        south.add(rightSouth, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        // Carga inicial
        cargarProductosDesdeBD();
        filtrarCatalogo();
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
    
    /*private boolean coincideCategoriaProducto(Producto p, String categoriaSel) {
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT Id_Categoria FROM Producto WHERE Id_Producto = ?")
        ) {
            ps.setInt(1, p.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int idCat = rs.getInt(1);
                return categoriasMap.get(categoriaSel) == idCat;
            }
        } catch (Exception ignored) { }

        return false;
    }*/

    
    private void filtrarCatalogo() {
        String q = txtBuscar.getText().trim().toLowerCase();
        String categoriaSel = comboFiltroCat.getSelectedItem().toString();

        panelCatalogo.removeAll();

        /*Integer idCatSeleccionada =
                categoriaSel.equals("Todas") ? null : categoriasMap.get(categoriaSel);*/

        for (Producto p : productosMap.values()) {

        	 boolean coincideTexto =
                     q.isEmpty()
                     || p.nombre.toLowerCase().contains(q)
                     || (p.nombreGenerico != null && p.nombreGenerico.toLowerCase().contains(q));

             boolean coincideCategoria =
                     categoriaSel.equals("Todas")
                     || categoriasMap.get(categoriaSel) == p.idCategoria;

            if (coincideCategoria && coincideTexto) {
                panelCatalogo.add(new ProductoCard(p));
            }
        }

        panelCatalogo.revalidate();
        panelCatalogo.repaint();

        SwingUtilities.invokeLater(() -> {
            scrollCatalogo.getViewport().revalidate();
            scrollCatalogo.getViewport().repaint();
        });
    }



    private void ejecutarFiltrado() {
        if (bloqueoFiltro) return;   // 🔥 evita refrescos cuando NO toca
        filtrarCatalogo();
    }
    
    // ---------------------- CREACIÓN DE UI ----------------------
    private JPanel crearPanelBusqueda() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        p.setOpaque(false);
        cargarCategorias(); 

        // ===== CONTENEDOR MODERNO =====
        JPanel contBusqueda = new JPanel(new BorderLayout(6, 0));
        contBusqueda.setOpaque(true);
        contBusqueda.setBackground(new Color(255, 255, 255, 230));
        contBusqueda.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(170,170,170), 1, 14),
                new EmptyBorder(4, 8, 4, 8)
        ));

        JLabel iconSearch = new JLabel("🔍");
        iconSearch.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        contBusqueda.add(iconSearch, BorderLayout.WEST);

        
        txtBuscar.setFont(FONT_NORMAL.deriveFont(16f));
        txtBuscar.setOpaque(false);
        txtBuscar.setBorder(null);
        txtBuscar.setPreferredSize(new Dimension(260, 28));
        //colocarPlaceholder(txtBuscar, "Buscar producto...");
        contBusqueda.add(txtBuscar, BorderLayout.CENTER);

        // EVENTO DE FILTRADO
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
        	public void insertUpdate(DocumentEvent e) { ejecutarFiltrado(); }
            public void removeUpdate(DocumentEvent e) { ejecutarFiltrado(); }
            public void changedUpdate(DocumentEvent e) { ejecutarFiltrado(); }
        });       
        p.add(contBusqueda);

        // ==========================
        //     FILTRO DE CATEGORÍA
        // ==========================
        comboFiltroCat = new JComboBox<>();
        comboFiltroCat.setFont(FONT_NORMAL.deriveFont(15f));
        comboFiltroCat.setUI(new ModernComboBoxUI());

        // estilo moderno
        comboFiltroCat.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(170,170,170), 1, 14),
                new EmptyBorder(6, 12, 6, 12)
        ));

        //comboFiltroCat.setRenderer(new ModernComboRenderer());

        // cargar categorías
               

        comboFiltroCat.addItem("Todas");
        for (String cat : categoriasMap.keySet())
            comboFiltroCat.addItem(cat);
        
        //comboFiltroCat.setSelectedIndex(0);

        comboFiltroCat.addActionListener(e -> ejecutarFiltrado());
             
        p.add(comboFiltroCat);
        
       

        return p;
    }




    private JPanel crearPanelAccionesRapidas() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);

        if(rolUsuario.equals("GERENTE")){
        	p.add(makeIconButton("🔧", "Editar", e -> abrirPanelDetalle()));
        	
        	
        }
        
        p.add(makeIconButton("🔄", "Refrescar", e -> {        	
        	cargarProductosDesdeBD();
            filtrarCatalogo();
            refrescarCatalogo();                
        }));
       // p.add(makeIconButton("📃", "Ver Factura", e -> mostrarVentanaFactura()));
        
        return p;
    }

    private JButton makeIconButton(String text, String tooltip, ActionListener listener) {
        JButton b = new JButton(text);
        b.setToolTipText(tooltip);
        b.setFont(new Font("Segoe UI Symbol", Font.BOLD, 30));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(72, 63));
        b.addActionListener(listener);
        return b;
    }
    
    private JButton makeIconButtonUI(String text, String tooltip, ActionListener listener) {
        JButton b = new JButton(text);
        b.setToolTipText(tooltip);
        b.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
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

        String sql = "SELECT Id_Producto, Nombre, Nombre_Generico, Farmaceutica, Gramaje, Precio_Venta, Stock, Foto_Producto, Activo, Id_Categoria, StockMin, Fecha_Caducidad FROM Producto";

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
                int cat = rs.getInt("Id_Categoria");
                int stockMin = rs.getInt("StockMin");
                String cad = rs.getString("Fecha_Caducidad");

                // === Verificar caducidad ===
                if (cad != null && !cad.isEmpty()) {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate fechaCad = LocalDate.parse(cad, fmt);
                    LocalDate hoy = LocalDate.now();

                    // Si ya expiró → actualizar BD
                    if (fechaCad.isBefore(hoy)) {
                        activo = false;

                        PreparedStatement psInactivar = conn.prepareStatement(
                            "UPDATE Producto SET Activo = 'N' WHERE Id_Producto = ?"
                        );
                        psInactivar.setInt(1, id);
                        psInactivar.executeUpdate();
                        psInactivar.close();
                        
                     //Notificación al panel de ANUNCIOS
                        mensaje = "El producto '" + nombre + "' ha caducado y fue marcado como inactivo.";
                        NotificacionManager.agregarNotificacion("PRODUCTO", mensaje);
                    }
                }

                // NO agregar productos inactivos al catálogo
                if (!activo) continue;

                Producto p = new Producto(
                    id, nombre, nombreGen, farm, gram,
                    precio, stock, foto, activo, cat, stockMin, cad
                );

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
        
        comboFiltroCat.setSelectedItem("Todas");
        
        for (Producto p : productosMap.values()) {
            if (q.isEmpty() || p.nombre.toLowerCase().contains(q) || (p.nombreGenerico != null && p.nombreGenerico.toLowerCase().contains(q))) {
                panelCatalogo.add(new ProductoCard(p));
            }
        }
        panelCatalogo.revalidate();
        panelCatalogo.repaint();
        
        
    }

    private void refrescarCatalogo() {
    	filtrarCatalogo(); 
    	aplicarFiltro("");
        
    }

    //private void agregarSeleccionadoAlCarrito() {
        // helper no usado: las tarjetas tienen su propio botón agregado
    //}

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

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> e : carritoMap.entrySet()) {
            Producto p = productosMap.get(e.getKey());
            int qty = e.getValue();

            BigDecimal precio = BigDecimal.valueOf(p.precioVenta);
            BigDecimal total = precio.multiply(BigDecimal.valueOf(qty));

            modeloFactura.addRow(new Object[]{
                    p.nombre,
                    String.format("%.2f", p.precioVenta),
                    qty,
                    String.format("%.2f", total.doubleValue())
            });

            subtotal = subtotal.add(total);
        }

        lblTotal.setText("Total: $" + String.format("%.2f", subtotal.doubleValue()));
    }


    private void mostrarVentanaFactura() {
        try {
            // === Crear carpeta en Escritorio ===
            String userHome = System.getProperty("user.home");
            File carpeta = new File(userHome + File.separator + "Desktop" + File.separator + "FacturasFarmacia");
            if (!carpeta.exists()) carpeta.mkdirs();

            // === Nombre archivo con fecha ===
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            File pdfFile = new File(carpeta, "Factura_" + timestamp + ".pdf");

            // === Crear documento PDF ===
            com.itextpdf.text.Document doc = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(pdfFile));

            doc.open();

            // ======= FUENTES ========
            com.itextpdf.text.Font tituloFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
            com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);

            // ======= LOGO ========
            try {
                java.net.URL logoUrl = getClass().getResource(LOGO_PATH);
                if (logoUrl != null) {
                    com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoUrl);
                    logo.scaleToFit(120, 120);       // tamaño del logo
                    logo.setAlignment(com.itextpdf.text.Image.ALIGN_LEFT);
                    doc.add(logo);
                }
            } catch (Exception exLogo) {
                System.out.println("No se pudo cargar el logo: " + exLogo.getMessage());
            }

            // ======= DATOS DE LA FARMACIA ========
            doc.add(new com.itextpdf.text.Paragraph(NOMBRE_FARMACIA, tituloFont));
            doc.add(new com.itextpdf.text.Paragraph(DIRECCION_FARMACIA, normalFont));
            doc.add(new com.itextpdf.text.Paragraph(TELEFONO_FARMACIA, normalFont));
            doc.add(new com.itextpdf.text.Paragraph(RUC_FARMACIA, normalFont));
            doc.add(new com.itextpdf.text.Paragraph(" "));

            // ======= DATOS EMPLEADO / FECHA ========
            String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
            doc.add(new com.itextpdf.text.Paragraph("Atendido por: " + nombreEmpleado, normalFont));
            doc.add(new com.itextpdf.text.Paragraph("ID empleado: " + idEmpleado, normalFont));
            doc.add(new com.itextpdf.text.Paragraph("Fecha: " + fecha, normalFont));
            doc.add(new com.itextpdf.text.Paragraph(" "));
            doc.add(new com.itextpdf.text.Paragraph("--------------- DETALLE DE COMPRA ---------------", boldFont));
            doc.add(new com.itextpdf.text.Paragraph(" "));

            // ======= TABLA DE PRODUCTOS ========
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{40, 10, 25, 25}); // ancho de columnas

            table.addCell(new com.itextpdf.text.Phrase("Producto", boldFont));
            table.addCell(new com.itextpdf.text.Phrase("Cant.", boldFont));
            table.addCell(new com.itextpdf.text.Phrase("Precio Unit.", boldFont));
            table.addCell(new com.itextpdf.text.Phrase("Total", boldFont));

            BigDecimal totalGeneral = BigDecimal.ZERO;

            for (Map.Entry<Integer, Integer> e : carritoMap.entrySet()) {
                Producto p = productosMap.get(e.getKey());
                int qty = e.getValue();

                BigDecimal precioUnit = BigDecimal.valueOf(p.precioVenta)
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal totalLinea = precioUnit.multiply(BigDecimal.valueOf(qty))
                        .setScale(2, RoundingMode.HALF_UP);

                table.addCell(new com.itextpdf.text.Phrase(p.nombre, normalFont));
                table.addCell(new com.itextpdf.text.Phrase(String.valueOf(qty), normalFont));
                table.addCell(new com.itextpdf.text.Phrase("$" + precioUnit, normalFont));
                table.addCell(new com.itextpdf.text.Phrase("$" + totalLinea, normalFont));

                totalGeneral = totalGeneral.add(totalLinea);
            }

            doc.add(table);

            // ======= TOTAL ========
            doc.add(new com.itextpdf.text.Paragraph(" "));
            doc.add(new com.itextpdf.text.Paragraph(
                    "TOTAL A PAGAR: $" + totalGeneral.setScale(2, RoundingMode.HALF_UP),
                    tituloFont));

            doc.add(new com.itextpdf.text.Paragraph(" "));
            doc.add(new com.itextpdf.text.Paragraph("Gracias por su compra. ¡Vuelva pronto!", boldFont));

            doc.close();

            // ======= ABRIR PDF ========
            Desktop.getDesktop().open(pdfFile);

            JOptionPane.showMessageDialog(this,
                    "Factura generada:\n" + pdfFile.getAbsolutePath(),
                    "Factura PDF", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al generar factura PDF:\n" + ex.getMessage());
        }
    }



    private void ejecutarVenta() {
        if (carritoMap.isEmpty()) { JOptionPane.showMessageDialog(this, "El carrito está vacío"); return; }

        try (Connection conn = ConexionDB.obtenerConexion()) {
            if (conn == null) return;
            conn.setAutoCommit(false);

            PreparedStatement psVenta = conn.prepareStatement(
                    "INSERT INTO Venta (Fecha, Id_Empleado) VALUES (DATE('now'), ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            psVenta.setInt(1, idEmpleado);
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
                
             // ==== OBTENER STOCK ACTUAL DESPUÉS DEL UPDATE ====
                PreparedStatement psCheck = conn.prepareStatement(
                        "SELECT Stock FROM Producto WHERE Id_Producto = ?"
                );
                psCheck.setInt(1, idProd);
                ResultSet rsStock = psCheck.executeQuery();

                if (rsStock.next()) {
                    int newStock = rsStock.getInt("Stock");

                    if (newStock == 0) {
                        mensaje = "El producto '" + p.nombre + "' llegó a 0 unidades y fue deshabilitado para la venta.";
                        NotificacionManager.agregarNotificacion("PRODUCTO", mensaje);
                    }
                }

                psCheck.close();
                
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

            JButton btnAdd = makeIconButtonUI("➕", "Agregar al carrito", e -> {
            	//aviso de stock minimo
            	 int STOCK_MINIMO = p.stockmin;

            	    if (p.stock <= STOCK_MINIMO) {
            	        JOptionPane.showMessageDialog(
            	            PanelProductos.this,
            	            "⚠ ADVERTENCIA: El producto \"" + p.nombre +
            	            "\" ha alcanzado su stock mínimo (" + p.stock +
            	            " unidades). Considere reabastecerlo pronto.",
            	            "Stock mínimo",
            	            JOptionPane.WARNING_MESSAGE
            	        );
            	    }
            	
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
            
            //Desavilitacion de stock bajo
            
         // --- CONTROLAR STOCK = 0 ---
            if (p.stock <= 0) {
                btnAdd.setEnabled(false);
                btnAdd.setBackground(new Color(200, 200, 200)); // gris

                // toda la tarjeta en gris
                setBackground(new Color(230, 230, 230));

                lblStock.setText("Stock: 0 (Agotado)");
                lblStock.setForeground(Color.RED);
                            
                // desactivar hover
                for (MouseListener ml : getMouseListeners())
                    removeMouseListener(ml);
            }


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
        int idCategoria;
        int stockmin;
        String caducidad;

        Producto(int id, String nombre, String nombreGenerico, String farmaceutica, String gramaje, double precioVenta, int stock, byte[] foto, boolean activo, int idcategoria, int stockmin, String caducidad) {
            this.id = id; this.nombre = nombre; this.nombreGenerico = nombreGenerico; this.farmaceutica = farmaceutica; this.gramaje = gramaje; this.precioVenta = precioVenta; this.stock = stock; this.foto = foto; this.activo = activo; this.idCategoria=idcategoria;this.stockmin = stockmin;this.caducidad=caducidad;
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
    
    private JTextField redondearTextField(JTextField tf) {
        return new JTextField(tf.getText()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255,255,255,200)); // fondo suave
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);

                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            public void setBorder(Border border) { /* bloquear border externo */ }
        };
    }
    
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

    
    private static void colocarPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(new Color(150,150,150));

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                bloqueoFiltro = true;  // ⛔ EVITA FILTRADO TEMPORALMENTE

                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }

                // 🔓 volver permitir filtrado
                SwingUtilities.invokeLater(() -> bloqueoFiltro = false);
            }

            @Override public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    bloqueoFiltro = true; // evita refresco raro al restaurar placeholder
                    field.setText(placeholder);
                    field.setForeground(new Color(150,150,150));

                    SwingUtilities.invokeLater(() -> bloqueoFiltro = false);
                }
            }
        });
    }

    
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
    
    private static class ModernComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            lbl.setBorder(new EmptyBorder(6, 8, 6, 8));

            if (isSelected) {
                lbl.setBackground(new Color(0, 150, 136));
                lbl.setForeground(Color.WHITE);
            } else {
                lbl.setBackground(Color.WHITE);
                lbl.setForeground(Color.DARK_GRAY);
            }

            return lbl;
        }
    }

    
    private void abrirPanelDetalle() {
        Window window = SwingUtilities.getWindowAncestor(this);

        JDialog dialog = new JDialog(window, "Administrar Productos", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(1100, 700);
        dialog.setLocationRelativeTo(window);

        // Crear el panel que ya tienes en canvas
        PanelDetalleProducto panel = new PanelDetalleProducto();

        dialog.setContentPane(panel);
        dialog.setVisible(true);

        // Si quieres refrescar catálogo cuando se cierra:
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                cargarProductosDesdeBD();
                refrescarCatalogo();
            }
        });
    }

}
