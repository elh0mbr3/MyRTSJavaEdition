package rts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
class GamePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
    public static final int TILE_SIZE = 32;
    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 25;

    private GameMap gameMap;
    private List<Unit> units;           // Unit class is defined in Unit.java
    private BuildingManager buildingManager;  // Defined in BuildingManager.java

    private Timer timer;

    // Textures for map tiles
    private BufferedImage grassTexture;
    private BufferedImage waterTexture;

    // Variables for drag-selection
    private Rectangle selectionRect;
    private boolean isSelecting = false;
    private int selectStartX, selectStartY;

    private ResourceBar resourceBar;
    private boolean buildMode = false; // Flag for build mode
    private BuildingType buildType = BuildingType.BARRACKS;
    private int buildCost = 20;

    public GamePanel(ResourceBar resourceBar) {
        this.resourceBar = resourceBar;
        gameMap = new GameMap(MAP_WIDTH, MAP_HEIGHT);
        units = new ArrayList<>();
        units.add(new Unit(100, 100));
        units.add(new Unit(200, 150));
        buildingManager = new BuildingManager();

        try {
            grassTexture = ImageIO.read(new File("src/texture/grass_texture.png"));
            waterTexture = ImageIO.read(new File("src/texture/water_texture.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        addMouseListener(this);
        addMouseMotionListener(this);

        timer = new Timer(16, this);
        timer.start();

        // Spawn button action - only place new units on grass tiles
        resourceBar.getSpawnButton().addActionListener(e -> {
            if (resourceBar.getGold() < 10) {
                return;
            }
            Random rand = new Random();
            int tx, ty;
            do {
                tx = rand.nextInt(gameMap.getWidth());
                ty = rand.nextInt(gameMap.getHeight());
            } while (gameMap.getTile(tx, ty) != Tile.GRASS);
            resourceBar.updateGold(-10);
            units.add(new Unit(tx * TILE_SIZE + TILE_SIZE / 2,
                               ty * TILE_SIZE + TILE_SIZE / 2));
        });

        // Build button toggles build mode and lets the user choose a type
        resourceBar.getBuildButton().addActionListener(e -> {
            String[] opts = {"Barracks - 20g", "Resource Depot - 10g", "Tower - 15g"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Select building to construct",
                    "Build",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null, opts, opts[0]);
            if(choice == -1) return;
            switch(choice) {
                case 0:
                    buildType = BuildingType.BARRACKS;
                    buildCost = 20;
                    break;
                case 1:
                    buildType = BuildingType.DEPOT;
                    buildCost = 10;
                    break;
                case 2:
                    buildType = BuildingType.TOWER;
                    buildCost = 15;
                    break;
            }
            if(resourceBar.getGold() >= buildCost) {
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
                BufferedImage img = null;
                if(tile == Tile.GRASS || tile == Tile.BUILDING) {
                    img = grassTexture;
                } else if(tile == Tile.WATER) {
                    img = waterTexture;
                }
                if (img != null) {
                    g.drawImage(img, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                } else if (tile == Tile.BRIDGE) {
                    g.setColor(new Color(139, 69, 19));
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else {
                    g.setColor(Color.BLUE);
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
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
            unit.update(gameMap);
        }
        // Update buildings (handle unit production)
        buildingManager.updateBuildings(units, gameMap);
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
                        int dir = (i + j) % 2 == 0 ? 1 : -1;
                        u1.moveBy(dir,0);
                        u2.moveBy(-dir,0);
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
            int tilesWide = 64 / TILE_SIZE; // building size in tiles
            int tilesHigh = 64 / TILE_SIZE;

            boolean inBounds = tileX >= 0 && tileY >= 0 &&
                    tileX + tilesWide <= gameMap.getWidth() &&
                    tileY + tilesHigh <= gameMap.getHeight();

            if(inBounds) {
                boolean allGrass = true;
                for(int ty = tileY; ty < tileY + tilesHigh && allGrass; ty++) {
                    for(int tx = tileX; tx < tileX + tilesWide; tx++) {
                        if(gameMap.getTile(tx, ty) != Tile.GRASS) {
                            allGrass = false;
                            break;
                        }
                    }
                }

                Rectangle newBuildingArea = new Rectangle(tileX * TILE_SIZE, tileY * TILE_SIZE, 64, 64);
                boolean spaceFree = true;
                for(Building b : buildingManager.getBuildings()) {
                    Rectangle r = new Rectangle(b.getX(), b.getY(), b.getWidth(), b.getHeight());
                    if(r.intersects(newBuildingArea)) {
                        spaceFree = false;
                        break;
                    }
                }

                if(allGrass && spaceFree) {
                    buildingManager.addBuilding(
                            new Building(tileX * TILE_SIZE, tileY * TILE_SIZE, 64, 64, buildType)
                    );
                    for(int ty = tileY; ty < tileY + tilesHigh; ty++) {
                        for(int tx = tileX; tx < tileX + tilesWide; tx++) {
                            gameMap.setTile(tx, ty, Tile.BUILDING);
                        }
                    }
                    resourceBar.updateGold(-buildCost);
                } else if(!allGrass) {
                    JOptionPane.showMessageDialog(this, "Cannot build on water!");
                } else {
                    JOptionPane.showMessageDialog(this, "Cannot build on top of another building!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Not enough space to build here.");
            }

            buildMode = false;
            return;
        } else if(buildMode && SwingUtilities.isRightMouseButton(e)) {
            // allow the user to cancel building placement with right click
            buildMode = false;
            repaint();
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
            Building b = buildingManager.getBuildingAt(e.getPoint());
            if(b != null && b.getType() == BuildingType.BARRACKS) {
                if(resourceBar.getGold() >= 10) {
                    b.queueUnit();
                    resourceBar.updateGold(-10);
                } else {
                    JOptionPane.showMessageDialog(this, "Not enough gold to train unit.");
                }
                return;
            }
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
