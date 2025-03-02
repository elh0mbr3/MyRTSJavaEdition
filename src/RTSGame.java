import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main class that sets up the frame and initial panels.
 */
public class RTSGame extends JFrame {

    private ControlPanel controlPanel;
    private GamePanel gamePanel;

    public RTSGame() {
        setTitle("Primitive RTS with Basic UI and Tiles");
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
        // British English remarks
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
 * Allows selection of units and movement by right-clicking.
 */
class GamePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
    private static final int TILE_SIZE = 32;
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

        // British English remarks
        // Initialise the map and create some initial units
        gameMap = new GameMap(MAP_WIDTH, MAP_HEIGHT);
        units = new ArrayList<>();
        units.add(new Unit(100, 100));
        units.add(new Unit(200, 150));

        addMouseListener(this);
        addMouseMotionListener(this);

        // Set up a timer to repeatedly update and repaint the game
        timer = new Timer(16, this);
        timer.start();

        // Attach an ActionListener to the spawn button to add new units
        controlPanel.getSpawnButton().addActionListener(e -> {
            // Deduct some gold for spawning
            controlPanel.updateGold(-10);
            // Create a new unit at a random position
            units.add(new Unit(
                    (int) (Math.random() * getWidth()),
                    (int) (Math.random() * getHeight())
            ));
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // British English remarks
        // Draw the map as a grid of tiles
        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                Tile tile = gameMap.getTile(col, row);
                switch (tile) {
                    case GRASS:
                        g.setColor(Color.GREEN);
                        break;
                    case WATER:
                        g.setColor(Color.BLUE);
                        break;
                }
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw all units
        for (Unit unit : units) {
            unit.draw(g);
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
        // British English remarks
        // Update the units' positions and repaint
        for (Unit unit : units) {
            unit.update();
        }
        repaint();
    }

    // ---- Mouse handling for selection and movement ----
    @Override
    public void mousePressed(MouseEvent e) {
        // British English remarks
        // Start the selection rectangle when the mouse is pressed
        isSelecting = true;
        selectStartX = e.getX();
        selectStartY = e.getY();
        selectionRect = new Rectangle(selectStartX, selectStartY, 0, 0);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // British English remarks
        // Finalise selection, determine which units are in the rectangle
        isSelecting = false;
        if (selectionRect != null) {
            for (Unit unit : units) {
                if (selectionRect.intersects(unit.getBounds())) {
                    unit.setSelected(true);
                } else if (!e.isShiftDown()) {
                    // If SHIFT is not held, deselect other units
                    unit.setSelected(false);
                }
            }
        }
        selectionRect = null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // British English remarks
        // If right-clicked, move all selected units to the clicked location
        if (SwingUtilities.isRightMouseButton(e)) {
            for (Unit unit : units) {
                if (unit.isSelected()) {
                    unit.setTarget(e.getX(), e.getY());
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // British English remarks
        // Update the dimensions of the selection rectangle as the mouse is dragged
        if (isSelecting) {
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
 * A simple enum representing different tile types.
 */
enum Tile {
    GRASS,
    WATER
}

/**
 * A class representing the game map as a 2D array of tiles.
 */
class GameMap {
    private Tile[][] tiles;

    public GameMap(int width, int height) {
        tiles = new Tile[height][width];
        Random rand = new Random();

        // British English remarks
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
}

/**
 * A basic Unit class that can be selected and moved around.
 */
class Unit {
    private int x, y;
    private int targetX, targetY;
    private int speed = 2;
    private boolean selected;
    private int size = 20;

    public Unit(int x, int y) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
    }

    /**
     * Set the target location to move towards.
     */
    public void setTarget(int tx, int ty) {
        this.targetX = tx;
        this.targetY = ty;
    }

    /**
     * Update the unit's position, moving it closer to the target.
     */
    public void update() {
        // British English remarks
        // Move in small increments towards the target
        if (x < targetX) {
            x += Math.min(speed, targetX - x);
        } else if (x > targetX) {
            x -= Math.min(speed, x - targetX);
        }
        if (y < targetY) {
            y += Math.min(speed, targetY - y);
        } else if (y > targetY) {
            y -= Math.min(speed, y - targetY);
        }
    }

    /**
     * Draw the unit, using a different colour if it is selected.
     */
    public void draw(Graphics g) {
        if (selected) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.YELLOW);
        }
        g.fillRect(x - size / 2, y - size / 2, size, size);
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
}