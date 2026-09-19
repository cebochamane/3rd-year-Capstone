import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.util.ArrayList;

/**
 * GameWindow class.
 * Displays the Bridge table, player hands,
 * auction and played cards.
 */
public class GameWindow {

    private JFrame mainFrame;

    /*
     * Main table and centre.
     */
    private JPanel tablePanel;
    private JPanel trickPanel;
    private JPanel headerPanel;
    private ContractMarkerPanel contractMarkerPanel;
    private JButton saveGameButton;
    private JButton nextTrickButton;

    /*
     * Separate panel responsible for the auction UI.
     */
    private AuctionPanel auctionPanel;

    /*
     * Separate panel responsible for the home page.
     */
    private HomePanel homePanel;

    /*
     * Panels containing each player's cards.
     */
    private PlayerHandPanel northHandPanel;
    private PlayerHandPanel eastHandPanel;
    private PlayerHandPanel southHandPanel;
    private PlayerHandPanel westHandPanel;

    /*
     * Panels containing player names and hands.
     */
    private JPanel northPanel;
    private JPanel eastPanel;
    private JPanel southPanel;
    private JPanel westPanel;

    /*
     * Player labels.
     */
    private JLabel northLabel;
    private JLabel eastLabel;
    private JLabel southLabel;
    private JLabel westLabel;

    /*
     * Dealer and declarer information.
     */
    private JLabel dealerLabel;
    private JLabel declarerLabel;
    private JButton quitGameButton;
    private JPanel actionPanel;
    private JButton swapCardsButton;
    private JButton claimButton;
    private JButton concedeButton;
    private JButton doubleDummyButton;
    private JButton tutorialHintButton;
    private JLabel tutorialStatusLabel;

    /*
     * Colours.
     */
    private static final Color BACKGROUND_GREEN = new Color(20, 58, 48);
    private static final Color FELT_GREEN = new Color(27, 77, 62);
    private static final Color GOLD_ACCENT = new Color(201, 162, 39);
    private static final Color CARD_BACKGROUND = new Color(250, 248, 240);
    private static final Color RED_SUIT = new Color(190, 30, 45);
    /*
     * Fonts.
     */
    private static final Font PLAYER_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font CARD_FONT = new Font("Arial", Font.BOLD, 14);

    /*
     * Card dimensions.
     */
    private static final int CARD_WIDTH = 68;
    private static final int CARD_HEIGHT = 36;

    /*
     * Reference to GameView.
     */
    private GameView view;

    /**
     * Creates the game window.
     */
    public GameWindow() {

        /*
         * =====================================================
         * MAIN FRAME
         * =====================================================
         */

        mainFrame = new JFrame("BridgeTut");
        mainFrame.setSize(1200, 900);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.getContentPane().setBackground(BACKGROUND_GREEN);

        /*
         * =====================================================
         * HEADER
         * =====================================================
         */

        headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        headerPanel.setOpaque(false);

        dealerLabel = new JLabel("Dealer:", SwingConstants.CENTER);
        dealerLabel.setForeground(GOLD_ACCENT);
        dealerLabel.setFont(PLAYER_FONT);
        dealerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // chat idea

        declarerLabel = new JLabel("", SwingConstants.CENTER);
        declarerLabel.setForeground(GOLD_ACCENT);
        declarerLabel.setFont(PLAYER_FONT);
        declarerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // chat idea

        quitGameButton = new JButton("Quit Game");
        quitGameButton.setFont(new Font("Arial", Font.BOLD, 13));
        quitGameButton.setBackground(FELT_GREEN);
        quitGameButton.setForeground(Color.WHITE);
        quitGameButton.setFocusPainted(false);

        quitGameButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GOLD_ACCENT),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        headerPanel.add(dealerLabel);
        headerPanel.add(declarerLabel);
        doubleDummyButton = createActionButton("DD Results");
        doubleDummyButton.setEnabled(false);
        doubleDummyButton.addActionListener(event -> {
            if (view != null) view.doubleDummyClicked();
        });
        headerPanel.add(doubleDummyButton);
        mainFrame.add(headerPanel, BorderLayout.NORTH);
        /*
         * =====================================================
         * TABLE
         * =====================================================
         */

