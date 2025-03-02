import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Point;       // For working with Point
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.PriorityQueue;  // For PriorityQueue
import java.util.Comparator;      // For Comparator

/**
 * Main class that sets up the frame and initial panels,
 * approximating a Warcraft II–style UI.
 */
public class RTSGame extends JFrame {

    private ResourceBar resourceBar;      // Top resource panel
    private GamePanel gamePanel;          // Center game area
    private BottomPanel bottomPanel;      // Bottom panel with mini-map and unit commands

    public RTSGame() {
        setTitle("RTS with Warcraft II–style UI");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        resourceBar = new ResourceBar();
        gamePanel = new GamePanel(resourceBar);
        bottomPanel = new BottomPanel(gamePanel, resourceBar);

        setLayout(new BorderLayout());
        add(resourceBar, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RTSGame game = new RTSGame();
            game.setVisible(true);
        });
    }
}

/**
 * ResourceBar displays resources and action buttons at the top.
 */
class ResourceBar extends JPanel {
    JLabel goldLabel, woodLabel, oilLabel;
    JButton spawnButton, buildButton;
    int gold = 500, wood = 200, oil = 50;

    public ResourceBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));

        goldLabel = new JLabel("Gold: " + gold);
        woodLabel = new JLabel("Wood: " + wood);
        oilLabel  = new JLabel("Oil: " + oil);
        spawnButton = new JButton("Spawn Unit");
        buildButton = new JButton("Build");

        add(goldLabel);
        add(woodLabel);
        add(oilLabel);
        add(Box.createHorizontalStrut(40));
        add(spawnButton);
        add(buildButton);

        setBackground(new Color(80,80,80));
        goldLabel.setForeground(Color.WHITE);
        woodLabel.setForeground(Color.WHITE);
        oilLabel.setForeground(Color.WHITE);
    }

    public JButton getSpawnButton() { return spawnButton; }
    public JButton getBuildButton() { return buildButton; }
    public void updateGold(int amount) {
        gold += amount;
        goldLabel.setText("Gold: " + gold);
    }
    public int getGold() { return gold; }
}

/**
 * GamePanel draws the game map, units, and buildings.
 * It handles mouse interactions for unit selection, movement, and building placement.
 */
class GamePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
    public static final int TILE_SIZE = 32;
    private static final int MAP_WIDTH = 25;
    private static final int MAP_HEIGHT = 15;

    private GameMap gameMap;
    private List<Unit> units;           // Unit class is defined in Unit.java
    private BuildingManager buildingManager;  // Defined in BuildingMechanic.java

    private Timer timer;

    // Variables for drag-selection
    private Rectangle selectionRect;
    private boolean isSelecting = false;
    private int selectStartX, selectStartY;

    private ResourceBar resourceBar;
    private boolean buildMode = false; // Flag for build mode

    public GamePanel(ResourceBar resourceBar) {
        this.resourceBar = resourceBar;
        gameMap = new GameMap(MAP_WIDTH, MAP_HEIGHT);
        units = new ArrayList<>();
        units.add(new Unit(100, 100));
        units.add(new Unit(200, 150));
        buildingManager = new BuildingManager();

        addMouseListener(this);
        addMouseMotionListener(this);

        timer = new Timer(16, this);
        timer.start();

        // Spawn button action (for testing; later you may remove this)
        resourceBar.getSpawnButton().addActionListener(e -> {
            resourceBar.updateGold(-10);
            units.add(new Unit(
                    (int)(Math.random() * getWidth()),
                    (int)(Math.random() * getHeight())
            ));
        });

        // Build button toggles build mode
        resourceBar.getBuildButton().addActionListener(e -> {
            if(resourceBar.getGold() >= 20) {
                buildMode = true;
                JOptionPane.showMessageDialog(this,
                        "Build mode activated. Click on the map to place a building.");
            } else {
                JOptionPane.showMessageDialog(this, "Not enough gold to build.");
            }
        });
    }

    public GameMap getGameMap() { return gameMap; }
    public List<Unit> getUnits() { return units; }

    // Returns the first selected unit, if any
    public Unit getSelectedUnit() {
        for(Unit u : units) {
            if(u.isSelected()) return u;
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the map as a grid of tiles with borders
        for(int row = 0; row < MAP_HEIGHT; row++) {
            for(int col = 0; col < MAP_WIDTH; col++) {
                Tile tile = gameMap.getTile(col, row);
                g.setColor(tile == Tile.GRASS ? Color.GREEN : Color.BLUE);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        // Draw buildings
        buildingManager.drawBuildings(g);
        // Draw units
        for(Unit unit : units) {
            unit.draw(g);
        }
        // Draw path lines for selected units
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(new BasicStroke(2));
        for(Unit unit : units) {
            if(unit.isSelected() && !unit.getPath().isEmpty()) {
                List<Point> path = unit.getPath();
                int n = path.size();
                int[] xs = new int[n+1];
                int[] ys = new int[n+1];
                xs[0] = unit.getX();
                ys[0] = unit.getY();
                for(int i = 0; i < n; i++) {
                    xs[i+1] = path.get(i).x * TILE_SIZE + TILE_SIZE/2;
                    ys[i+1] = path.get(i).y * TILE_SIZE + TILE_SIZE/2;
                }
                g2.setColor(Color.MAGENTA);
                g2.drawPolyline(xs, ys, n+1);
            }
        }
        // Draw selection rectangle if dragging
        if(isSelecting && selectionRect != null) {
            g.setColor(new Color(0,0,255,50));
            g.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
            g.setColor(Color.BLUE);
            g.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update all units
        for(Unit unit : units) {
            unit.update();
        }
        // Simple collision resolution between units
        for(int i = 0; i < units.size(); i++) {
            for(int j = i+1; j < units.size(); j++) {
                Unit u1 = units.get(i);
                Unit u2 = units.get(j);
                int dx = u1.getX() - u2.getX();
                int dy = u1.getY() - u2.getY();
                double dist = Math.sqrt(dx*dx+dy*dy);
                double minDist = u1.getSize();
                if(dist < minDist) {
                    if(dist == 0) {
                        u1.moveBy(1,0);
                        u2.moveBy(-1,0);
                    } else {
                        double overlap = (minDist - dist)/2.0;
                        double ox = (dx/dist)*overlap;
                        double oy = (dy/dist)*overlap;
                        u1.moveBy(ox,oy);
                        u2.moveBy(-ox,-oy);
                    }
                }
            }
        }
        repaint();
    }

    // ---- Mouse handling ----
    @Override
    public void mousePressed(MouseEvent e) {
        if(buildMode && SwingUtilities.isLeftMouseButton(e)) {
            int bx = e.getX();
            int by = e.getY();
            int tileX = bx / TILE_SIZE;
            int tileY = by / TILE_SIZE;
            if(tileX >= 0 && tileX < gameMap.getWidth() && tileY >= 0 && tileY < gameMap.getHeight()) {
                if(gameMap.getTile(tileX, tileY) == Tile.GRASS) {
                    buildingManager.addBuilding(
                            new Building(tileX * TILE_SIZE, tileY * TILE_SIZE, 64, 64, "Building")
                    );
                    resourceBar.updateGold(-20);
                } else {
                    JOptionPane.showMessageDialog(this, "Cannot build on water!");
                }
            }
            buildMode = false;
            return;
        }
        if(SwingUtilities.isLeftMouseButton(e)) {
            isSelecting = true;
            selectStartX = e.getX();
            selectStartY = e.getY();
            selectionRect = new Rectangle(selectStartX, selectStartY, 0, 0);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(buildMode) return;
        if(SwingUtilities.isLeftMouseButton(e)) {
            isSelecting = false;
            if(selectionRect != null) {
                if(selectionRect.width < 5 && selectionRect.height < 5) {
                    Point clickPoint = new Point(selectStartX, selectStartY);
                    boolean found = false;
                    for(Unit unit : units) {
                        if(unit.getBounds().contains(clickPoint)) {
                            unit.setSelected(true);
                            found = true;
                            if(!e.isShiftDown()) {
                                for(Unit other : units) {
                                    if(other != unit) {
                                        other.setSelected(false);
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if(!found && !e.isShiftDown()) {
                        for(Unit unit : units) {
                            unit.setSelected(false);
                        }
                    }
                } else {
                    for(Unit unit : units) {
                        if(selectionRect.intersects(unit.getBounds())) {
                            unit.setSelected(true);
                        } else if(!e.isShiftDown()) {
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
        if(SwingUtilities.isRightMouseButton(e)) {
            for(Unit unit : units) {
                if(unit.isSelected()) {
                    unit.setTarget(e.getX(), e.getY(), gameMap);
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(isSelecting && SwingUtilities.isLeftMouseButton(e)) {
            int x = Math.min(selectStartX, e.getX());
            int y = Math.min(selectStartY, e.getY());
            int w = Math.abs(e.getX() - selectStartX);
            int h = Math.abs(e.getY() - selectStartY);
            selectionRect.setBounds(x, y, w, h);
            repaint();
        }
    }

    @Override public void mouseMoved(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
}

/**
 * BottomPanel contains the mini-map and unit commands.
 */
class BottomPanel extends JPanel {
    private MiniMapPanel miniMap;
    private UnitCommandsPanel commandsPanel;

    public BottomPanel(GamePanel gamePanel, ResourceBar resourceBar) {
        setLayout(new BorderLayout());
        miniMap = new MiniMapPanel(gamePanel);
        commandsPanel = new UnitCommandsPanel(gamePanel, resourceBar);
        add(miniMap, BorderLayout.WEST);
        add(commandsPanel, BorderLayout.CENTER);
        setBackground(new Color(60,60,60));
        setPreferredSize(new Dimension(1000,150));
    }
}

/**
 * MiniMapPanel displays a scaled-down version of the game map.
 */
class MiniMapPanel extends JPanel {
    private GamePanel gamePanel;

    public MiniMapPanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        setPreferredSize(new Dimension(200,150));
        setBackground(new Color(30,30,30));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        GameMap map = gamePanel.getGameMap();
        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();
        int miniTileSize = Math.min(getWidth()/mapWidth, getHeight()/mapHeight);
        for(int row = 0; row < mapHeight; row++) {
            for(int col = 0; col < mapWidth; col++) {
                Tile tile = map.getTile(col, row);
                g.setColor(tile == Tile.GRASS ? Color.GREEN : Color.BLUE);
                g.fillRect(col * miniTileSize, row * miniTileSize, miniTileSize, miniTileSize);
            }
        }
    }
}

/**
 * UnitCommandsPanel displays command buttons and unit stats (Attack, Defense, Intellect, HP)
 * for the currently selected unit. (Placeholder values are used for now.)
 */
class UnitCommandsPanel extends JPanel {
    private GamePanel gamePanel;
    private ResourceBar resourceBar;
    private JButton attackButton, stopButton, patrolButton;
    private JLabel attackLabel, defenseLabel, intellectLabel, hpLabel;

    public UnitCommandsPanel(GamePanel gamePanel, ResourceBar resourceBar) {
        this.gamePanel = gamePanel;
        this.resourceBar = resourceBar;
        setPreferredSize(new Dimension(800,150));
        setLayout(new FlowLayout(FlowLayout.LEFT,20,20));
        setBackground(new Color(50,50,50));

        attackButton = new JButton("Attack");
        stopButton = new JButton("Stop");
        patrolButton = new JButton("Patrol");

        attackLabel = new JLabel("Attack: -");
        defenseLabel = new JLabel("Defense: -");
        intellectLabel = new JLabel("Intellect: -");
        hpLabel = new JLabel("HP: -");

        add(attackButton);
        add(stopButton);
        add(patrolButton);
        add(attackLabel);
        add(defenseLabel);
        add(intellectLabel);
        add(hpLabel);

        attackButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Attack command clicked! (Not implemented)"));
        stopButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Stop command clicked! (Not implemented)"));
        patrolButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Patrol command clicked! (Not implemented)"));

        Timer statTimer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Unit selected = gamePanel.getSelectedUnit();
                if(selected != null) {
                    updateUnitStats(selected);
                } else {
                    attackLabel.setText("Attack: -");
                    defenseLabel.setText("Defense: -");
                    intellectLabel.setText("Intellect: -");
                    hpLabel.setText("HP: -");
                }
            }
        });
        statTimer.start();
    }

    /**
     * Updates the stat labels with values from the selected unit.
     * @param unit The selected unit.
     */
    public void updateUnitStats(Unit unit) {
        attackLabel.setText("Attack: " + unit.getAttack());
        defenseLabel.setText("Defense: " + unit.getDefense());
        intellectLabel.setText("Intellect: " + unit.getIntellect());
        hpLabel.setText("HP: " + unit.getHP());
    }
}

/**
 * Represents the game map as a 2D array of tiles.
 */
class GameMap {
    private Tile[][] tiles;

    public GameMap(int width, int height) {
        tiles = new Tile[height][width];
        Random rand = new Random();
        for(int row = 0; row < height; row++) {
            for(int col = 0; col < width; col++) {
                tiles[row][col] = (rand.nextDouble() < 0.2) ? Tile.WATER : Tile.GRASS;
            }
        }
    }

    public Tile getTile(int x, int y) {
        return tiles[y][x];
    }

    public int getWidth() { return tiles[0].length; }
    public int getHeight() { return tiles.length; }
}

/**
 * A simple enum representing tile types.
 */
enum Tile {
    GRASS,
    WATER
}

/**
 * Note:
 * - The Unit class is defined in a separate file (Unit.java) and includes the stat getters:
 *      getAttack(), getDefense(), getIntellect(), getHP()
 * - The BuildingManager and Building classes are defined in BuildingMechanic.java.
 * - The Pathfinder class is defined in Pathfinder.java.
 */