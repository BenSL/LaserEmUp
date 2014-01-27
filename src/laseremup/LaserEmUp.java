package laseremup;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class LaserEmUp {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        } catch (UnsupportedLookAndFeelException ex) {}
        JFrame window = new JFrame("LaserEmUp!");
        window.setAutoRequestFocus(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container contentPane = (JPanel) window.getContentPane();
        contentPane.setPreferredSize(new Dimension(800, 600));
        contentPane.setLayout(new BorderLayout());
        GameCanvas canvas = new GameCanvas();
        contentPane.add(canvas, BorderLayout.CENTER);
        window.pack();
        window.setResizable(false);
        window.setVisible(true);
        canvas.start();
    }
    
}
