package Proyecto_final;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class VentanaAnimada extends JFrame {

    private Timer timerFade;
    private float opacidad = 0.0f;
    private final float FADE_STEP = 0.05f;
    private final int FADE_DELAY = 20;

    public VentanaAnimada() {
        setUndecorated(true);
        
        setTitle("Ventana con Animación");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        add(new JLabel("¡Hola! Esta es una ventana animada.", SwingConstants.CENTER));
        JButton closeButton = new JButton("Cerrar");
        add(closeButton, BorderLayout.SOUTH);

        closeButton.addActionListener(e -> startFadeOut());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                startFadeOut();
            }
        });
    }

    public void startFadeIn() {
        if (timerFade != null && timerFade.isRunning()) {
            timerFade.stop();
        }

        opacidad = 0.0f;
        setOpacity(opacidad);

        timerFade = new Timer(FADE_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacidad += FADE_STEP;
                
                if (opacidad >= 1.0f) {
                    opacidad = 1.0f;
                    setOpacity(opacidad);
                    ((Timer) e.getSource()).stop();
                } else {
                    setOpacity(opacidad);
                }
            }
        });
        timerFade.start();
    }

    public void startFadeOut() {
        if (timerFade != null && timerFade.isRunning()) {
            timerFade.stop();
        }

        opacidad = 1.0f;

        timerFade = new Timer(FADE_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacidad -= FADE_STEP;
                
                if (opacidad <= 0.0f) {
                    opacidad = 0.0f;
                    setOpacity(opacidad);
                    ((Timer) e.getSource()).stop();
                    
                    dispose(); 
                    System.exit(0); 
                } else {
                    setOpacity(opacidad);
                }
            }
        });
        timerFade.start();
    }

    public static void main(String[] args) {
        
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
            System.err.println("La translucidez (opacidad) no está soportada en este sistema.");
            JOptionPane.showMessageDialog(null, 
                "La animación de opacidad no es soportada en este sistema.", 
                "Error de Gráficos", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            VentanaAnimada ventana = new VentanaAnimada();
            ventana.setVisible(true); 
            ventana.startFadeIn();
        });
    }
}