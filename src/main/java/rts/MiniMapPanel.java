package rts;
import javax.swing.*;
import java.awt.*;
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
                if(tile == Tile.GRASS || tile == Tile.BUILDING) {
                    g.setColor(Color.GREEN);
                } else if(tile == Tile.WATER) {
                    g.setColor(Color.BLUE);
                } else { // bridge
                    g.setColor(new Color(139,69,19));
                }
                g.fillRect(col * miniTileSize, row * miniTileSize, miniTileSize, miniTileSize);
            }
        }
    }
}
