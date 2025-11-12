package Proyecto_final;

import javax.swing.*;
import java.awt.*;

public class FarmaciaApp {

    public static void main(String[] args) {
        mostrarPantallaDeCarga();
        
        new Interfaz().setVisible(true);
    }

    private static void mostrarPantallaDeCarga() {
        JWindow splash = new JWindow();
        JPanel panel = new JPanel();
        panel.setBackground(new Color(36, 37, 42));
        panel.setLayout(null);

        JLabel lblTitulo = new JLabel("Conectando a la base de datos...");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBounds(60, 40, 250, 25);
        panel.add(lblTitulo);

        JProgressBar barra = new JProgressBar(0, 100);
        barra.setBounds(30, 80, 300, 25);
        barra.setStringPainted(true);
        panel.add(barra);

        splash.add(panel);
        splash.setSize(360, 150);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        try {
            for (int i = 0; i <= 100; i += 5) {
                Thread.sleep(80);
                barra.setValue(i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        splash.dispose();
    }
}
