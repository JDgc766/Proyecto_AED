package Proyecto_final;

import javax.swing.*;
import java.awt.*;

public class PanelVendedores extends JPanel {
	
    private final Color COLOR_FONDO = new Color(225, 245, 254); 

    public PanelVendedores() {
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JLabel lbl = new JLabel("Aquí van los vendedores", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(lbl, BorderLayout.CENTER);
    }
}
