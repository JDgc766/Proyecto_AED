package Proyecto_final;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Mensajes extends JWindow {

    private Timer fadeInTimer;
    private Timer fadeOutTimer;
    private Timer pauseTimer;
    private float currentOpacity = 0f;
    
        private Runnable onFinishedCallback;


    public Mensajes(JFrame parent, String texto) {
        this(parent, texto, null);
    }
    

    public Mensajes(JFrame parent, String texto, Runnable onFinishedCallback) {
        super(parent);
        this.onFinishedCallback = onFinishedCallback; 
        JPanel panel = new JPanel();
        panel.setBackground(new Color(36, 37, 42));
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2));
        panel.setLayout(new BorderLayout());

        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbl.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        panel.add(lbl, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();

        if (parent != null) {
            int x = parent.getX() + (parent.getWidth() - getWidth()) / 2;
            int y = parent.getY() + (parent.getHeight() - getHeight()) / 2;
            setLocation(x, y);
        } else {
            setLocationRelativeTo(null);
        }

        setOpacity(0f); 

        fadeInTimer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentOpacity += 0.1f;
                if (currentOpacity >= 1.0f) {
                    currentOpacity = 1.0f;
                    setOpacity(currentOpacity);
                    ((Timer) e.getSource()).stop(); 
                    pauseTimer.start();
                } else {
                    setOpacity(currentOpacity);
                }
            }
        });

        pauseTimer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fadeOutTimer.start();
            }
        });
        pauseTimer.setRepeats(false); 

        fadeOutTimer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentOpacity -= 0.1f;
                if (currentOpacity <= 0.0f) {
                    currentOpacity = 0.0f;
                    setOpacity(currentOpacity);
                    ((Timer) e.getSource()).stop();
                    
                    if (onFinishedCallback != null) {
                        onFinishedCallback.run();
                    }
                    
                    dispose(); 
                } else {
                    setOpacity(currentOpacity);
                }
            }
        });

        setVisible(true);
        fadeInTimer.start();
    }
}