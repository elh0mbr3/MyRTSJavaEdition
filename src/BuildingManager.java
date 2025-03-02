import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BuildingManager handles construction and rendering of buildings.
 */
public class BuildingManager {
    private List<Building> buildings;

    public BuildingManager() {
        buildings = new ArrayList<>();
    }

    /**
     * Adds a new building to the game.
     * @param building The building to add.
     */
    public void addBuilding(Building building) {
        buildings.add(building);
    }

    /**
     * Draws all buildings on the screen.
     * @param g The Graphics object used for drawing.
     */
    public void drawBuildings(Graphics g) {
        for (Building b : buildings) {
            b.draw(g);
        }
    }

    /**
     * Returns the list of buildings.
     * @return List of buildings.
     */
    public List<Building> getBuildings() {
        return buildings;
    }
}

/**
 * Represents a building on the game map.
 */
class Building {
    private int x, y, width, height;
    private String type;

    /**
     * Constructs a building.
     * @param x The x-coordinate of the building (top-left).
     * @param y The y-coordinate of the building (top-left).
     * @param width The width of the building.
     * @param height The height of the building.
     * @param type The type or name of the building.
     */
    public Building(int x, int y, int width, int height, String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    /**
     * Draws the building with a dark grey fill, a black border, and a type label.
     * @param g The Graphics object used for drawing.
     */
    public void draw(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        g.setColor(Color.WHITE);
        g.drawString(type, x + 5, y + 15);
    }

    /**
     * Checks if the given point is inside the building's bounds.
     * @param p The point to check.
     * @return True if the point is within the building, false otherwise.
     */
    public boolean contains(Point p) {
        return new Rectangle(x, y, width, height).contains(p);
    }

    // Optional getters for building properties
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}