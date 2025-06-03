import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a unit that can be selected and moved around.
 * Uses A* pathfinding to avoid WATER tiles, and collision resolution to prevent overlap.
 */
public class Unit {
    private int x, y;
    private int speed = 2;
    private boolean selected;
    private int size = 20;
    private List<Point> path = new ArrayList<>();
    private int targetTileX = -1, targetTileY = -1;
    private int prevX, prevY, stuckCounter;

    // Placeholder stat fields
    private int attack = 10;
    private int defense = 5;
    private int intellect = 3;
    private int hp = 100;

    /**
     * Constructs a unit at the specified coordinates.
     * @param x The initial x-coordinate (in pixels).
     * @param y The initial y-coordinate (in pixels).
     */
    public Unit(int x, int y) {
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
    }

    /**
     * Sets the target location and computes a path using A* pathfinding.
     * Coordinates are converted to tile coordinates (assuming each tile is 32px) and clamped to map boundaries.
     * @param tx The target x-coordinate (in pixels).
     * @param ty The target y-coordinate (in pixels).
     * @param map The game map used for pathfinding.
     */
    public void setTarget(int tx, int ty, GameMap map) {
        int startTileX = x / GamePanel.TILE_SIZE;
        int startTileY = y / GamePanel.TILE_SIZE;
        int goalTileX = tx / GamePanel.TILE_SIZE;
        int goalTileY = ty / GamePanel.TILE_SIZE;
        goalTileX = Math.max(0, Math.min(goalTileX, map.getWidth() - 1));
        goalTileY = Math.max(0, Math.min(goalTileY, map.getHeight() - 1));
        this.targetTileX = goalTileX;
        this.targetTileY = goalTileY;
        List<Point> newPath = Pathfinder.findPath(map, new Point(startTileX, startTileY), new Point(goalTileX, goalTileY));
        if (!newPath.isEmpty() && newPath.get(0).equals(new Point(startTileX, startTileY))) {
            newPath.remove(0);
        }
        path = newPath;
    }

    /**
     * Updates the unit's position along the computed path.
     * The unit moves toward the centre of the next tile in the path.
     */
    public void update(GameMap map) {
        prevX = x;
        prevY = y;
        if (!path.isEmpty()) {
            Point nextTile = path.get(0);
            int nextX = nextTile.x * GamePanel.TILE_SIZE + GamePanel.TILE_SIZE / 2;
            int nextY = nextTile.y * GamePanel.TILE_SIZE + GamePanel.TILE_SIZE / 2;
            if (x < nextX) {
                x += Math.min(speed, nextX - x);
            } else if (x > nextX) {
                x -= Math.min(speed, x - nextX);
            }
            if (y < nextY) {
                y += Math.min(speed, nextY - y);
            } else if (y > nextY) {
                y -= Math.min(speed, y - nextY);
            }
            if (Math.abs(x - nextX) < speed && Math.abs(y - nextY) < speed) {
                x = nextX;
                y = nextY;
                path.remove(0);
            }
        }
        if (x == prevX && y == prevY && !path.isEmpty()) {
            stuckCounter++;
            if (stuckCounter > 15) {
                recalcPath(map);
                stuckCounter = 0;
            }
        } else {
            stuckCounter = 0;
        }
    }

    private void recalcPath(GameMap map) {
        if (targetTileX < 0 || targetTileY < 0) return;
        int startTileX = x / GamePanel.TILE_SIZE;
        int startTileY = y / GamePanel.TILE_SIZE;
        List<Point> newPath = Pathfinder.findPath(map, new Point(startTileX, startTileY), new Point(targetTileX, targetTileY));
        if (!newPath.isEmpty() && newPath.get(0).equals(new Point(startTileX, startTileY))) {
            newPath.remove(0);
        }
        path = newPath;
    }

    /**
     * Draws the unit on the provided Graphics object.
     * The unit is filled with yellow (or red if selected) and outlined in black.
     * @param g The Graphics object used for drawing.
     */
    public void draw(Graphics g) {
        g.setColor(selected ? Color.RED : Color.YELLOW);
        g.fillRect(x - size / 2, y - size / 2, size, size);
        g.setColor(Color.BLACK);
        g.drawRect(x - size / 2, y - size / 2, size, size);
    }

    /**
     * Returns the bounding rectangle of the unit (used for selection).
     * @return A Rectangle representing the unit's bounds.
     */
    public Rectangle getBounds() {
        return new Rectangle(x - size / 2, y - size / 2, size, size);
    }

    // Getters and setters

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    /**
     * Moves the unit by the specified offsets.
     * @param dx The offset in the x-direction.
     * @param dy The offset in the y-direction.
     */
    public void moveBy(double dx, double dy) {
        x += (int) Math.round(dx);
        y += (int) Math.round(dy);
    }

    /**
     * Returns the computed path (a list of tile coordinates).
     * @return The list of Points representing the path.
     */
    public List<Point> getPath() {
        return path;
    }

    // Stat getters (placeholder values)
    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getIntellect() {
        return intellect;
    }

    public int getHP() {
        return hp;
    }
}
