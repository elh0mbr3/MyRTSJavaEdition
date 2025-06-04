package rts;

import javax.swing.*;
import java.awt.*;

/**
 * Simple test profile page showcasing UI components like progress bar and statistics.
 */
public class ProfilePage extends JFrame {
    private JProgressBar expBar;
    private JLabel nameLabel, gamesLabel, winsLabel, lossesLabel;

    public ProfilePage() {
        setTitle("Player Profile");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(350, 250);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(60, 60, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        nameLabel = new JLabel("Player: TestUser");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 18f));
        nameLabel.setForeground(Color.WHITE);
        add(nameLabel, gbc);

        gbc.gridy++;
        expBar = new JProgressBar(0, 100);
        expBar.setValue(70);
        expBar.setStringPainted(true);
        add(expBar, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gamesLabel = new JLabel("Games Played: 12");
        gamesLabel.setForeground(Color.WHITE);
        add(gamesLabel, gbc);

        gbc.gridx = 1;
        winsLabel = new JLabel("Wins: 8");
        winsLabel.setForeground(Color.WHITE);
        add(winsLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        lossesLabel = new JLabel("Losses: 4");
        lossesLabel.setForeground(Color.WHITE);
        add(lossesLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton closeButton = new JButton("Close");
        add(closeButton, gbc);
        closeButton.addActionListener(e -> dispose());
    }
}
