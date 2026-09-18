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
import javax.swing.JComboBox;
import java.util.ArrayList;

public class HomePanel extends JPanel {
    private static final Color BACKGROUND_GREEN = new Color(20, 58, 48);
    private static final Color GOLD_ACCENT = new Color(201, 162, 39);
    private static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 48);
    private static final Color FELT_GREEN = new Color(27, 77, 62);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 16);
    private JButton newGameButton;
    private JButton loadGameButton;
    private JButton exitButton;
    private JComboBox<User> userComboBox;
    private JButton createUserButton;
    private JButton tutorialButton;

    /**
     * Constructs the home panel with UI components and layout for the main menu.
     */
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
        JLabel profileLabel = new JLabel("Player Profile", SwingConstants.CENTER);
        profileLabel.setForeground(Color.WHITE);
        profileLabel.setFont(BUTTON_FONT);
        profileLabel.setAlignmentX(CENTER_ALIGNMENT);

        userComboBox = new JComboBox<>();
        userComboBox.setFont(BUTTON_FONT);
        userComboBox.setMaximumSize(new Dimension(220, 40));
        userComboBox.setAlignmentX(CENTER_ALIGNMENT);

        createUserButton = createMenuButton("Create Profile");
        menuPanel.add(profileLabel);
        menuPanel.add(Box.createVerticalStrut(8));
        menuPanel.add(userComboBox);
        menuPanel.add(Box.createVerticalStrut(8));
        menuPanel.add(createUserButton);
        newGameButton = createMenuButton("New Game");
        loadGameButton = createMenuButton("Load Game");
        tutorialButton = createMenuButton("Load Tutorial");
        exitButton = createMenuButton("Exit");

        /*
         * Loading remains unavailable until Storage has been implemented.
         */
        loadGameButton.setEnabled(false);

        menuPanel.add(newGameButton);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(loadGameButton);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(tutorialButton);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(exitButton);
        menuPanel.add(Box.createVerticalStrut(25));
        this.add(menuPanel);
    }

    /**
     * Displays all available user profiles.
     *
     * @param users stored user profiles
     */
    public void setUsers(ArrayList<User> users) {
        userComboBox.removeAllItems();

        if (users != null) {
            for (User user : users) {
                userComboBox.addItem(user);
            }
        }
        boolean hasUser = userComboBox.getItemCount() > 0;

        newGameButton.setEnabled(hasUser);
        loadGameButton.setEnabled(hasUser);
        tutorialButton.setEnabled(hasUser);
    }

    /**
     * Selects a particular user in the profile combo box.
     *
     * @param user user that should be selected
     */
    public void selectUser(User user) {
        if (user == null) {
            return;
        }

        for (int index = 0; index < userComboBox.getItemCount(); index++) {

            User item = userComboBox.getItemAt(index);
            if (item.getUserId().equals(user.getUserId())) {
                userComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    /**
     * Returns the profile selected in the combo box.
     *
     * @return selected user, or null if none is selected
     */
    public User getSelectedUser() {
        return (User) userComboBox.getSelectedItem();
    }

    /**
     * Sets the action for the Create Profile button.
     *
     * @param listener action to perform
     */
    public void setCreateUserAction(ActionListener listener) {
        createUserButton.addActionListener(listener);
    }

    /**
     * Sets the action performed when a profile is selected.
     *
     * @param listener action to perform
     */
    public void setUserSelectionAction(ActionListener listener) {
        userComboBox.addActionListener(listener);
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
     * Adds the specified action listener to the Load Tutorial button.
     * 
     * @param listener the ActionListener to be added to the tutorial button
     */
    public void setLoadTutorialAction(ActionListener listener) {
        tutorialButton.addActionListener(listener);
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