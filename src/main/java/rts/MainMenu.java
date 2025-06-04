package rts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Simple main menu for launching the RTS game. It includes
 * a basic settings dialog and a slightly nicer layout.
 */
public class MainMenu extends JFrame {

    private Dimension resolution = new Dimension(1000, 700);
    private boolean fullscreen = false;

    public MainMenu() {
        setTitle("Main Menu");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(40,40,40));

        JLabel title = new JLabel("Primitive RTS");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        title.setForeground(Color.WHITE);

        JButton startButton = new JButton("Start Game");
        JButton editorButton = new JButton("Map Editor");
        JButton settingsButton = new JButton("Settings");
        JButton exitButton = new JButton("Exit");

        Font btnFont = startButton.getFont().deriveFont(Font.PLAIN, 16f);
        startButton.setFont(btnFont);
        editorButton.setFont(btnFont);
        settingsButton.setFont(btnFont);
        exitButton.setFont(btnFont);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        gbc.gridy = 0;
        add(title, gbc);

        // Add Start Game button
        gbc.gridy = 1;
        add(startButton, gbc);

        // Add Map Editor button
        gbc.gridy = 2;
        add(editorButton, gbc);

        // Add Settings button
        gbc.gridy = 3;
        add(settingsButton, gbc);

        // Add Exit button
        gbc.gridy = 4;
        add(exitButton, gbc);

        // Action listener for "Start Game"
        startButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                RTSGame game = new RTSGame(resolution.width, resolution.height, fullscreen);
                game.setVisible(true);
            });
            dispose();
        });

        // Action listener for "Map Editor"
        editorButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                MapEditor editor = new MapEditor();
                editor.setVisible(true);
            });
        });

        // Action listener for "Settings"
        settingsButton.addActionListener(e -> openSettings());

        // Action listener for "Exit"
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(this, resolution, fullscreen);
        dialog.setVisible(true);
        Dimension sel = dialog.getSelectedResolution();
        if(sel != null) {
            resolution = sel;
        }
        fullscreen = dialog.isFullscreen();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}

/**
 * Simple modal dialog that lets the user choose a resolution.
 */
class SettingsDialog extends JDialog {
    private JComboBox<String> resCombo;
    private JCheckBox fullCheck;
    private Dimension result;
    private boolean fullscreen;

    public SettingsDialog(JFrame parent, Dimension current, boolean initialFullscreen) {
        super(parent, "Settings", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        add(new JLabel("Resolution:"), gbc);
        gbc.gridx = 1;
        String[] opts = {"1000 x 700", "1920 x 1080"};
        resCombo = new JComboBox<>(opts);
        if(current.width == 1920) {
            resCombo.setSelectedIndex(1);
        }
        add(resCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        fullCheck = new JCheckBox("Fullscreen");
        fullCheck.setSelected(initialFullscreen);
        add(fullCheck, gbc);

        JButton ok = new JButton("OK");
        gbc.gridy = 2;
        add(ok, gbc);

        ok.addActionListener(e -> {
            String sel = (String)resCombo.getSelectedItem();
            if(sel != null && sel.startsWith("1920")) {
                result = new Dimension(1920,1080);
            } else {
                result = new Dimension(1000,700);
            }
            this.fullscreen = fullCheck.isSelected();
            setVisible(false);
        });

        pack();
        setLocationRelativeTo(parent);
    }

    public Dimension getSelectedResolution() { return result; }
    public boolean isFullscreen() { return fullscreen; }
}
