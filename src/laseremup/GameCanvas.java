package laseremup;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Random;

public class GameCanvas extends Canvas {
    
    ArrayList<AlienSpaceShip> asships = new ArrayList<AlienSpaceShip>();

    public GameCanvas() {
        this.setIgnoreRepaint(true);
    }
    
    public void start() {
        
        // 3 buffers
        this.createBufferStrategy(3);
        final BufferStrategy bs = this.getBufferStrategy();
        
        // generate new alienspaceships < every 5 seconds
        new Thread(new Runnable() {
            
            Random rg = new Random();

            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    try {
                        Thread.sleep((long) (5000d * rg.nextDouble()));
                    } catch (InterruptedException ex) {}
                    int y = 1;
                    int x = (int) Math.round(GameCanvas.this.getBounds().getWidth() * rg.nextDouble());
                    AlienSpaceShip newShip = new AlienSpaceShip(new Point(x, y), SpaceShip.SPEED_FAST, SpaceShip.WIDTH_MEDIUM, GameCanvas.this.getBounds());
                    asships.add(newShip);
                    newShip.fly();
                }
                
            }
        }, "generateAlienSpaceShips").start();
        
        
        // paint everything
        new Thread(new Runnable() {

            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    // Antialiasing on
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // draw Background black
                    g.setColor(Color.black);
                    g.fillRect(0, 0, GameCanvas.this.getBounds().width, GameCanvas.this.getBounds().height);
                    // draw alienspaceships
                    for(int i = 0; i<asships.size(); i++) {
                        AlienSpaceShip ship = asships.get(i);
                        int x = (int) Math.round(ship.getPosition().getX());
                        int y = (int) Math.round(ship.getPosition().getY());
                        g.setColor(Color.red);
                        g.fillOval(x, y, 50, 10);
                    }
                    // into buffer
                    g.dispose();
                    bs.show();
                }
            }
        }, "drawEverything").start();
    }
    
    
}
