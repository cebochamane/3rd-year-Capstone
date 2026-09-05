import java.awt.Color;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import javax.swing.Box;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import java.awt.event.ActionListener;

public class HomePanel extends JPanel {
    private static final Color BACKGROUND_GREEN = new Color(20, 58, 48);
    private static final Color GOLD_ACCENT = new Color(201, 162, 39);
    private static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 48);
    private static final Color FELT_GREEN = new Color(27, 77, 62);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 16);
    private JButton newGameButton;
    private JButton loadGameButton;
    private JButton exitButton;

    public HomePanel() {
        this.setLayout(new GridBagLayout());
        this.setBackground(BACKGROUND_GREEN);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("BridgeTut", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(GOLD_ACCENT);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        menuPanel.add(titleLabel);

        /*
         * add space between the title and buttons.
         */

        menuPanel.add(Box.createVerticalStrut(30));
        newGameButton = createMenuButton("New Game");
        loadGameButton = createMenuButton("Load Game");
        exitButton = createMenuButton("Exit");

        /*
         * Loading remains unavailable until Storage has been implemented.
         */
        loadGameButton.setEnabled(false);

        menuPanel.add(newGameButton);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(loadGameButton);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(exitButton);
        this.add(menuPanel);
    }

    /**
     * Creates a button for the home page.
     * 
     * @param text the text to display on the button
     * @return the created JButton
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(FELT_GREEN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setAlignmentX(CENTER_ALIGNMENT);

        Dimension size = new Dimension(220, 45);
        button.setPreferredSize(size);
        button.setMaximumSize(size);

        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GOLD_ACCENT),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        return button;
    }

    /**
     * Sets the auction performed by new Game.
     * 
     * @param listener the ActionListener to be added to the new game button
     */
    public void setNewGameAction(ActionListener listener) {
        newGameButton.addActionListener(listener);
    }

    /**
     * Sets the action performed by the load game button.
     * 
     * @param listener the ActionListener to be added to the load game button
     */
    public void setLoadGameAction(ActionListener listener) {
        loadGameButton.addActionListener(listener);
    }

    /**
     * Sets the action performed by the exit button.
     * 
     * @param listener the ActionListener to be added to the exit button
     */
    public void setExitAction(ActionListener listener) {
        exitButton.addActionListener(listener);
    }
}