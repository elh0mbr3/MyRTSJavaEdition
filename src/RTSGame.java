import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Point;       // Для роботи з Point
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.PriorityQueue;  // Для PriorityQueue
import java.util.Comparator;    // Для Comparator

/**
 * Main class that sets up the frame and initial panels.
 */
public class RTSGame extends JFrame {

    private ControlPanel controlPanel;
    private GamePanel gamePanel;

    public RTSGame() {
        setTitle("Primitive RTS with Pathfinding, Borders & Path Line");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create the control panel and the main game panel
        controlPanel = new ControlPanel();
        gamePanel = new GamePanel(controlPanel);

        // Use a BorderLayout to place panels
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RTSGame game = new RTSGame();
            game.setVisible(true);
        });
    }
}

/**
 * A simple panel at the top that displays resources and has a button to spawn units.
 * Comments are written in British English.
 */
class ControlPanel extends JPanel {
    private JLabel resourcesLabel;
    private JButton spawnButton;

    private int gold = 100;

    public ControlPanel() {
        // Initialise the control panel with a label and a button
        resourcesLabel = new JLabel("Gold: " + gold);
        spawnButton = new JButton("Spawn Unit");

        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(resourcesLabel);
        add(spawnButton);
    }

    /**
     * Updates the amount of gold and refreshes the label.
     */
    public void updateGold(int amount) {
        gold += amount;
        resourcesLabel.setText("Gold: " + gold);
    }

    public JButton getSpawnButton() {
        return spawnButton;
    }
}

/**
 * The main game panel where the map is drawn and units are displayed.
 * Allows selection of units (using LMB) and movement command via RMB.
 */
class GamePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
    public static final int TILE_SIZE = 32;
    private static final int MAP_WIDTH = 25;
    private static final int MAP_HEIGHT = 15;

    private GameMap gameMap;
    private List<Unit> units;
    private Timer timer;

    // Variables for drag-selection
    private Rectangle selectionRect;
    private boolean isSelecting = false;
    private int selectStartX, selectStartY;

    private ControlPanel controlPanel;

    public GamePanel(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;

        // Initialise the map and create some initial units
        gameMap = new GameMap(MAP_WIDTH, MAP_HEIGHT);
        units = new ArrayList<>();
        units.add(new Unit(100, 100));
        units.add(new Unit(200, 150));

        addMouseListener(this);
        addMouseMotionListener(this);

        // Set up a timer to repeatedly update and repaint the game (approx. 60 FPS)
        timer = new Timer(16, this);
        timer.start();

        // Attach an ActionListener to the spawn button to add new units
        controlPanel.getSpawnButton().addActionListener(e -> {
            // Deduct some gold for spawning
            controlPanel.updateGold(-10);
            // Create a new unit at a random position within the panel's dimensions
            units.add(new Unit(
                    (int) (Math.random() * getWidth()),
                    (int) (Math.random() * getHeight())
            ));
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the map as a grid of tiles with borders
        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                Tile tile = gameMap.getTile(col, row);
                if (tile == Tile.GRASS) {
                    g.setColor(Color.GREEN);
                } else if (tile == Tile.WATER) {
                    g.setColor(Color.BLUE);
                }
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                // Draw border for each tile
                g.setColor(Color.BLACK);
                g.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw all units
        for (Unit unit : units) {
            unit.draw(g);
        }

        // Draw a line from each selected unit to its destination (if a path exists)
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        for (Unit unit : units) {
            if (unit.isSelected() && !unit.getPath().isEmpty()) {
                List<Point> path = unit.getPath();
                int n = path.size();
                int[] xs = new int[n + 1];
                int[] ys = new int[n + 1];
                xs[0] = unit.getX();
                ys[0] = unit.getY();
                for (int i = 0; i < n; i++) {
                    xs[i + 1] = path.get(i).x * TILE_SIZE + TILE_SIZE / 2;
                    ys[i + 1] = path.get(i).y * TILE_SIZE + TILE_SIZE / 2;
                }
                g2.setColor(Color.MAGENTA);
                g2.drawPolyline(xs, ys, n + 1);
            }
        }

        // If we are currently dragging the mouse, show the selection rectangle
        if (isSelecting && selectionRect != null) {
            g.setColor(new Color(0, 0, 255, 50)); // translucent fill
            g.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
            g.setColor(Color.BLUE); // outline
            g.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update the units' positions
        for (Unit unit : units) {
            unit.update();
        }
        // Resolve collisions between units
        for (int i = 0; i < units.size(); i++) {
            for (int j = i + 1; j < units.size(); j++) {
                Unit u1 = units.get(i);
                Unit u2 = units.get(j);
                int dx = u1.getX() - u2.getX();
                int dy = u1.getY() - u2.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                double minDistance = u1.getSize(); // assuming both have the same size
                if (distance < minDistance) {
                    // Avoid division by zero
                    if (distance == 0) {
                        u1.moveBy(1, 0);
                        u2.moveBy(-1, 0);
                    } else {
                        double overlap = (minDistance - distance) / 2.0;
                        double offsetX = (dx / distance) * overlap;
                        double offsetY = (dy / distance) * overlap;
                        u1.moveBy(offsetX, offsetY);
                        u2.moveBy(-offsetX, -offsetY);
                    }
                }
            }
        }
        repaint();
    }

    // ---- Mouse handling for selection (LMB) and movement command (RMB) ----
    @Override
    public void mousePressed(MouseEvent e) {
        // Only process left mouse button for selection
        if (SwingUtilities.isLeftMouseButton(e)) {
            isSelecting = true;
            selectStartX = e.getX();
            selectStartY = e.getY();
            selectionRect = new Rectangle(selectStartX, selectStartY, 0, 0);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Only process selection logic for left mouse button
        if (SwingUtilities.isLeftMouseButton(e)) {
            isSelecting = false;
            if (selectionRect != null) {
                // If the rectangle is very small, treat it as a simple click
                if (selectionRect.width < 5 && selectionRect.height < 5) {
                    Point clickPoint = new Point(selectStartX, selectStartY);
                    boolean found = false;
                    for (Unit unit : units) {
                        if (unit.getBounds().contains(clickPoint)) {
                            unit.setSelected(true);
                            found = true;
                            // If SHIFT is not held, deselect other units
                            if (!e.isShiftDown()) {
                                for (Unit other : units) {
                                    if (other != unit) {
                                        other.setSelected(false);
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (!found && !e.isShiftDown()) {
                        // If click is not on any unit, deselect all
                        for (Unit unit : units) {
                            unit.setSelected(false);
                        }
                    }
                } else {
                    // If this was a drag selection, check intersection with units
                    for (Unit unit : units) {
                        if (selectionRect.intersects(unit.getBounds())) {
                            unit.setSelected(true);
                        } else if (!e.isShiftDown()) {
                            unit.setSelected(false);
                        }
                    }
                }
            }
            selectionRect = null;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Process right mouse button clicks for movement command
        if (SwingUtilities.isRightMouseButton(e)) {
            for (Unit unit : units) {
                if (unit.isSelected()) {
                    unit.setTarget(e.getX(), e.getY(), gameMap);
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Update the dimensions of the selection rectangle if using left button
        if (isSelecting && SwingUtilities.isLeftMouseButton(e)) {
            int x = Math.min(selectStartX, e.getX());
            int y = Math.min(selectStartY, e.getY());
            int width = Math.abs(e.getX() - selectStartX);
            int height = Math.abs(e.getY() - selectStartY);
            selectionRect.setBounds(x, y, width, height);
            repaint();
        }
    }

    @Override public void mouseMoved(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
}

/**
 * A class representing the game map as a 2D array of tiles.
 */
class GameMap {
    private Tile[][] tiles;

    public GameMap(int width, int height) {
        tiles = new Tile[height][width];
        Random rand = new Random();

        // Randomly assign GRASS or WATER to each tile
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (rand.nextDouble() < 0.2) {
                    tiles[row][col] = Tile.WATER;
                } else {
                    tiles[row][col] = Tile.GRASS;
                }
            }
        }
    }

    public Tile getTile(int x, int y) {
        return tiles[y][x];
    }

    public int getWidth() {
        return tiles[0].length;
    }

    public int getHeight() {
        return tiles.length;
    }
}

/**
 * A simple enum representing different tile types.
 */
enum Tile {
    GRASS,
    WATER
}

/**
 * A basic Unit class that can be selected and moved around.
 * The movement now utilises A* pathfinding to avoid obstacles (WATER tiles),
 * and collision resolution is applied so that units do not overlap.
 */
class Unit {
    private int x, y;
    private int speed = 2;
    private boolean selected;
    private int size = 20;
    private List<Point> path = new ArrayList<>();

    public Unit(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Set the target location and compute a path using A* pathfinding.
     * The coordinates are converted to tile coordinates (assuming each tile is 32px).
     * Target coordinates are clamped to the map boundaries.
     */
    public void setTarget(int tx, int ty, GameMap map) {
        int startTileX = x / GamePanel.TILE_SIZE;
        int startTileY = y / GamePanel.TILE_SIZE;
        int goalTileX = tx / GamePanel.TILE_SIZE;
        int goalTileY = ty / GamePanel.TILE_SIZE;

        // Clamp goal coordinates to the map bounds
        goalTileX = Math.max(0, Math.min(goalTileX, map.getWidth() - 1));
        goalTileY = Math.max(0, Math.min(goalTileY, map.getHeight() - 1));

        List<Point> newPath = Pathfinder.findPath(map, new Point(startTileX, startTileY), new Point(goalTileX, goalTileY));

        if (newPath.isEmpty()) {
            System.out.println("No path found from (" + startTileX + ", " + startTileY + ") to (" + goalTileX + ", " + goalTileY + ")");
        } else {
            // If the starting tile is included as the first node, remove it.
            if (!newPath.isEmpty() && newPath.get(0).equals(new Point(startTileX, startTileY))) {
                newPath.remove(0);
            }
        }
        path = newPath;
    }

    /**
     * Update the unit's position along the computed path.
     * The unit moves towards the centre of the next tile in the path.
     */
    public void update() {
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

            // Once the unit is near the centre of the tile, proceed to the next waypoint.
            if (Math.abs(x - nextX) < speed && Math.abs(y - nextY) < speed) {
                x = nextX;
                y = nextY;
                path.remove(0);
            }
        }
    }

    /**
     * Draw the unit, using a different colour if it is selected.
     * A black border is drawn around the unit.
     */
    public void draw(Graphics g) {
        g.setColor(selected ? Color.RED : Color.YELLOW);
        g.fillRect(x - size / 2, y - size / 2, size, size);
        g.setColor(Color.BLACK);
        g.drawRect(x - size / 2, y - size / 2, size, size);
    }

    /**
     * Return the bounding rectangle of the unit for selection.
     */
    public Rectangle getBounds() {
        return new Rectangle(x - size / 2, y - size / 2, size, size);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    // --- Methods for collision resolution and drawing the path ---

    /**
     * Returns the x-coordinate of the unit's centre.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the unit's centre.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the size (width/height) of the unit.
     */
    public int getSize() {
        return size;
    }

    /**
     * Moves the unit by the specified offsets.
     * The offsets are rounded to the nearest integer.
     */
    public void moveBy(double dx, double dy) {
        x += (int) Math.round(dx);
        y += (int) Math.round(dy);
    }

    /**
     * Returns the computed path (list of tile coordinates).
     */
    public List<Point> getPath() {
        return path;
    }
}

/**
 * A simple Pathfinder class implementing the A* algorithm for grid-based pathfinding.
 */
class Pathfinder {

    private static class Node {
        int x, y;
        int g, h, f;
        Node parent;

        Node(int x, int y, int g, int h, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent;
        }
    }

    /**
     * Finds a path from the start to the goal on the provided game map.
     * Only GRASS tiles are considered passable.
     *
     * @param map   The game map.
     * @param start The starting tile (in tile coordinates).
     * @param goal  The target tile (in tile coordinates).
     * @return A list of tile coordinates (as Points) representing the path.
     */
    public static List<Point> findPath(GameMap map, Point start, Point goal) {
        List<Point> path = new ArrayList<>();
        if (start.equals(goal)) {
            return path;
        }

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        boolean[][] closed = new boolean[map.getHeight()][map.getWidth()];

        Node startNode = new Node(start.x, start.y, 0, manhattan(start, goal), null);
        openList.add(startNode);

        Node current = null;

        while (!openList.isEmpty()) {
            current = openList.poll();

            if (current.x == goal.x && current.y == goal.y) {
                break;
            }

            closed[current.y][current.x] = true;

            // Explore neighbouring tiles in 4 directions
            int[][] directions = { {0, 1}, {1, 0}, {0, -1}, {-1, 0} };
            for (int[] d : directions) {
                int nx = current.x + d[0];
                int ny = current.y + d[1];

                if (nx < 0 || ny < 0 || nx >= map.getWidth() || ny >= map.getHeight()) {
                    continue;
                }

                // WATER is considered an obstacle
                if (map.getTile(nx, ny) == Tile.WATER) {
                    continue;
                }

                if (closed[ny][nx]) {
                    continue;
                }

                int gNew = current.g + 1;
                int hNew = manhattan(new Point(nx, ny), goal);
                Node neighbour = new Node(nx, ny, gNew, hNew, current);
                openList.add(neighbour);
            }
        }

        // If the goal was not reached, return an empty path
        if (current == null || (current.x != goal.x || current.y != goal.y)) {
            return path;
        }

        // Reconstruct the path by backtracking from the goal node
        while (current != null) {
            path.add(0, new Point(current.x, current.y));
            current = current.parent;
        }

        return path;
    }

    private static int manhattan(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}