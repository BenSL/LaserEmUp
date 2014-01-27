package laseremup;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameCanvas extends Canvas implements KeyListener {
    
    public static final int         CONFIG_LIVES = 2;
    public static final double      CONFIG_SPEED_ALIENS = SpaceShip.SPEED_FAST;
    public static final double      CONFIG_SPEED_HUMANS = SpaceShip.SPEED_FAST;
    public static final double      CONFIG_WIDTH_ALIENS = SpaceShip.WIDTH_MEDIUM;
    public static final double      CONFIG_WIDTH_HUMANS = SpaceShip.WIDTH_SMALL;
    public static final long        CONFIG_MAX_SPAWN_TIME = 5000l;
    public static final long        CONFIG_INC_LEVEL_TIME = 30000l;
    
    private List<AlienSpaceShip> alienSpaceShips;
    private double alienSpaceShipSpeed;
    private int livesRemaing;
    private HumanSpaceShip humanSpaceShip;

    public GameCanvas() {
        addKeyListener(this);
        this.setIgnoreRepaint(true);
    }
    
    public void start() {
        
        // init game variables
        alienSpaceShips = Collections.synchronizedList(new ArrayList<AlienSpaceShip>());
        alienSpaceShipSpeed = CONFIG_SPEED_ALIENS;
        livesRemaing = CONFIG_LIVES;
        humanSpaceShip = null;
        
        // 3 buffers
        this.createBufferStrategy(3);
        final BufferStrategy bs = this.getBufferStrategy();
        
        // humanspaceship
        int hsshipX = (int) Math.round((this.getBounds().getWidth()/2)-(CONFIG_WIDTH_HUMANS/2));
        int hsshipY = (int) Math.round(this.getBounds().getHeight() - 20);
        humanSpaceShip = new HumanSpaceShip(new Point(hsshipX, hsshipY), CONFIG_WIDTH_HUMANS, GameCanvas.this.getBounds(), CONFIG_SPEED_HUMANS);
        
        // generate new alienspaceships < max spawn time
        new Thread(new Runnable() {
            
            Random rg = new Random();

            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    try {
                        Thread.sleep((long) (CONFIG_MAX_SPAWN_TIME * rg.nextDouble()));
                    } catch (InterruptedException ex) {}
                    int y = 1;
                    int x = (int) Math.round(GameCanvas.this.getBounds().getWidth() * rg.nextDouble());
                    AlienSpaceShip newShip = new AlienSpaceShip(new Point(x, y), CONFIG_WIDTH_ALIENS, GameCanvas.this.getBounds(), alienSpaceShipSpeed);
                    alienSpaceShips.add(newShip);
                    newShip.fly();
                }
                
            }
        }, "generateAlienSpaceShips").start();
        
        // increase speed = level time
        new Thread(new Runnable() {

            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    try {
                        Thread.sleep(CONFIG_INC_LEVEL_TIME);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    // increasing speed 20 percent
                    alienSpaceShipSpeed = 1.2d * alienSpaceShipSpeed;
                    humanSpaceShip.setSpeed(1.2d * humanSpaceShip.getSpeed());
                }
            }
        }, "increaseAlienSpaceShipSpeed").start();
        
        
        
        // game logic & paint everything
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
                    int x = humanSpaceShip.getPosition().x;
                    int y = humanSpaceShip.getPosition().y;
                    g.setColor(Color.white);
                    Polygon p = new Polygon();
                    Point hssLeft = new Point(x, y);
                    Point hssRight = new Point(x + (int) Math.round(humanSpaceShip.getWidth()), y);
                    Point hssFront = new Point(x + (int) Math.round(humanSpaceShip.getWidth()/2), y-10);
                    p.addPoint(hssLeft.x, hssLeft.y);
                    p.addPoint(hssRight.x, hssRight.y);
                    p.addPoint(hssFront.x, hssFront.y);
                    g.fillPolygon(p);
                    // draw laser
                    if(humanSpaceShip.isShooting()) {
                        g.setColor(Color.yellow);
                        g.drawLine(hssFront.x, hssFront.y, hssFront.x, getBounds().y);
                    }
                    // draw & destroy alienspaceships by lazer
                    synchronized(alienSpaceShips) {
                        Iterator<AlienSpaceShip> it = alienSpaceShips.iterator();
                        while(it.hasNext()) {
                            AlienSpaceShip ship = it.next();
                            // alien ship hidden by laser
                            if(humanSpaceShip.isShooting() 
                                    && ship.getPosition().x <= hssFront.x 
                                    && (ship.getPosition().x+(int) Math.round(ship.getWidth())) >= hssFront.x) {
                                // destroy ship
                                ship.destroy();
                                it.remove();
                                continue;
                            }
                            // alien ship reached earth -> 1 down
                            if(ship.getPosition().y > getBounds().getHeight()) {
                                ship.destroy();
                                it.remove();
                                livesRemaing--;
                                continue;
                            }
                            // alien ship collides with humanspaceship
                            Rectangle2D alienSpaceShipShapeCollisionModel = new Rectangle2D.Double(ship.getPosition().x, ship.getPosition().y, (int) Math.round(ship.getWidth()), 10d);
                            Ellipse2D.Double alienSpaceShipShape = new Ellipse2D.Double(ship.getPosition().x, ship.getPosition().y, (int) Math.round(ship.getWidth()), 10d);
                            if(p.intersects(alienSpaceShipShapeCollisionModel)) {
                                ship.destroy();
                                it.remove();
                                livesRemaing--;
                                continue;
                            }
                            // draw alien ship
                            g.setColor(Color.red);
                            g.fill(alienSpaceShipShape);
                        }
                    }
                    // draw remaining lives or end game
                    if(livesRemaing >= 0) {
                        g.setColor(Color.GREEN);
                        g.drawString(String.valueOf(livesRemaing), 25, 25);
                    }else {
                        FontMetrics fm = g.getFontMetrics();
                        String gameOverStr = "GAME OVER";
                        Rectangle2D gameOverStrBounds = fm.getStringBounds(gameOverStr, g);
                        g.setColor(Color.red);
                        int gameOverX = (int) Math.round((getBounds().getWidth()/2) - (gameOverStrBounds.getWidth()/2));
                        int gameOverY = (int) Math.round((getBounds().getHeight()/2) - (gameOverStrBounds.getHeight()/2));
                        g.drawString(gameOverStr, gameOverX, gameOverY);
                        // write out buffer
                        g.dispose();
                        bs.show();
                        break;
                    }
                    // into buffer
                    g.dispose();
                    bs.show();
                }
                cleanUpAfterGame();
            }
        }, "drawEverything").start();
        
        this.requestFocus();
    }
    
    private void cleanUpAfterGame() {
        humanSpaceShip.destroy();
        for(AlienSpaceShip ship : alienSpaceShips) {
            ship.destroy();
        }
        alienSpaceShips.clear();
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
            humanSpaceShip.moveRight();
        }
        // move humanspaceship left
        if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            humanSpaceShip.moveLeft();
        }
        // make humanspaceship laser em up
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            humanSpaceShip.shoot();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    
    
}
