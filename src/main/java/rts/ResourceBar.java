package rts;
import javax.swing.*;
import java.awt.*;
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

        Font f = goldLabel.getFont().deriveFont(Font.BOLD, 14f);
        goldLabel.setFont(f);
        woodLabel.setFont(f);
        oilLabel.setFont(f);
        Font b = spawnButton.getFont().deriveFont(Font.PLAIN, 14f);
        spawnButton.setFont(b);
        buildButton.setFont(b);

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
