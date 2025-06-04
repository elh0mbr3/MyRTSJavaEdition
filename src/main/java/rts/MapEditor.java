package rts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Very basic map editor that lets the user cycle through tile types
 * by clicking on a grid. Left click cycles Grass -> Water -> Bridge,
 * right click resets a tile to Grass.
 */
public class MapEditor extends JFrame {
    private GameMap map;
    private EditorPanel panel;

    public MapEditor() {
        setTitle("Map Editor");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        map = new GameMap(40, 25);
        // Clear the randomly generated map
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                map.setTile(x, y, Tile.GRASS);
            }
        }

        panel = new EditorPanel();
        add(panel);
        // ensure the frame fits the editing panel so the entire map is visible
        pack();
    }

    private class EditorPanel extends JPanel implements MouseListener {
        EditorPanel() {
            setPreferredSize(new Dimension(40 * GamePanel.TILE_SIZE,
                                          25 * GamePanel.TILE_SIZE));
            addMouseListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    Tile t = map.getTile(x, y);
                    switch (t) {
                        case GRASS:
                        case BUILDING:
                            g.setColor(Color.GREEN);
                            break;
                        case WATER:
                            g.setColor(Color.BLUE);
                            break;
                        case BRIDGE:
                            g.setColor(new Color(139, 69, 19));
                            break;
                    }
                    g.fillRect(x * GamePanel.TILE_SIZE,
                               y * GamePanel.TILE_SIZE,
                               GamePanel.TILE_SIZE,
                               GamePanel.TILE_SIZE);
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(x * GamePanel.TILE_SIZE,
                               y * GamePanel.TILE_SIZE,
                               GamePanel.TILE_SIZE,
                               GamePanel.TILE_SIZE);
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int tx = e.getX() / GamePanel.TILE_SIZE;
            int ty = e.getY() / GamePanel.TILE_SIZE;
            if (tx < 0 || ty < 0 || tx >= map.getWidth() || ty >= map.getHeight()) {
                return;
            }
            if (SwingUtilities.isLeftMouseButton(e)) {
                Tile t = map.getTile(tx, ty);
                if (t == Tile.GRASS) {
                    map.setTile(tx, ty, Tile.WATER);
                } else if (t == Tile.WATER) {
                    map.setTile(tx, ty, Tile.BRIDGE);
                } else {
                    map.setTile(tx, ty, Tile.GRASS);
                }
            } else if (SwingUtilities.isRightMouseButton(e)) {
                map.setTile(tx, ty, Tile.GRASS);
            }
            repaint();
        }

        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }
}
