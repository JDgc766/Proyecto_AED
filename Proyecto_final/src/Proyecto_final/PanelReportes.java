package Proyecto_final;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

public class PanelReportes extends JPanel {

    // ===================== COMPONENTES PRINCIPALES =====================
    private JTable tabla;
    private DefaultTableModel modelo;

    private JComboBox<String> comboEmpleado, comboProducto, comboFecha;
    private JButton btnRefrescar, btnExportar;

    private java.util.List<Map<String, Object>> datosActuales = new ArrayList<>();

    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);


    // =========================== CONSTRUCTOR ===========================
    public PanelReportes() {

        setLayout(new BorderLayout(12, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // FILTROS ARRIBA
        add(crearFiltros(), BorderLayout.NORTH);

        // TABLA AL CENTRO
        crearTabla();
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        JPanel panelTabla = new RoundedPanel(16, Color.WHITE);
        panelTabla.setLayout(new BorderLayout());
        panelTabla.setBorder(new EmptyBorder(8,8,8,8));
        panelTabla.add(scroll, BorderLayout.CENTER);

        add(panelTabla, BorderLayout.CENTER);

        // Cargar contenido
        cargarFiltros();
        cargarDatos();
    }


    // ======================= PANEL FILTROS ========================
    private JPanel crearFiltros() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        p.setOpaque(false);

        comboEmpleado = new JComboBox<>();
        comboProducto = new JComboBox<>();
        comboFecha    = new JComboBox<>(new String[]{"Todos", "Semana", "Mes", "Año"});

        aplicarEstiloCombo(comboEmpleado);
        aplicarEstiloCombo(comboProducto);
        aplicarEstiloCombo(comboFecha);

        btnRefrescar = makeButton("🔄", e -> cargarDatos());
        btnExportar  = makeButton("📤", e -> exportarDatos());

        p.add(new JLabel("Empleado:"));
        p.add(comboEmpleado);

        p.add(new JLabel("Producto:"));
        p.add(comboProducto);

        p.add(new JLabel("Fecha:"));
        p.add(comboFecha);

        p.add(btnRefrescar);
        p.add(btnExportar);

        return p;
    }

    private void aplicarEstiloCombo(JComboBox<String> combo) {
        combo.setFont(FONT_NORMAL);
        combo.setPreferredSize(new Dimension(170, 34));
        combo.setUI(new ModernComboBoxUI());
        combo.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(170,170,170),1,12),
                new EmptyBorder(4,8,4,8)
        ));
    }

    private JButton makeButton(String icon, ActionListener al) {
        JButton b = new JButton(icon);
        b.setFont(new Font("Segoe UI Symbol", Font.BOLD, 30));
        b.setPreferredSize(new Dimension(72, 63));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);

        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setOpaque(true); b.setBackground(new Color(200,230,200)); }
            public void mouseExited(MouseEvent e) { b.setOpaque(false); }
        });

        return b;
    }


    // ======================= TABLA DE REPORTES ========================
    private void crearTabla() {
        modelo = new DefaultTableModel(
                new Object[]{"Fecha", "Empleado", "Producto", "Cantidad", "Precio Unit.", "Total"},
                0
        ) { /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int r, int c) { return false; } };

        tabla = new JTable(modelo);
        tabla.setRowHeight(30);
        tabla.setFont(FONT_NORMAL);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0,0));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header estilo moderno
        JTableHeader header = tabla.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer(header.getDefaultRenderer()));

        // Hover + zebra igual a PanelProductos
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            int hoverRow = -1;

            {
                tabla.addMouseMotionListener(new MouseMotionAdapter() {
                    public void mouseMoved(MouseEvent e) {
                        hoverRow = tabla.rowAtPoint(e.getPoint());
                        tabla.repaint();
                    }
                });
                tabla.addMouseListener(new MouseAdapter() {
                    public void mouseExited(MouseEvent e) {
                        hoverRow = -1;
                        tabla.repaint();
                    }
                });
            }

            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean sel, boolean has, int row, int col) {

                Component c = super.getTableCellRendererComponent(table, value, sel, has, row, col);

                if (sel) {
                    c.setBackground(new Color(200,230,200));
                    c.setForeground(Color.BLACK);
                } else if (row == hoverRow) {
                    c.setBackground(new Color(240,255,245));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(250,255,250) : new Color(244,250,244));
                    c.setForeground(new Color(30,60,40));
                }

                setBorder(new EmptyBorder(6,10,6,10));
                return c;
            }
        });
    }


    // ======================= CARGA DE FILTROS ========================
    private void cargarFiltros() {
        comboEmpleado.removeAllItems();
        comboProducto.removeAllItems();

        comboEmpleado.addItem("Todos");
        comboProducto.addItem("Todos");

        try (Connection conn = ConexionDB.obtenerConexion()) {

            ResultSet rsE = conn.prepareStatement("SELECT Nombre FROM Empleado ORDER BY Nombre").executeQuery();
            while (rsE.next()) comboEmpleado.addItem(rsE.getString(1));

            ResultSet rsP = conn.prepareStatement("SELECT Nombre FROM Producto ORDER BY Nombre").executeQuery();
            while (rsP.next()) comboProducto.addItem(rsP.getString(1));

        } catch (Exception ex) { ex.printStackTrace(); }
    }


    // ======================= CARGA DE DATOS ========================
    private void cargarDatos() {
        modelo.setRowCount(0);
        datosActuales.clear();

        String empleado = comboEmpleado.getSelectedItem().toString();
        String producto = comboProducto.getSelectedItem().toString();
        String fecha    = comboFecha.getSelectedItem().toString();

        String sql = """
                SELECT V.Fecha, E.Nombre AS Empleado, P.Nombre AS Producto,
                       DV.Cantidad, DV.Precio_Unitario
                FROM Venta V
                JOIN Empleado E ON V.Id_Empleado = E.Id_Empleado
                JOIN Detalle_Venta DV ON V.Id_Venta = DV.Id_Venta
                JOIN Producto P ON DV.Id_Producto = P.Id_Producto
                """;

        List<String> filtros = new ArrayList<>();

        if (!empleado.equals("Todos"))
            filtros.add("E.Nombre = '" + empleado + "'");

        if (!producto.equals("Todos"))
            filtros.add("P.Nombre = '" + producto + "'");

        if (!fecha.equals("Todos")) {
            switch (fecha) {
                case "Semana" -> filtros.add("DATE(V.Fecha) >= DATE('now','-7 days')");
                case "Mes"    -> filtros.add("strftime('%Y-%m', V.Fecha) = strftime('%Y-%m','now')");
                case "Año"    -> filtros.add("strftime('%Y', V.Fecha) = strftime('%Y','now')");
            }
        }

        if (!filtros.isEmpty())
            sql += " WHERE " + String.join(" AND ", filtros);

        sql += " ORDER BY V.Fecha DESC";


        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String fechaR  = rs.getString("Fecha");
                String emp     = rs.getString("Empleado");
                String prod    = rs.getString("Producto");

                int cant       = rs.getInt("Cantidad");
                double precio  = rs.getDouble("Precio_Unitario");
                double total   = precio * cant;

                modelo.addRow(new Object[]{
                        fechaR, emp, prod,
                        cant, String.format("%.2f", precio),
                        String.format("%.2f", total)
                });

                Map<String,Object> fila = new HashMap<>();
                fila.put("Fecha", fechaR);
                fila.put("Empleado", emp);
                fila.put("Producto", prod);
                fila.put("Cantidad", cant);
                fila.put("Precio", precio);
                fila.put("Total", total);

                datosActuales.add(fila);
            }

        } catch (Exception ex) { ex.printStackTrace(); }
    }


    // ======================= EXPORTACIÓN ========================
    private void exportarDatos() {

        String[] opcion = {"Excel (.xlsx)", "PDF"};
        int sel = JOptionPane.showOptionDialog(
                this, "¿En qué formato deseas exportar?",
                "Exportar", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null,
                opcion, opcion[0]
        );

        if (sel == 0) exportarExcel();
        else if (sel == 1) exportarPDF();
    }


    // ======================= EXPORTAR A EXCEL ========================
    private void exportarExcel() {
        try {

            File carpeta = new File(System.getProperty("user.home")
                    + "/Desktop/ReportesFarmacia");

            if (!carpeta.exists()) carpeta.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File archivo = new File(carpeta, "Reporte_" + timestamp + ".xlsx");

            org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Reporte");

            String[] headers = {"Fecha", "Empleado", "Producto", "Cantidad", "Precio Unit.", "Total"};

            org.apache.poi.ss.usermodel.Row hr = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++)
                hr.createCell(i).setCellValue(headers[i]);

            int rowNum = 1;

            for (var fila : datosActuales) {
                var r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue(fila.get("Fecha").toString());
                r.createCell(1).setCellValue(fila.get("Empleado").toString());
                r.createCell(2).setCellValue(fila.get("Producto").toString());
                r.createCell(3).setCellValue((int) fila.get("Cantidad"));
                r.createCell(4).setCellValue((double) fila.get("Precio"));
                r.createCell(5).setCellValue((double) fila.get("Total"));
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            wb.write(new java.io.FileOutputStream(archivo));
            wb.close();

            Desktop.getDesktop().open(archivo);

            JOptionPane.showMessageDialog(this,
                    "Excel generado:\n" + archivo.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al exportar a Excel:\n" + ex.getMessage());
        }
    }


    // ======================= EXPORTAR A PDF ========================
    private void exportarPDF() {
        try {
            File carpeta = new File(System.getProperty("user.home")
                    + "/Desktop/ReportesFarmacia");

            if (!carpeta.exists()) carpeta.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File pdfFile = new File(carpeta, "Reporte_" + timestamp + ".pdf");

            com.itextpdf.text.Document doc = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(pdfFile));
            doc.open();

            com.itextpdf.text.Font tituloFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA,18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA,12);

            // LOGO
            try {
                var url = getClass().getResource("/imagenes/f.jpg");
                if (url != null) {
                    var logo = com.itextpdf.text.Image.getInstance(url);
                    logo.scaleToFit(120,120);
                    doc.add(logo);
                }
            } catch(Exception ignored){}

            doc.add(new com.itextpdf.text.Paragraph("FARMACIA JALINAS 2", tituloFont));
            doc.add(new com.itextpdf.text.Paragraph("Reporte de ventas", normalFont));
            doc.add(new com.itextpdf.text.Paragraph("Generado: "
                    + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), normalFont));
            doc.add(new com.itextpdf.text.Paragraph(" "));

            // TABLA PDF
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(6);
            table.setWidthPercentage(100);

            String[] cols = {"Fecha","Empleado","Producto","Cantidad","Precio Unit.","Total"};
            for (String col : cols)
                table.addCell(new com.itextpdf.text.Phrase(col, tituloFont));

            for (var fila : datosActuales) {
                table.addCell(fila.get("Fecha").toString());
                table.addCell(fila.get("Empleado").toString());
                table.addCell(fila.get("Producto").toString());
                table.addCell(fila.get("Cantidad").toString());
                table.addCell(String.format("%.2f", fila.get("Precio")));
                table.addCell(String.format("%.2f", fila.get("Total")));
            }

            doc.add(table);
            doc.close();

            Desktop.getDesktop().open(pdfFile);

            JOptionPane.showMessageDialog(this,
                    "PDF generado:\n" + pdfFile.getAbsolutePath());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error al exportar PDF:\n" + ex.getMessage());
        }
    }




    // =====================================================================
    //                     COMPONENTES VISUALES (MISMOS QUE PANELPRODUCTOS)
    // =====================================================================

    // PANEL REDONDEADO
    class RoundedPanel extends JPanel {
        private final int arc;
        private final Color bg;

        public RoundedPanel(int arc, Color bg) {
            this.arc = arc; this.bg = bg;
            setOpaque(false);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),arc,arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // BORDER REDONDEADO
    private static class RoundedLineBorder extends javax.swing.border.LineBorder {
        private final int radius;
        RoundedLineBorder(Color c, int t, int r) { super(c,t,true); radius = r; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(lineColor);
            g2.drawRoundRect(x,y,w-1,h-1,radius,radius);
            g2.dispose();
        }
    }

    // SCROLLBAR MODERNO
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        private final Color colorThumb = new Color(179,215,168);

        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(colorThumb);
            g2.fillRoundRect(r.x,r.y,r.width-2,r.height,14,14);
            g2.dispose();
        }
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(new Color(240,240,240));
            g.fillRect(r.x,r.y,r.width,r.height);
        }
        protected JButton createDecreaseButton(int o){ return zero(); }
        protected JButton createIncreaseButton(int o){ return zero(); }
        private JButton zero(){
            JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0));
            b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0));
            return b;
        }
    }

    // RENDERER DEL HEADER
    private static class HeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer delegate;
        HeaderRenderer(TableCellRenderer d){ delegate=d; }

        public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
            Component comp = delegate.getTableCellRendererComponent(t,v,s,f,r,c);
            comp.setBackground(new Color(235,250,235));
            comp.setForeground(new Color(30,80,40));
            comp.setFont(FONT_BOLD);
            if(comp instanceof JComponent jc)
                jc.setBorder(new EmptyBorder(8,10,8,10));
            return comp;
        }
    }

    // COMBOBOX ESTILIZADO
    private static class ModernComboBoxUI extends javax.swing.plaf.basic.BasicComboBoxUI {

        protected JButton createArrowButton() {
            JButton b = new JButton("▼");
            b.setFont(new Font("Segoe UI Symbol",Font.BOLD,14));
            b.setBorder(null);
            b.setBackground(Color.WHITE);
            return b;
        }

        public void installUI(JComponent c) {
            super.installUI(c);
            JComboBox<?> combo = (JComboBox<?>) c;

            combo.setFocusable(false);
            combo.setRenderer(new ModernComboRenderer());
        }

        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox){
                protected JScrollPane createScroller(){
                    JScrollPane scroll=new JScrollPane(list);
                    scroll.setBorder(null);
                    scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
                    scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
                    return scroll;
                }
            };
            popup.setBorder(new RoundedLineBorder(new Color(200,200,200),1,14));
            return popup;
        }
    }

    private static class ModernComboRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean sel,boolean f){
            JLabel lbl=(JLabel)super.getListCellRendererComponent(l,v,i,sel,f);
            lbl.setFont(FONT_NORMAL);
            lbl.setBorder(new EmptyBorder(6,10,6,10));

            if(sel){ lbl.setBackground(new Color(0,150,136)); lbl.setForeground(Color.WHITE); }
            else   { lbl.setBackground(Color.WHITE); lbl.setForeground(Color.DARK_GRAY); }

            return lbl;
        }
    }

}
