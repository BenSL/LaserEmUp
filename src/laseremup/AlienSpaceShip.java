package laseremup;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

public class AlienSpaceShip extends SpaceShip {
    
    private Thread flyingThread;

    public AlienSpaceShip(Point position, double speed, double width, Rectangle bounds) {
        super(position, speed, width, bounds);
    }
    
    @Override
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
                    double x = position.getX();
                    double y = position.getY();
                    double moveLeft = new Random().nextDouble() * speed;
                    double moveRight = new Random().nextDouble() * speed;
                    double moveForward = new Random().nextDouble() * speed;

                    // xNew + width >= bounds.x && xNew + width < bounds.width
                    double xNew = x + (moveLeft - moveRight);
                    if(xNew < bounds.getX()) {
                        xNew = x + moveRight;
                    }else if(xNew + width > bounds.getWidth()) {
                        xNew = x - moveLeft;
                    }
                    x = xNew;
                    
                    // if yNew > canvas height --> destroy thread
                    double yNew = y + moveForward;
                    if(yNew > bounds.getHeight()) {
                        destroy();
                    }
                    y = yNew;

                    position.setLocation(x, y);
                }
            }
        }, "flyingThread");
        this.flyingThread.start();
        
    }
    
    @Override
    public void destroy() {
        flyingThread.interrupt();
    }
    
}