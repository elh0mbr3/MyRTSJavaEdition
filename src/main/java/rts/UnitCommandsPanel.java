package rts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
class UnitCommandsPanel extends JPanel {
    private GamePanel gamePanel;
    private ResourceBar resourceBar;
    private JButton attackButton, stopButton, patrolButton;
    private JLabel attackLabel, defenseLabel, intellectLabel, hpLabel;

    public UnitCommandsPanel(GamePanel gamePanel, ResourceBar resourceBar, int width) {
        this.gamePanel = gamePanel;
        this.resourceBar = resourceBar;
        setPreferredSize(new Dimension(width,150));
        setLayout(new FlowLayout(FlowLayout.LEFT,20,20));
        setBackground(new Color(50,50,50));

        attackButton = new JButton("Attack");
        stopButton = new JButton("Stop");
        patrolButton = new JButton("Patrol");

        attackLabel = new JLabel("Attack: -");
        defenseLabel = new JLabel("Defense: -");
        intellectLabel = new JLabel("Intellect: -");
        hpLabel = new JLabel("HP: -");

        Font labelFont = attackLabel.getFont().deriveFont(Font.BOLD, 13f);
        attackLabel.setFont(labelFont);
        defenseLabel.setFont(labelFont);
        intellectLabel.setFont(labelFont);
        hpLabel.setFont(labelFont);
        Font btnFont = attackButton.getFont().deriveFont(Font.PLAIN, 13f);
        attackButton.setFont(btnFont);
        stopButton.setFont(btnFont);
        patrolButton.setFont(btnFont);

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
