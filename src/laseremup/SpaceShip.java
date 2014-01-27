package laseremup;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Definition von Raumschiffen im Allgemeinen
 */
public abstract class SpaceShip {
    
    // Geschindigkeitskonstanten
    public static final double SPEED_SLOW = 1d;
    public static final double SPEED_MEDIUM = 2d;
    public static final double SPEED_FAST = 8d;
    
    // verschiedene Breiten
    public static final double WIDTH_SMALL = 35d;
    public static final double WIDTH_MEDIUM = 50d;
    public static final double WIDTH_BIG = 65d;
    
    // Größe der Leinwand
    protected Rectangle bounds;
    // Position des Raumschiffes
    protected Point position;
    // Geschwindigkeit des Raumschiffes
    protected double speed;
    // Breite des Raumschiffes
    protected double width;

    public SpaceShip(Point position, double width, Rectangle bounds) {
        this.bounds = bounds;
        this.position = position;
        this.width = width;
    }

    public abstract void destroy();

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
}
