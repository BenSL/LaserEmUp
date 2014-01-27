package laseremup;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

/**
 * AlienSpaceShip
 * abgeleitet von SpaceShip
 */
public class AlienSpaceShip extends SpaceShip {
    
    // Prozess der den Flug des Alienraumschiffes steuert
    private Thread flyingThread;

    public AlienSpaceShip(Point position, double width, Rectangle bounds, double speed) {
        super(position, width, bounds);
        setSpeed(speed);
    }
    
    public void fly() {
        
        this.flyingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    try {
                        Thread.sleep(25l);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    // Neue Position berechnen
                    double x = position.getX();
                    double y = position.getY();
                    // Bewegung nach Links
                    double moveLeft = new Random().nextDouble() * speed;
                    // Bewegung nach Rechts
                    double moveRight = new Random().nextDouble() * speed;
                    // Bewegung nach Vorne
                    double moveForward = new Random().nextDouble() * speed;

                    // Bewegung nach links/rechts errechnen
                    double xNew = x + (moveLeft - moveRight);
                    
                    // Korrektur falls Alienraumschiff aus Fenster fliegt
                    if(xNew < bounds.getX()) {
                        // Links heraus: Korrektur nach rechts
                        xNew = x + moveRight;
                    }else if(xNew + width > bounds.getWidth()) {
                        // Rechts heraus: Korrektur nach links
                        xNew = x - moveLeft;
                    }
                    x = xNew;
                    
                    y = y + moveForward;

                    // Errechnete Position setzen: Hauptprogramm zeichnet Raumschiff dann
                    position.setLocation(x, y);
                }
            }
        }, "flyingThread");
        this.flyingThread.start();
        
    }
    
    @Override
    public void destroy() {
        // Flugprozess stoppen
        flyingThread.interrupt();
    }
    
}
