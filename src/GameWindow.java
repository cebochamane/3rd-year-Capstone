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
                headerPanel.add(quitGameButton);

                mainFrame.add(headerPanel, BorderLayout.NORTH);

                /*
                 * =====================================================
                 * TABLE
                 * =====================================================
                 */

                tablePanel = new JPanel(new BorderLayout()) {
                        // subclass of JPanel for custom drawing.
                        @Override
                        protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                Graphics2D g2d = (Graphics2D) g;
                                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                                RenderingHints.VALUE_ANTIALIAS_ON); // chat'ts idea for smoothing the//
                                                                                    // edges of the oval.

                                int marginX = 250;
                                int marginY = 145;

                                int tableWidth = getWidth() - (2 * marginX);

                                int tableHeight = getHeight() - (2 * marginY);

                                /*
                                 * Draw green table.
                                 */
                                g2d.setColor(FELT_GREEN);
                                g2d.fillOval(marginX, marginY, tableWidth, tableHeight); // interior

                                /*
                                 * Draw gold outline.
                                 */
                                g2d.setColor(GOLD_ACCENT);
                                g2d.drawOval(marginX, marginY, tableWidth, tableHeight);
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
                southPanel.setPreferredSize(new Dimension(0, 110));
                southPanel.setOpaque(false);

                southLabel = new JLabel("SOUTH", SwingConstants.CENTER);
                southLabel.setForeground(Color.WHITE);
                southLabel.setFont(PLAYER_FONT);

                southHandPanel = new PlayerHandPanel(Position.SOUTH, false);
                southHandPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 5));

                southPanel.add(southLabel, BorderLayout.NORTH);
                southPanel.add(southHandPanel, BorderLayout.CENTER);

                /*
                 * =====================================================
                 * WEST
                 * =====================================================
                 */

                westPanel = new JPanel(new BorderLayout());
                westPanel.setPreferredSize(new Dimension(105, 0));
                westPanel.setOpaque(false);

                /*
                 * Give the cards a little space from the
                 * left edge of the window.
                 */
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

                /*
                 * =====================================================
                 * CENTRE
                 * =====================================================
                 */

                trickPanel = new JPanel(new GridBagLayout());
                trickPanel.setOpaque(false);

                /*
                 * GameWindow creates one AuctionPanel and reuses it
                 * whenever bidding information changes.
                 */
                auctionPanel = new AuctionPanel();
                tablePanel.add(trickPanel, BorderLayout.CENTER);
                mainFrame.add(tablePanel, BorderLayout.CENTER);

                /*
                 * Create the home page.
                 */
                homePanel = new HomePanel();

                /*
                 * Centre and display the window.
                 */
                mainFrame.setLocationRelativeTo(null);
                mainFrame.setVisible(true);
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

                /*
                 * AuctionPanel also needs GameView because
                 * it sends bid clicks to GameView.
                 */
                auctionPanel.setView(view);
                /*
                 * Start the new game from the home page.
                 */
                homePanel.setNewGameAction(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                if (GameWindow.this.view != null) {
                                        GameWindow.this.view.newGameClicked();
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
                 * First reset every player label.
                 */
                northLabel.setForeground(Color.WHITE);
                eastLabel.setForeground(Color.WHITE);
                southLabel.setForeground(Color.WHITE);
                westLabel.setForeground(Color.WHITE);

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
                 * Only cards belonging to the current
                 * player should be clickable.
                 */
                northHandPanel.setCardsEnabled(seat.getPosition() == Position.NORTH);
                eastHandPanel.setCardsEnabled(seat.getPosition() == Position.EAST);
                southHandPanel.setCardsEnabled(seat.getPosition() == Position.SOUTH);
                westHandPanel.setCardsEnabled(seat.getPosition() == Position.WEST);
        }

        /**
         * Displays cards played in the current trick.
         *
         * Calling this method automatically removes
         * the auction from the centre.
         *
         * @param trick current trick
         */
        public void updateTrick(Trick trick) {

                /*
                 * Remove whatever was previously
                 * displayed in the centre.
                 *
                 * During bidding this will be AuctionPanel.
                 */
                trickPanel.removeAll();

                if (trick == null) {

                        trickPanel.revalidate();
                        trickPanel.repaint();

                        return;
                }

                /*
                 * Display every played card according
                 * to the seat that played it.
                 */
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
         * Displays the auction.
         *
         * GameWindow no longer creates bid buttons itself.
         * That responsibility belongs to AuctionPanel.
         *
         * @param auction current auction
         * @param dealer  dealer
         * @param bidder  current bidder
         */
        public void updateAuction(
                        Auction auction,
                        Seat dealer,
                        Seat bidder) {

                if (auction == null) {
                        return;
                }

                /*
                 * Remove whatever is currently displayed
                 * in the centre.
                 */
                trickPanel.removeAll();

                /*
                 * Give the current auction information
                 * to AuctionPanel.
                 */
                auctionPanel.updateAuction(auction, dealer, bidder);

                /*
                 * Put AuctionPanel into the centre.
                 */
                trickPanel.add(auctionPanel);

                /*
                 * Recalculate and redraw the centre.
                 */
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
}