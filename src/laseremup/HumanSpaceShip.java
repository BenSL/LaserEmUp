package laseremup;

import java.awt.Point;
import java.awt.Rectangle;

public class HumanSpaceShip extends SpaceShip {
    
    private Thread hFlyingThread, shootingThread;
    private boolean shooting;

    public HumanSpaceShip(Point position, double width, Rectangle bounds, double speed) {
        super(position, width, bounds);
        setSpeed(speed);
    }

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

    public void shoot() {
        if(isShooting()) {
            return;
        }
        shootingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                setShooting(true);
                try {
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
        hFlyingThread.interrupt();
        shootingThread.interrupt();
    }
    
}
