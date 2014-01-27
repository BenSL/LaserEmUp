package laseremup;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class LaserEmUp {

    public static void main(String[] args) {
        // Fenster erzeugen
        JFrame window = new JFrame("LaserEmUp!");
        window.setAutoRequestFocus(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Content Pane Größe und Layout setzen
        Container contentPane = (JPanel) window.getContentPane();
        contentPane.setPreferredSize(new Dimension(800, 600));
        contentPane.setLayout(new BorderLayout());
        // GameCanvas erzeugen und hinzufügen
        GameCanvas canvas = new GameCanvas();
        contentPane.add(canvas, BorderLayout.CENTER);
        // Fenstergröße fixieren und sichtbar machen
        window.pack();
        window.setResizable(false);
        window.setVisible(true);
        // GameCanvas starten
        canvas.start();
    }
    
}
