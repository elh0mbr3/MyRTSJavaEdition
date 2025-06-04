package rts;
import javax.swing.*;
import java.awt.*;
class BottomPanel extends JPanel {
    private MiniMapPanel miniMap;
    private UnitCommandsPanel commandsPanel;

    public BottomPanel(GamePanel gamePanel, ResourceBar resourceBar, int width) {
        setLayout(new BorderLayout());
        miniMap = new MiniMapPanel(gamePanel);
        commandsPanel = new UnitCommandsPanel(gamePanel, resourceBar, width - 200);
        add(miniMap, BorderLayout.WEST);
        add(commandsPanel, BorderLayout.CENTER);
        setBackground(new Color(60,60,60));
        setPreferredSize(new Dimension(width,150));
    }
}