        tablePanel = new JPanel(new BorderLayout()) {
            // subclass of JPanel for custom drawing.
            @Override
            /**
             * Custom paints the table panel with a green oval representing the bridge table.
             * @param g the Graphics object used for painting
             */
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON); // chat'ts idea for smoothing the//
                                                            // edges of the oval.

                int marginX = 250;
                int topMargin = 145;
                int bottomMargin = 190;

                int tableWidth = getWidth() - (2 * marginX);

                int tableHeight = getHeight() - topMargin - bottomMargin;

                /*
                 * Draw green table.
                 */
                g2d.setColor(FELT_GREEN);
                g2d.fillOval(marginX, topMargin, tableWidth, tableHeight); // interior

                /*
                 * Draw gold outline.
                 */
                g2d.setColor(GOLD_ACCENT);
                g2d.drawOval(marginX, topMargin, tableWidth, tableHeight);
            }
        };

        tablePanel.setOpaque(false);

        /*
         * =====================================================
         * NORTH
         * =====================================================
         */

        northPanel = new JPanel(new BorderLayout());
        northPanel.setPreferredSize(new Dimension(0, 110));
        northPanel.setOpaque(false);

        northLabel = new JLabel("NORTH", SwingConstants.CENTER);
        northLabel.setForeground(Color.WHITE);
        northLabel.setFont(PLAYER_FONT);

        northHandPanel = new PlayerHandPanel(Position.NORTH, false);
        northPanel.add(northLabel, BorderLayout.NORTH);
        northPanel.add(northHandPanel, BorderLayout.CENTER);

        /*
         * =====================================================
         * EAST
         * =====================================================
         */

        eastPanel = new PlayerHandPanel(Position.EAST, true);
        eastPanel.setPreferredSize(new Dimension(105, 0));
        /*
         * Give the cards a little space from the
         * right edge of the window.
         */
        eastPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // avoid cards on the edge of the
                                                                           // screen

        eastLabel = new JLabel("EAST", SwingConstants.CENTER);
        eastLabel.setForeground(Color.WHITE);
        eastLabel.setFont(PLAYER_FONT);

        eastHandPanel = new PlayerHandPanel(Position.EAST, true);
        eastHandPanel.setLayout(new BoxLayout(eastHandPanel, BoxLayout.Y_AXIS));

        eastPanel.add(eastLabel, BorderLayout.NORTH);
        eastPanel.add(eastHandPanel, BorderLayout.CENTER);

        /*
         * =====================================================
         * SOUTH
         * =====================================================
         */

        southPanel = new JPanel(new BorderLayout());
        southPanel.setPreferredSize(new Dimension(0, 150));
        southPanel.setOpaque(false);

        southLabel = new JLabel("SOUTH", SwingConstants.CENTER);
        southLabel.setForeground(Color.WHITE);
        southLabel.setFont(PLAYER_FONT);

        southHandPanel = new PlayerHandPanel(Position.SOUTH, false);
        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        actionPanel.setOpaque(false);
        saveGameButton = createActionButton("Save Game");
        swapCardsButton = createActionButton("Swap cards");
        claimButton = createActionButton("Claim");
        concedeButton = createActionButton("Concede");
        tutorialHintButton = createActionButton("Hint");
        tutorialHintButton.setEnabled(false);
        tutorialHintButton.setVisible(false);
        tutorialHintButton.setToolTipText("Available after an incorrect tutorial bid or card choice");
        tutorialHintButton.addActionListener(event -> { if (view != null) {
                view.tutorialHintClicked();
            }
        });
        tutorialStatusLabel = new JLabel("");
        tutorialStatusLabel.setForeground(Color.WHITE);
        nextTrickButton = createActionButton("Next Trick");

        nextTrickButton.setVisible(false);
        swapCardsButton.setEnabled(false);

        nextTrickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (view != null) {
                    view.nextTrickClicked();
                }
            }
        });

        swapCardsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (view != null) {
                    view.swapCardsClicked();
                }
            }
        });

        saveGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (view != null) {
                    view.saveGameClicked();
                }
            }
        });

        claimButton.setEnabled(false);
        concedeButton.setEnabled(false);
        actionPanel.add(quitGameButton);
        actionPanel.add(swapCardsButton);
        actionPanel.add(saveGameButton);
        actionPanel.add(nextTrickButton);
        actionPanel.add(claimButton);
        actionPanel.add(concedeButton);
        actionPanel.add(tutorialHintButton);
        actionPanel.add(tutorialStatusLabel);
        southHandPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 5));

        southPanel.add(southLabel, BorderLayout.NORTH);
        southPanel.add(southHandPanel, BorderLayout.CENTER);
        southPanel.add(actionPanel, BorderLayout.SOUTH);

        /*
         * =====================================================
         * WEST
         * =====================================================
         */

        westPanel = new JPanel(new BorderLayout());
        westPanel.setPreferredSize(new Dimension(105, 0));
        westPanel.setOpaque(false);

        westPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        westLabel = new JLabel("WEST", SwingConstants.CENTER);
        westLabel.setForeground(Color.WHITE);
        westLabel.setFont(PLAYER_FONT);

        westHandPanel = new PlayerHandPanel(Position.WEST, true);

        westPanel.add(westLabel, BorderLayout.NORTH);
        westPanel.add(westHandPanel, BorderLayout.CENTER);

        /*
         * =====================================================
         * ADD PLAYERS TO TABLE
         * =====================================================
         */

        tablePanel.add(northPanel, BorderLayout.NORTH);
        tablePanel.add(eastPanel, BorderLayout.EAST);
        tablePanel.add(southPanel, BorderLayout.SOUTH);
        tablePanel.add(westPanel, BorderLayout.WEST);

        GridBagLayout trickLayout = new GridBagLayout();
        trickLayout.columnWidths = new int[] { 140, 140, 140 };
        trickLayout.rowHeights = new int[] { 95, 95, 95 };

        trickPanel = new JPanel(trickLayout);
        trickPanel.setOpaque(false);
        this.contractMarkerPanel = new ContractMarkerPanel();
        auctionPanel = new AuctionPanel();
        tablePanel.add(trickPanel, BorderLayout.CENTER);
        mainFrame.add(tablePanel, BorderLayout.CENTER);

        homePanel = new HomePanel();

        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
            addLearningNotesButtons();
    }

    /**
     * Connects the GameWindow to the GameView.
     *
     * @param view the GameView
     */
    public void setView(GameView view) {
        this.view = view;
        northHandPanel.setView(view);
        eastHandPanel.setView(view);
        southHandPanel.setView(view);
        westHandPanel.setView(view);

        auctionPanel.setView(view);
        homePanel.setNewGameAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GameWindow.this.view != null) {
                    GameWindow.this.view.newGameClicked();
                }
            }
        });

        homePanel.setLoadGameAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (GameWindow.this.view != null) {
                    GameWindow.this.view.loadGameClicked();
                }
            }
        });

        homePanel.setCreateUserAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (GameWindow.this.view != null) {
                    GameWindow.this.view.createUserClicked();
                }
            }
        });

        homePanel.setUserSelectionAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                User selectedUser = homePanel.getSelectedUser();

                if (GameWindow.this.view != null
                        && selectedUser != null) {

                    GameWindow.this.view.userSelected(selectedUser);
                }
            }
        });

        /*
         * Close the BridgeTut application.
         */
        homePanel.setExitAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();
            }
        });

        homePanel.setLoadTutorialAction(event -> {
            if (view != null) {
                view.loadTutorialClicked();
            }
        });

        /*
         * Quit the current game and return to the home page.
         */
        quitGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GameWindow.this.view != null) {
                    GameWindow.this.view.quitGameClicked();
                }
            }
        });

        concedeButton.addActionListener(event -> {
            if (view != null) view.concedeClicked();
        });

        claimButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (view != null) {
                    view.claimClicked();
                }
            }
        });
    }

    /**
     * Updates the user profiles displayed on the home page.
     *
     * @param users available user profiles
     */
    public void updateUsers(ArrayList<User> users) {
        homePanel.setUsers(users);
    }

    /**
     * Enables or disables the concede button in the UI.
     * 
     * @param enabled true to enable the concede button, false to disable
     */
    public void setConcedeEnabled(boolean enabled) {
        concedeButton.setEnabled(enabled);
    }

    /**
     * Updates the tutorial status label with the given status text.
     * 
     * @param status the tutorial status message to display
     */
    public void setTutorialStatus(String status) {
        tutorialStatusLabel.setText(status == null ? "" : status);
    }

    /**
     * Enables or disables the claim button in the UI.
     * 
     * @param enabled true to enable the claim button, false to disable
     */
    public void setClaimEnabled(boolean enabled) {
        claimButton.setEnabled(enabled);
    }

    /**
     * Selects a user profile on the home page.
     *
     * @param user user to select
     */
    public void selectUser(User user) {
        homePanel.selectUser(user);
    }

    /**
     * Updates the cards displayed for a particular player.
     *
     * @param position player's position
     * @param hand     player's hand
     */
    public void updateHand(Position position, Hand hand) {

        if (hand == null || position == null) {
            return;
        }
        PlayerHandPanel handPanel;

        /*
         * Select the correct hand panel.
         */
        if (position == Position.NORTH) {
            handPanel = northHandPanel;
        } else if (position == Position.EAST) {
            handPanel = eastHandPanel;
        } else if (position == Position.SOUTH) {
            handPanel = southHandPanel;
        } else {
            handPanel = westHandPanel;
        }
        handPanel.showCards(hand);
    }

    /**
     * Displays a player's cards face-down.
     * 
     * @param position      player's position
     * @param numberOfCards number of cards to display
     */
    public void updateHiddenHand(Position position, int numberOfCards) {
        if (position == null || numberOfCards < 0) {
            return;
        }
        PlayerHandPanel handPanel;
        if (position == Position.NORTH) {
            handPanel = northHandPanel;
        } else if (position == Position.EAST) {
            handPanel = eastHandPanel;
        } else if (position == Position.SOUTH) {
            handPanel = southHandPanel;
        } else {
            handPanel = westHandPanel;
        }
        handPanel.showHiddenCards(numberOfCards);
    }

    /**
     * Updates dealer information.
     *
     * @param dealer dealer
     */
    public void updateInfo(Seat dealer) {
        dealerLabel.setText("Dealer: " + dealer);
    }

    /**
     * Updates declarer information.
     *
     * @param declarer declarer
     */
    public void updateDeclarer(Seat declarer) {
        declarerLabel.setText("Declarer: " + declarer);
    }

    /**
     * Highlights the player whose turn it is
     * and enables only that player's cards.
     *
     * @param seat current player
     */
    public void updateCurrentPlayer(Seat seat) {

        /*
         * Reset every player label.
         */
        northLabel.setForeground(Color.WHITE);
        eastLabel.setForeground(Color.WHITE);
        southLabel.setForeground(Color.WHITE);
        westLabel.setForeground(Color.WHITE);

        /*
         * Disable every hand first.
         */
        northHandPanel.setCardsEnabled(false);
        eastHandPanel.setCardsEnabled(false);
        southHandPanel.setCardsEnabled(false);
        westHandPanel.setCardsEnabled(false);

        /*
         * A null seat means that nobody may play.
         * This will be used while the completed trick remains visible.
         */
        if (seat == null) {
            return;
        }

        /*
         * Highlight the current player.
         */
        switch (seat.getPosition()) {
            case NORTH:
                northLabel.setForeground(GOLD_ACCENT);
                break;
            case EAST:
                eastLabel.setForeground(GOLD_ACCENT);
                break;
            case SOUTH:
                southLabel.setForeground(GOLD_ACCENT);
                break;
            case WEST:
                westLabel.setForeground(GOLD_ACCENT);
                break;
        }

        /*
         * Enable only the current player's cards.
         */
        northHandPanel.setCardsEnabled(seat.getPosition() == Position.NORTH);
        eastHandPanel.setCardsEnabled(seat.getPosition() == Position.EAST);
        southHandPanel.setCardsEnabled(seat.getPosition() == Position.SOUTH);
        westHandPanel.setCardsEnabled(seat.getPosition() == Position.WEST);
    }

    /**
     * Displays the button used to continue after a completed trick.
     *
     * @param finalTrick whether the completed trick is trick thirteen
     */
    public void showNextTrickButton(boolean finalTrick) {

        if (finalTrick) {
            nextTrickButton.setText("Show Result");
        } else {
            nextTrickButton.setText("Next Trick");
        }

        nextTrickButton.setVisible(true);

        actionPanel.revalidate();
        actionPanel.repaint();
    }

    /**
     * Hides the Next Trick button.
     */
    public void hideNextTrickButton() {
        nextTrickButton.setVisible(false);
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    /**
     * Displays the cards played in the current trick on the table and updates the UI accordingly.
     * 
     * @param trick the current trick to display
     */
    public void updateTrick(Trick trick) {
        trickPanel.removeAll();

        GridBagConstraints contractConstraints = new GridBagConstraints();
        contractConstraints.gridx = 1;
        contractConstraints.gridy = 1;
        contractConstraints.insets = new Insets(10, 15, 10, 15);
        trickPanel.add(contractMarkerPanel, contractConstraints);

        if (trick == null) {
            trickPanel.revalidate();
            trickPanel.repaint();
            return;
        }

        for (Map.Entry<Seat, Card> entry : trick.getPlays().entrySet()) {

            Seat seat = entry.getKey();
            Card card = entry.getValue();

            String cardText = PlayerHandPanel.getRankText(card.getRank())
                    + PlayerHandPanel.getSuitSymbol(card.getSuit());
            JLabel cardLabel = new JLabel(cardText, SwingConstants.CENTER);

            cardLabel.setFont(CARD_FONT);
            cardLabel.setForeground(PlayerHandPanel.getSuitColor(card.getSuit()));
            cardLabel.setBackground(CARD_BACKGROUND);
            cardLabel.setOpaque(true);
            cardLabel.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

            cardLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GOLD_ACCENT, 1), BorderFactory.createEmptyBorder(
                            3, 5, 3, 5)));
            // GenAi helped
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 25, 15, 25);

            /*
             * GenAi helped with the coordinates.
             * Position the card according
             * to the player who played it.
             */
            switch (seat.getPosition()) {
                case NORTH:
                    gbc.gridx = 1;
                    gbc.gridy = 0;
                    break;
                case EAST:
                    gbc.gridx = 2;
                    gbc.gridy = 1;
                    break;
                case SOUTH:
                    gbc.gridx = 1;
                    gbc.gridy = 2;
                    break;
                case WEST:
                    gbc.gridx = 0;
                    gbc.gridy = 1;
                    break;
            }
            trickPanel.add(cardLabel, gbc);
        }

        trickPanel.revalidate();
        trickPanel.repaint();
    }

    /**
     * Displays the current auction in the center UI area with dealer and current bidder information.
     * 
     * @param auction the current auction
     * @param dealer the dealer's seat
     * @param bidder the current bidder's seat
     */
    public void updateAuction(Auction auction, Seat dealer, Seat bidder) {

        if (auction == null) {
            return;
        }

        trickPanel.removeAll();

        auctionPanel.updateAuction(auction, dealer, bidder);

        GridBagConstraints auctionConstraints = new GridBagConstraints();
        auctionConstraints.gridx = 0;
        auctionConstraints.gridy = 0;
        auctionConstraints.gridwidth = 3;
        auctionConstraints.gridheight = 3;
        auctionConstraints.anchor = GridBagConstraints.CENTER;

        trickPanel.add(auctionPanel, auctionConstraints);
        trickPanel.revalidate();
        trickPanel.repaint();
    }

    /**
     * Dispaly the home page.
     *
     */
    public void showHomePage() {
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(homePanel, BorderLayout.CENTER);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    /**
     * Dispaly the bridge game table.
     */
    public void showGameTable() {
        mainFrame.getContentPane().removeAll();
        mainFrame.getContentPane().add(headerPanel, BorderLayout.NORTH);
        mainFrame.getContentPane().add(tablePanel, BorderLayout.CENTER);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    /**
     * Returns the main application frame.
     * 
     * @return main frame
     */
    public JFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Updates the contract displayed in the centre of the table.
     * 
     * @param contract the contract to be displayed
     */
    public void updateContract(Contract contract) {
        contractMarkerPanel.updateContract(contract);
    }

    /**
     * Creates and returns a styled JButton with specified text for use in the UI.
     * 
     * @param text the button text
     * @return a configured JButton with specified text
     */
    private JButton createActionButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(FELT_GREEN);
        button.setFocusPainted(false);

        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GOLD_ACCENT),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));

        return button;
    }

    /**
     * Enables or disables the swap cards button.
     * 
     * @param enabled true when swapping is allowed.
     */
    public void setSwapCardsEnabled(boolean enabled) {
        swapCardsButton.setEnabled(enabled);
    }

    /**
     * Enables or disables the cards belonging to all four players.
     * 
     * @param enabled whether every face-up card can be clicked
     */
    public void setAllHandsEnabled(boolean enabled) {
        northHandPanel.setCardsEnabled(enabled);
        eastHandPanel.setCardsEnabled(enabled);
        southHandPanel.setCardsEnabled(enabled);
        westHandPanel.setCardsEnabled(enabled);
    }

    // ===== Learning Notes buttons (AI-assisted) =====

    /** Adds "Learning Notes" and "Quick Note" buttons to the header. */
    public void addLearningNotesButtons() {
        if (headerPanel == null)
            return;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonPanel.setOpaque(false);

        JButton notesBtn = new JButton("Learning Notes");
        notesBtn.setBackground(new Color(200, 180, 50));
        notesBtn.setForeground(Color.BLACK);
        notesBtn.setFont(new Font("Arial", Font.BOLD, 12));
        notesBtn.addActionListener(e -> {
            if (view != null)
                view.showLearningNotes();
        });
        buttonPanel.add(notesBtn);

        JButton quickBtn = new JButton("Quick Note");
        quickBtn.setBackground(new Color(100, 150, 200));
        quickBtn.setForeground(Color.WHITE);
        quickBtn.setFont(new Font("Arial", Font.BOLD, 12));
        quickBtn.addActionListener(e -> {
            if (view != null && view.getController() != null) {
                NotePhase notePhase = NotePhase.fromGamePhase(
                        view.getController().getPhase());
                view.addQuickNote(notePhase);
            }
        });
        buttonPanel.add(quickBtn);

        headerPanel.add(buttonPanel);
        headerPanel.revalidate();
        headerPanel.repaint();
    }
    
    /**
     * Sets the state of the double dummy analysis button with status text and tooltip.
     * 
     * @param playExists true if a play exists and double dummy is enabled
     * @param calculating true if double dummy analysis is currently calculating
     * @param failed true if double dummy calculation failed, prompting retry
     */
    public void setDoubleDummyState(boolean playExists, boolean calculating, boolean failed) {
        doubleDummyButton.setEnabled(playExists);
        doubleDummyButton.setText(calculating ? "DD: analysing…" : failed ? "DD: retry" : "DD Results");
        doubleDummyButton.setToolTipText(!playExists ? "Available once the playing phase starts" 
            : "View original-deal tricks for every strain and declarer");
    }

    /** Hide hints in normal games; tutorial sessions decide whether the current action unlocks one.
     * 
     * @param tutorialActive true if currently doing a Tutorial lesson, false otherwise
     * @param enabled true if the hint button should be enabled, false otherwise
     */
    public void setTutorialHintState(boolean tutorialActive, boolean enabled) {
        tutorialHintButton.setVisible(tutorialActive);
        tutorialHintButton.setEnabled(tutorialActive && enabled);
        actionPanel.revalidate();
        actionPanel.repaint();
    }

}