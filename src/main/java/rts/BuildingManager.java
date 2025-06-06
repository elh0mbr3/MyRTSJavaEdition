package rts;
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
     * @param map   The game map for checking valid spawn locations.
     */
    public void updateBuildings(List<Unit> units, GameMap map) {
        for (Building b : buildings) {
            b.update(units, map);
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

    public void update(List<Unit> units, GameMap map) {
        if (type == BuildingType.BARRACKS && queue > 0) {
            buildTimer++;
            if (buildTimer >= TRAIN_TIME) {
                buildTimer = 0;
                queue--;
                Point spawn = findSpawnPoint(map);
                units.add(new Unit(spawn.x, spawn.y));
            }
        }
    }

    /**
     * Finds a valid spawn location adjacent to this building. If no grass tile
     * is available, the unit is spawned at the building centre as a fallback.
     */
    private Point findSpawnPoint(GameMap map) {
        int baseTileX = x / GamePanel.TILE_SIZE;
        int baseTileY = y / GamePanel.TILE_SIZE;
        int tilesWide = width / GamePanel.TILE_SIZE;
        int tilesHigh = height / GamePanel.TILE_SIZE;
        int centerX = baseTileX + tilesWide / 2;
        int centerY = baseTileY + tilesHigh / 2;
        int[][] dirs = { {1,0}, {-1,0}, {0,1}, {0,-1}, {1,1}, {-1,1}, {1,-1}, {-1,-1} };
        for (int[] d : dirs) {
            int tx = centerX + d[0];
            int ty = centerY + d[1];
            if (tx >= 0 && ty >= 0 && tx < map.getWidth() && ty < map.getHeight()) {
                if (map.getTile(tx, ty) == Tile.GRASS) {
                    return new Point(tx * GamePanel.TILE_SIZE + GamePanel.TILE_SIZE/2,
                                     ty * GamePanel.TILE_SIZE + GamePanel.TILE_SIZE/2);
                }
            }
        }
        return new Point(x + width / 2, y + height / 2);
    }

    // Optional getters for building properties
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}