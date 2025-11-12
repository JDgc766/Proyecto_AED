package Proyecto_final;
import javax.swing.*;
import java.awt.event.*;

public class VentanaConAnimacion extends JFrame {
    protected Timer timerFade;
    protected float opacidad = 0.0f;
    protected final float FADE_STEP = 0.05f;
    protected final int FADE_DELAY = 20;

    public VentanaConAnimacion() {
        setUndecorated(true);
    }

    public void startFadeIn() {
        if (timerFade != null && timerFade.isRunning()) timerFade.stop();
        opacidad = 0.0f;
        setOpacity(opacidad);
        timerFade = new Timer(FADE_DELAY, e -> {
            opacidad += FADE_STEP;
            if (opacidad >= 1.0f) {
                opacidad = 1.0f;
                setOpacity(opacidad);
                ((Timer) e.getSource()).stop();
            } else setOpacity(opacidad);
        });
        timerFade.start();
    }

    public void startFadeOut() {
        if (timerFade != null && timerFade.isRunning()) timerFade.stop();
        opacidad = 1.0f;
        timerFade = new Timer(FADE_DELAY, e -> {
            opacidad -= FADE_STEP;
            if (opacidad <= 0.0f) {
                opacidad = 0.0f;
                setOpacity(opacidad);
                ((Timer) e.getSource()).stop();
                dispose();
            } else setOpacity(opacidad);
        });
        timerFade.start();
    }
}
