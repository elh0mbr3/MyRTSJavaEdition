import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Main Menu");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // Create buttons
        JButton startButton = new JButton("Start Game");
        JButton settingsButton = new JButton("Settings");
        JButton exitButton = new JButton("Exit");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        // Add Start Game button
        gbc.gridy = 0;
        add(startButton, gbc);

        // Add Settings button
        gbc.gridy = 1;
        add(settingsButton, gbc);

        // Add Exit button
        gbc.gridy = 2;
        add(exitButton, gbc);

        // Action listener for "Start Game"
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Launch the game window (RTSGame should be in your project)
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        RTSGame game = new RTSGame();
                        game.setVisible(true);
                    }
                });
                dispose(); // Close the main menu
            }
        });

        // Action listener for "Settings"
        settingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(MainMenu.this,
                        "Settings are not implemented yet.",
                        "Settings",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Action listener for "Exit"
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainMenu menu = new MainMenu();
                menu.setVisible(true);
            }
        });
    }
}
