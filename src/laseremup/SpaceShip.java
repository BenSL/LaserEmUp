package laseremup;

import java.awt.Point;
import java.awt.Rectangle;

public abstract class SpaceShip {
    
    public static final double SPEED_SLOW = 1d;
    public static final double SPEED_MEDIUM = 2d;
    public static final double SPEED_FAST = 4d;
    
    public static final double WIDTH_SMALL = 35d;
    public static final double WIDTH_MEDIUM = 50d;
    public static final double WIDTH_BIG = 65d;
    
    protected Rectangle bounds;
    protected Point position;
    protected double speed;
    protected double width;

    public SpaceShip(Point position, double speed, double width, Rectangle bounds) {
        this.bounds = bounds;
        this.position = position;
        this.speed = speed;
        this.width = width;
    }

    public void fly() {}
    
    public void destroy() {}

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }
    
}
