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

    // Anzahl Leben beim Start
    public static final int CONFIG_LIVES = 10;
    // Geschwindigkeit der Aliens beim Start
    public static final double CONFIG_SPEED_ALIENS = SpaceShip.SPEED_SLOW;
    // Geschwindigkeit des menschlichen Raumschiffes beim Start
    public static final double CONFIG_SPEED_HUMANS = SpaceShip.SPEED_FAST;
    // Breite der Alienraumschiffe
    public static final double CONFIG_WIDTH_ALIENS = SpaceShip.WIDTH_MEDIUM;
    // Breite des menschlischen Raumschiffes
    public static final double CONFIG_WIDTH_HUMANS = SpaceShip.WIDTH_SMALL;
    // Maximale Zeit zum Erzeugen eines neuen Alienraumschiffes (in ms)
    public static final long CONFIG_MAX_SPAWN_TIME = 5000l;
    // Maximale Zeit bis die Geschwindigkeit des Spiels erhöht wird
    public static final long CONFIG_INC_LEVEL_TIME = 30000l;

    // Liste in der die Alienraumschiffe verwaltet werden
    private List<AlienSpaceShip> alienSpaceShips;
    // Geschwindigkeit der Alienraumschiffe
    private double alienSpaceShipSpeed;
    // Anzahl der verbleibenden Leben
    private int livesRemaing;
    // Das menschliche Raumschiff
    private HumanSpaceShip humanSpaceShip;

    public GameCanvas() {
        addKeyListener(this);
        this.setIgnoreRepaint(true);
    }

    public void start() {

        // Setzen der Startwerte
        alienSpaceShips = Collections.synchronizedList(new ArrayList<AlienSpaceShip>());
        alienSpaceShipSpeed = CONFIG_SPEED_ALIENS;
        livesRemaing = CONFIG_LIVES;
        humanSpaceShip = null;

        // TripleBuffering
        this.createBufferStrategy(3);
        final BufferStrategy bs = this.getBufferStrategy();

        // Erzeugen des menschlichen Raumschiffes in der Mitte
        int hsshipX = (int) Math.round((this.getBounds().getWidth() / 2) - (CONFIG_WIDTH_HUMANS / 2));
        int hsshipY = (int) Math.round(this.getBounds().getHeight() - 20);
        humanSpaceShip = new HumanSpaceShip(new Point(hsshipX, hsshipY), CONFIG_WIDTH_HUMANS, GameCanvas.this.getBounds(), CONFIG_SPEED_HUMANS);

        // Prozess der Alienraumschiffe erzeugt und startet
        new Thread(new Runnable() {
            Random rg = new Random();

            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep((long) (CONFIG_MAX_SPAWN_TIME * rg.nextDouble()));
                    } catch (InterruptedException ex) {
                    }
                    int y = 1;
                    int x = (int) Math.round(GameCanvas.this.getBounds().getWidth() * rg.nextDouble());
                    AlienSpaceShip newShip = new AlienSpaceShip(new Point(x, y), CONFIG_WIDTH_ALIENS, GameCanvas.this.getBounds(), alienSpaceShipSpeed);
                    alienSpaceShips.add(newShip);
                    newShip.fly();
                }

            }
        }, "generateAlienSpaceShips").start();

        // Prozess der die Spielgeschwindigkeit erhöht
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
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

        // Prozess der die Spiellogik enthält und die Objekte zeichnet
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    // Kantenglättung aktivieren
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // schwarzen Hintergrund zeichnen
                    g.setColor(Color.black);
                    g.fillRect(0, 0, GameCanvas.this.getBounds().width, GameCanvas.this.getBounds().height);
                    // Menschliches Raumschiff als weißes Polygon (Dreieck) zeichnen
                    int x = humanSpaceShip.getPosition().x;
                    int y = humanSpaceShip.getPosition().y;
                    g.setColor(Color.white);
                    Polygon p = new Polygon();
                    Point hssLeft = new Point(x, y);
                    Point hssRight = new Point(x + (int) Math.round(humanSpaceShip.getWidth()), y);
                    Point hssFront = new Point(x + (int) Math.round(humanSpaceShip.getWidth() / 2), y - 10);
                    p.addPoint(hssLeft.x, hssLeft.y);
                    p.addPoint(hssRight.x, hssRight.y);
                    p.addPoint(hssFront.x, hssFront.y);
                    g.fillPolygon(p);
                    // falls das menschliche Raumschiff schiesst: Laser zeichnen als gelbe Linie
                    if (humanSpaceShip.isShooting()) {
                        g.setColor(Color.yellow);
                        g.drawLine(hssFront.x, hssFront.y, hssFront.x, getBounds().y);
                    }
                    // Alle Alienraumschiffe durchlaufen, Kollisionen, Treffer oder Erdankunft erkennen und behandeln
                    synchronized (alienSpaceShips) {
                        Iterator<AlienSpaceShip> it = alienSpaceShips.iterator();
                        while (it.hasNext()) {
                            AlienSpaceShip ship = it.next();
                            // Wurde das Alienraumschiff von Laser getroffen?
                            if (humanSpaceShip.isShooting()
                                    && ship.getPosition().x <= hssFront.x
                                    && (ship.getPosition().x + (int) Math.round(ship.getWidth())) >= hssFront.x) {
                                // Ja! Alienraumschiff zerstören
                                ship.destroy();
                                it.remove();
                                continue;
                            }
                            // Hat das Alienraumschiff die Erde erreicht?
                            if (ship.getPosition().y > getBounds().getHeight()) {
                                // Ja! Alienraumschiff zerstören, Leben herunterzählen
                                ship.destroy();
                                it.remove();
                                livesRemaing--;
                                continue;
                            }
                            // Ist das Alienraumschiff mit dem menschlichen Raumschiff kollidiert?
                            Rectangle2D alienSpaceShipShapeCollisionModel = new Rectangle2D.Double(ship.getPosition().x, ship.getPosition().y, (int) Math.round(ship.getWidth()), 10d);
                            Ellipse2D.Double alienSpaceShipShape = new Ellipse2D.Double(ship.getPosition().x, ship.getPosition().y, (int) Math.round(ship.getWidth()), 10d);
                            if (p.intersects(alienSpaceShipShapeCollisionModel)) {
                                // Ja! Alienraumschiff zerstören, Leben herunterzählen
                                ship.destroy();
                                it.remove();
                                livesRemaing--;
                                continue;
                            }
                            // Zeichnen des Alienraumschiffes, falls noch vorhanden
                            g.setColor(Color.red);
                            g.fill(alienSpaceShipShape);
                        }
                    }
                    if (livesRemaing >= 0) {
                        // Anzeige der verbleibenden Leben
                        g.setColor(Color.GREEN);
                        g.drawString(String.valueOf(livesRemaing), 25, 25);
                    } else {
                        // oder Game Over in der Mitte anzeigen und Spiel beenden
                        FontMetrics fm = g.getFontMetrics();
                        String gameOverStr = "GAME OVER";
                        Rectangle2D gameOverStrBounds = fm.getStringBounds(gameOverStr, g);
                        g.setColor(Color.red);
                        int gameOverX = (int) Math.round((getBounds().getWidth() / 2) - (gameOverStrBounds.getWidth() / 2));
                        int gameOverY = (int) Math.round((getBounds().getHeight() / 2) - (gameOverStrBounds.getHeight() / 2));
                        g.drawString(gameOverStr, gameOverX, gameOverY);
                        // Im Falle eines GameOvers den Buffer zeichnen
                        g.dispose();
                        bs.show();
                        // Hauptschleife beenden
                        break;
                    }
                    // Nach jedem Durchlauf Buffer zeichnen
                    g.dispose();
                    bs.show();
                }
                // Aufräumen aufrufen
                cleanUpAfterGame();
            }
        }, "drawEverything").start();

        this.requestFocus();
    }

    /**
     * Zerstören der Prozesse für alle Raumschiffe (Mensch, Aliens)
     */
    private void cleanUpAfterGame() {
        humanSpaceShip.destroy();
        for (AlienSpaceShip ship : alienSpaceShips) {
            ship.destroy();
        }
        alienSpaceShips.clear();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Bei Escape das Programm beenden
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
        // Bei rechter Pfeiltaste das Raumschiff nach rechts bewegen
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            humanSpaceShip.moveRight();
        }
        // Bei linker Pfeiltaste das Raumschiff nach links bewegen
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            humanSpaceShip.moveLeft();
        }
        // Bei Leertaste den Laser des menschlichen Raumschiffes feuern
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            humanSpaceShip.shoot();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
