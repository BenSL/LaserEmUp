package laseremup;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * HumanSpaceShip
 * abgeleitet von SpaceShip
 */
public class HumanSpaceShip extends SpaceShip {
    
    // Prozess für Flug nach rechts, links, Prozess für Laserschüsse
    private Thread hFlyingThread, shootingThread;
    // Status der Laserkanone
    private boolean shooting;

    public HumanSpaceShip(Point position, double width, Rectangle bounds, double speed) {
        super(position, width, bounds);
        setSpeed(speed);
    }

    /**
     * Fliege nach rechts
     */
    public void moveRight() {
        if(hFlyingThread != null) {
            hFlyingThread.interrupt();
        }
        hFlyingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    try {
                        Thread.sleep(25l);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    int newX = position.x + (int) Math.round(speed);
                    if(newX + width > bounds.width) {
                        break;
                    }
                    position.x = newX;
                }
            }
        }, "hFlyingThreadRight");
        hFlyingThread.start();
    }

    /**
     * Fliege nach links
     */
    public void moveLeft() {
        if(hFlyingThread != null) {
            hFlyingThread.interrupt();
        }
        hFlyingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    try {
                        Thread.sleep(25l);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    int newX = position.x - (int) Math.round(speed);
                    if(newX < bounds.x) {
                        break;
                    }
                    position.x = newX;
                }
            }
        }, "hFlyingThreadLeft");
        hFlyingThread.start();
    }

    /**
     * Schiesse
     */
    public void shoot() {
        // Falls bereits geschossen wird, nicht erneut schiessen
        if(isShooting()) {
            return;
        }
        shootingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                setShooting(true);
                try {
                    // Ein Schuss geht 100 ms
                    Thread.sleep(100l);
                } catch (InterruptedException ex) {
                } finally {
                    setShooting(false);
                }
            }
        }, "shootingThread");
        shootingThread.start();
    }

    public synchronized boolean isShooting() {
        return shooting;
    }

    public synchronized void setShooting(boolean shooting) {
        this.shooting = shooting;
    }

    @Override
    public void destroy() {
        // Flugprozess nach linkes/rechts beenden
        hFlyingThread.interrupt();
        // Schiessenprozess beenden
        shootingThread.interrupt();
    }
    
}
