import java.awt.*;
import java.util.ArrayList;
import java.util.List;

enum BuildingType {
    BARRACKS,
    DEPOT,
    TOWER
}

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

    /**
     * Returns the building at the given point, or null if none exists.
     */
    public Building getBuildingAt(Point p) {
        for(Building b : buildings) {
            if(b.contains(p)) return b;
        }
        return null;
    }

    /**
     * Updates all buildings and spawns units if training is complete.
     */
    public void updateBuildings(List<Unit> units) {
        for(Building b : buildings) {
            b.update(units);
        }
    }
}

/**
 * Represents a building on the game map.
 */
class Building {
    private int x, y, width, height;
    private BuildingType type;
    private int queue = 0;
    private int buildTimer = 0;
    private static final int TRAIN_TIME = 120;

    /**
     * Constructs a building.
     * @param x The x-coordinate of the building (top-left).
     * @param y The y-coordinate of the building (top-left).
     * @param width The width of the building.
     * @param height The height of the building.
     * @param type The type or name of the building.
     */
    public Building(int x, int y, int width, int height, BuildingType type) {
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
        g.drawString(type.toString(), x + 5, y + 15);
    }

    /**
     * Checks if the given point is inside the building's bounds.
     * @param p The point to check.
     * @return True if the point is within the building, false otherwise.
     */
    public boolean contains(Point p) {
        return new Rectangle(x, y, width, height).contains(p);
    }

    public BuildingType getType() { return type; }

    public void queueUnit() {
        if(type == BuildingType.BARRACKS) {
            queue++;
        }
    }

    public void update(List<Unit> units) {
        if(type == BuildingType.BARRACKS && queue > 0) {
            buildTimer++;
            if(buildTimer >= TRAIN_TIME) {
                buildTimer = 0;
                queue--;
                units.add(new Unit(x + width/2, y + height/2));
            }
        }
    }

    // Optional getters for building properties
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}