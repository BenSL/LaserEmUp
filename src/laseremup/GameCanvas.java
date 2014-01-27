package laseremup;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameCanvas extends Canvas implements KeyListener {
    
    public static final int         CONFIG_LIVES = 3;
    public static final double      CONFIG_SPEED_ALIENS = SpaceShip.SPEED_FAST;
    public static final double      CONFIG_SPEED_HUMANS = SpaceShip.SPEED_FAST;
    public static final double      CONFIG_WIDTH_ALIENS = SpaceShip.WIDTH_MEDIUM;
    public static final double      CONFIG_WIDTH_HUMANS = SpaceShip.WIDTH_SMALL;
    
    ArrayList<AlienSpaceShip> asships = new ArrayList<AlienSpaceShip>();
    HumanSpaceShip hsship;

    public GameCanvas() {
        addKeyListener(this);
        this.setIgnoreRepaint(true);
    }
    
    public void start() {
        
        // 3 buffers
        this.createBufferStrategy(3);
        final BufferStrategy bs = this.getBufferStrategy();
        
        // humanspaceship
        int hsshipX = (int) Math.round((this.getBounds().getWidth()/2)-(CONFIG_WIDTH_HUMANS/2));
        int hsshipY = (int) Math.round(this.getBounds().getHeight() - 20);
        hsship = new HumanSpaceShip(new Point(hsshipX, hsshipY), CONFIG_SPEED_HUMANS, CONFIG_WIDTH_HUMANS, GameCanvas.this.getBounds());
        
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
                    AlienSpaceShip newShip = new AlienSpaceShip(new Point(x, y), CONFIG_SPEED_ALIENS, CONFIG_WIDTH_ALIENS, GameCanvas.this.getBounds());
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
                    // draw humanspaceship
                    int x = hsship.getPosition().x;
                    int y = hsship.getPosition().y;
                    g.setColor(Color.white);
                    Polygon p = new Polygon();
                    Point hssLeft = new Point(x, y);
                    Point hssRight = new Point(x + (int) Math.round(hsship.getWidth()), y);
                    Point hssFront = new Point(x + (int) Math.round(hsship.getWidth()/2), y-10);
                    p.addPoint(hssLeft.x, hssLeft.y);
                    p.addPoint(hssRight.x, hssRight.y);
                    p.addPoint(hssFront.x, hssFront.y);
                    g.fillPolygon(p);
                    // draw laser
                    if(hsship.isShooting()) {
                        g.setColor(Color.yellow);
                        g.drawLine(hssFront.x, hssFront.y, hssFront.x, getBounds().y);
                    }
                    // draw & destroy alienspaceships by lazer
                    Iterator<AlienSpaceShip> it = asships.iterator();
                    while(it.hasNext()) {
                        AlienSpaceShip ship = it.next();
                        if(hsship.isShooting() 
                                && ship.getPosition().x <= hssFront.x 
                                && (ship.getPosition().x+(int) Math.round(ship.getWidth())) >= hssFront.x) {
                            ship.destroy();
                            it.remove();
                            continue;
                        }
                        g.setColor(Color.red);
                        g.fillOval(ship.getPosition().x, ship.getPosition().y, (int) Math.round(ship.getWidth()), 10);
                    }
                    // into buffer
                    g.dispose();
                    bs.show();
                }
            }
        }, "drawEverything").start();
        
        this.requestFocus();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        // escape terminates the programm
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
        // move humanspaceship right
        if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
            hsship.moveRight();
        }
        // move humanspaceship left
        if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            hsship.moveLeft();
        }
        // make humanspaceship laser em up
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            hsship.shoot();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    
    
}
