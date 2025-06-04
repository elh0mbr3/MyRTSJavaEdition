package rts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Point;       // For working with Point
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.PriorityQueue;  // For PriorityQueue
import java.util.Comparator;      // For Comparator

/**
 * Main class that sets up the frame and initial panels,
 * approximating a Warcraft II–style UI.
 */
public class RTSGame extends JFrame {

    private ResourceBar resourceBar;      // Top resource panel
    private GamePanel gamePanel;          // Center game area
    private BottomPanel bottomPanel;      // Bottom panel with mini-map and unit commands
    private JPopupMenu contextMenu;       // In‑game context menu

    /**
     * Creates the game window with the given resolution.
     * @param width  desired frame width
     * @param height desired frame height
     */
    public RTSGame(int width, int height, boolean fullscreen) {
        setTitle("RTS with Warcraft II–style UI");
        if(fullscreen) {
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setSize(width, height);
            setLocationRelativeTo(null);
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        resourceBar = new ResourceBar();
        gamePanel = new GamePanel(resourceBar);
        int panelWidth = fullscreen ? Toolkit.getDefaultToolkit().getScreenSize().width : width;
        bottomPanel = new BottomPanel(gamePanel, resourceBar, panelWidth);

        setLayout(new BorderLayout());
        add(resourceBar, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        initContextMenu();
        setupKeyBindings();
    }

    /**
     * Default constructor uses the classic 1000x700 window.
     */
    public RTSGame() {
        this(1000, 700, false);
    }

    private void initContextMenu() {
        contextMenu = new JPopupMenu();
        JMenuItem resume = new JMenuItem("Resume");
        JMenuItem exit = new JMenuItem("Exit to Main Menu");
        contextMenu.add(resume);
        contextMenu.add(exit);
        resume.addActionListener(e -> contextMenu.setVisible(false));
        exit.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                MainMenu menu = new MainMenu();
                menu.setVisible(true);
            });
            dispose();
        });
    }

    private void setupKeyBindings() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "showMenu");
        am.put("showMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contextMenu.show(root, getWidth()/2 - 60, getHeight()/2 - 30);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RTSGame game = new RTSGame();
            game.setVisible(true);
        });
    }
}
