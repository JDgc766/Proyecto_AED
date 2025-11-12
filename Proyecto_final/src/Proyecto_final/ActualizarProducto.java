package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate; // Importado para validar la fecha
import java.time.format.DateTimeParseException; // Importado para validar la fecha

public class ActualizarProducto extends JPanel {

    // --- Constantes de UI ---
    private final Color COLOR_FONDO = new Color(35, 35, 35);
    private final Color COLOR_BOTON = new Color(55, 55, 55);
    private final Color COLOR_HOVER = new Color(75, 75, 75);
    private final Color COLOR_TEXTO = new Color(230, 230, 230);

    // --- Componentes de UI (Ahora son miembros de la clase) ---
    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtNombreGen;
    private JTextField txtPrecioCompra;
    private JTextField txtPrecioVenta;
    private JTextField txtFechaCad;
    private JTextField txtStock;
    
    private JButton btnBuscar;
    private JButton btnActualizar;
    private JButton btnLimpiar;

    private JFrame frameActual;

    public ActualizarProducto(JFrame frameActual) {
        this.frameActual = frameActual;

        setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // --- Panel de Formulario (Cambiado a 8 filas) ---
        JPanel panelFormulario = new JPanel(new GridLayout(8, 2, 15, 15));
        panelFormulario.setBackground(COLOR_FONDO);

        // --- 1. Fila de ID y Búsqueda ---
        JLabel lblId = new JLabel("ID del Producto:");
        lblId.setForeground(COLOR_TEXTO);
        txtId = new JTextField();
        
        // Botón para buscar el producto por ID
        btnBuscar = crearBoton("Buscar");
        
        // Panel para juntar el campo ID y el botón Buscar
        JPanel panelId = new JPanel(new BorderLayout(5, 0));
        panelId.setBackground(COLOR_FONDO);
        panelId.add(txtId, BorderLayout.CENTER);
        panelId.add(btnBuscar, BorderLayout.EAST);

        panelFormulario.add(lblId);
        panelFormulario.add(panelId);

        // --- 2. Fila Nombre ---
        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setForeground(COLOR_TEXTO);
        txtNombre = new JTextField();
        panelFormulario.add(lblNombre);
        panelFormulario.add(txtNombre);

        // --- 3. Fila Nombre Genérico ---
        JLabel lblNombreGen = new JLabel("Nombre Genérico (Opcional):");
        lblNombreGen.setForeground(COLOR_TEXTO);
        txtNombreGen = new JTextField();
        panelFormulario.add(lblNombreGen);
        panelFormulario.add(txtNombreGen);

        // --- 4. Fila Precio Compra ---
        JLabel lblPrecioCompra = new JLabel("Precio Compra:");
        lblPrecioCompra.setForeground(COLOR_TEXTO);
        txtPrecioCompra = new JTextField();
        panelFormulario.add(lblPrecioCompra);
        panelFormulario.add(txtPrecioCompra);

        // --- 5. Fila Precio Venta ---
        JLabel lblPrecioVenta = new JLabel("Precio Venta:");
        lblPrecioVenta.setForeground(COLOR_TEXTO);
        txtPrecioVenta = new JTextField();
        panelFormulario.add(lblPrecioVenta);
        panelFormulario.add(txtPrecioVenta);

        // --- 6. Fila Fecha Caducidad ---
        JLabel lblFechaCad = new JLabel("Fecha Caducidad (YYYY-MM-DD):");
        lblFechaCad.setForeground(COLOR_TEXTO);
        txtFechaCad = new JTextField();
        panelFormulario.add(lblFechaCad);
        panelFormulario.add(txtFechaCad);

        // --- 7. Fila Stock ---
        JLabel lblStock = new JLabel("Stock:");
        lblStock.setForeground(COLOR_TEXTO);
        txtStock = new JTextField();
        panelFormulario.add(lblStock);
        panelFormulario.add(txtStock);

        // --- 8. Fila de Botones de Acción ---
        btnActualizar = crearBoton("Actualizar Producto");
        btnLimpiar = crearBoton("Limpiar");
        
        // Panel para los botones de Actualizar y Limpiar
        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 10, 0));
        panelBotones.setBackground(COLOR_FONDO);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnLimpiar);

        panelFormulario.add(new JLabel()); // Espacio vacío en la grilla
        panelFormulario.add(panelBotones);

        add(panelFormulario, BorderLayout.CENTER);

        // --- Estado Inicial de los Botones ---
        // El usuario no puede actualizar hasta que haya buscado un producto
        btnActualizar.setEnabled(false);

        // --- Lógica del Botón Buscar ---
        btnBuscar.addActionListener(e -> buscarProducto());

        // --- Lógica del Botón Actualizar ---
        btnActualizar.addActionListener(e -> actualizarProducto());

        // --- Lógica del Botón Limpiar ---
        btnLimpiar.addActionListener(e -> limpiarCampos());
    }

    /**
     * Busca un producto en la BD usando el ID y rellena los campos.
     */
    private void buscarProducto() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el ID del producto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int idProducto = Integer.parseInt(idStr);
            
            // **CORRECCIÓN:** Usar try-with-resources para Connection y PreparedStatement
            // Esto asegura que se cierren automáticamente, evitando fugas de memoria.
            String sql = "SELECT * FROM Producto WHERE Id_Producto = ?";
            
            try (Connection conn = ConexionDB.obtenerConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, idProducto);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Producto encontrado, rellenar campos
                        txtNombre.setText(rs.getString("Nombre"));
                        txtNombreGen.setText(rs.getString("Nombre_Generico"));
                        txtPrecioCompra.setText(String.valueOf(rs.getDouble("Precio_Compra")));
                        txtPrecioVenta.setText(String.valueOf(rs.getDouble("Precio_Venta")));
                        txtFechaCad.setText(rs.getString("Fecha_Caducidad"));
                        txtStock.setText(String.valueOf(rs.getInt("Stock")));
                        
                        // Bloquear el campo ID y habilitar la actualización
                        txtId.setEditable(false);
                        btnActualizar.setEnabled(true);
                        
                    } else {
                        // Producto no encontrado
                        JOptionPane.showMessageDialog(this, "No se encontró ningún producto con ese ID.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        limpiarCampos();
                    }
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al buscar producto:\n" + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El ID debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Actualiza el producto en la BD con los datos de los campos.
     */
    private void actualizarProducto() {
        // El ID no se puede editar, así que lo leemos (sabemos que es válido)
        int idProducto = Integer.parseInt(txtId.getText().trim());

        // Obtener y validar todos los demás campos
        String nombre = txtNombre.getText().trim();
        String nombreGen = txtNombreGen.getText().trim();
        String precioCompraStr = txtPrecioCompra.getText().trim();
        String precioVentaStr = txtPrecioVenta.getText().trim();
        String fechaCad = txtFechaCad.getText().trim();
        String stockStr = txtStock.getText().trim();

        if (nombre.isEmpty() || precioCompraStr.isEmpty() || precioVentaStr.isEmpty() 
                || fechaCad.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // **NUEVA VALIDACIÓN:** Validar el formato de la fecha
        if (!esFechaValida(fechaCad)) {
            JOptionPane.showMessageDialog(this, "Formato de fecha incorrecto. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double precioCompra = Double.parseDouble(precioCompraStr);
            double precioVenta = Double.parseDouble(precioVentaStr);
            int stock = Integer.parseInt(stockStr);

            if (precioCompra < 0 || precioVenta < 0 || stock < 0) {
                JOptionPane.showMessageDialog(this, "Precio y stock deben ser positivos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // **CORRECCIÓN:** Usar try-with-resources para Connection y PreparedStatement
            String sql = "UPDATE Producto SET Nombre=?, Nombre_Generico=?, Precio_Compra=?, Precio_Venta=?, Fecha_Caducidad=?, Stock=? WHERE Id_Producto=?";
            
            try (Connection conn = ConexionDB.obtenerConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, nombre);
                ps.setString(2, nombreGen.isEmpty() ? null : nombreGen); // Manejo correcto de opcional
                ps.setDouble(3, precioCompra);
                ps.setDouble(4, precioVenta);
                ps.setString(5, fechaCad);
                ps.setInt(6, stock);
                ps.setInt(7, idProducto); // El ID va al final en el WHERE

                int afectadas = ps.executeUpdate();
                
                if (afectadas > 0) {
                    JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos(); // Limpiar el formulario después de actualizar
                } else {
                    // Esto no debería pasar si la lógica de búsqueda funcionó
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar el producto (ID no encontrado).", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar producto:\n" + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Precios y stock deben ser números válidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Limpia todos los campos del formulario y restaura el estado inicial.
     */
    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtNombreGen.setText("");
        txtPrecioCompra.setText("");
        txtPrecioVenta.setText("");
        txtFechaCad.setText("");
        txtStock.setText("");
        
        // Restaurar estado de botones y campo ID
        txtId.setEditable(true);
        btnActualizar.setEnabled(false);
    }
    
    /**
     * Valida si un String tiene el formato de fecha YYYY-MM-DD.
     * @param fecha El String de la fecha.
     * @return true si es válido, false si no.
     */
    private boolean esFechaValida(String fecha) {
        try {
            LocalDate.parse(fecha); // Intenta parsear la fecha
            return true;
        } catch (DateTimeParseException e) {
            return false; // Falla si el formato es incorrecto
        }
    }

    /**
     * Método de fábrica para crear botones con el estilo deseado.
     */
    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(COLOR_BOTON);
        boton.setForeground(COLOR_TEXTO);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        boton.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { boton.setBackground(COLOR_HOVER); }
            @Override public void mouseExited(MouseEvent e) { boton.setBackground(COLOR_BOTON); }
        });
        return boton;
    }
}