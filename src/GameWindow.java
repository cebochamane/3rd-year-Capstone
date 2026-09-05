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
        private JPanel northHandPanel;
        private JPanel eastHandPanel;
        private JPanel southHandPanel;
        private JPanel westHandPanel;

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
        private static final Color CARD_BACK = new Color(35, 55, 100);
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

        private Gameplay gameplay;
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

                northHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 5));
                northHandPanel.setOpaque(false);

                northPanel.add(northLabel, BorderLayout.NORTH);
                northPanel.add(northHandPanel, BorderLayout.CENTER);

                /*
                 * =====================================================
                 * EAST
                 * =====================================================
                 */

                eastPanel = new JPanel(new BorderLayout());
                eastPanel.setPreferredSize(new Dimension(105, 0));
                eastPanel.setOpaque(false);

                /*
                 * Give the cards a little space from the
                 * right edge of the window.
                 */
                eastPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // avoid cards on the edge of the
                                                                                   // screen

                eastLabel = new JLabel("EAST", SwingConstants.CENTER);
                eastLabel.setForeground(Color.WHITE);
                eastLabel.setFont(PLAYER_FONT);

                eastHandPanel = new JPanel();
                eastHandPanel.setLayout(new BoxLayout(eastHandPanel, BoxLayout.Y_AXIS));
                eastHandPanel.setOpaque(false);

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

                southHandPanel = new JPanel(
                                new FlowLayout(FlowLayout.CENTER, 2, 5));

                southHandPanel.setOpaque(false);

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

                westHandPanel = new JPanel();
                westHandPanel.setLayout(new BoxLayout(westHandPanel, BoxLayout.Y_AXIS));

                westHandPanel.setOpaque(false);

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
         * Connects the GameWindow to Gameplay.
         *
         * @param gameplay current gameplay
         */
        public void setGameplay(Gameplay gameplay) {
                this.gameplay = gameplay;
        }

        /**
         * Updates the cards displayed for a particular player.
         *
         * @param position player's position
         * @param hand     player's hand
         */
        public void updateHand(Position position, Hand hand) {

                if (hand == null) {
                        return;
                }
                JPanel handPanel;

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
                /*
                 * Remove the cards previously displayed.
                 */
                handPanel.removeAll();

                /*
                 * Add every remaining card.
                 */
                for (Card card : hand.getCards()) {
                        JButton cardButton = createCardButton(position, card);
                        handPanel.add(cardButton);
                }

                handPanel.revalidate();
                handPanel.repaint();
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
                JPanel handPanel;
                if (position == Position.NORTH) {
                        handPanel = northHandPanel;
                } else if (position == Position.EAST) {
                        handPanel = eastHandPanel;
                } else if (position == Position.SOUTH) {
                        handPanel = southHandPanel;
                } else {
                        handPanel = westHandPanel;
                }
                handPanel.removeAll();
                /*
                 * Add face-down cards.
                 */
                for (int i = 0; i < numberOfCards; i++) {
                        handPanel.add(createCardBackButton());
                }
                handPanel.revalidate();
                handPanel.repaint();
        }

        /**
         * Creates one clickable card button.
         *
         * @param position position owning the card
         * @param card     card represented by the button
         * @return card button
         */
        private JButton createCardButton(Position position, Card card) {

                String cardText = getRankText(card.getRank()) + getSuitSymbol(card.getSuit());
                JButton button = new JButton(cardText);

                button.setFont(CARD_FONT);
                button.setForeground(getSuitColor(card.getSuit()));
                button.setBackground(CARD_BACKGROUND);
                button.setOpaque(true);
                button.setContentAreaFilled(true);

                // ensure that the cards maintain the same size throught(chat's advise)
                button.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
                button.setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
                button.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
                button.setFocusPainted(false);

                // draw the edges and add spacing around the text.
                button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GOLD_ACCENT, 1),
                                BorderFactory.createEmptyBorder(3, 5, 3, 5)));

                /*
                 * Listen for a click on this card.
                 */
                button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                if (view != null) {
                                        view.cardClicked(new Seat(position), card);
                                }
                        }
                });
                return button;
        }

        /**
         * Creates one disabled face-down card button.
         * 
         * @return a card-back button
         */
        private JButton createCardBackButton() {
                JButton button = new JButton("◆");
                button.setFont(CARD_FONT);
                button.setForeground(GOLD_ACCENT);
                button.setEnabled(false);
                button.setBackground(CARD_BACK);
                button.setOpaque(true);
                button.setContentAreaFilled(true);

                button.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
                button.setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
                button.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
                button.setFocusPainted(false);

                // draw the edges and add spacing around the text.
                button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GOLD_ACCENT, 1),
                                BorderFactory.createEmptyBorder(3, 5, 3, 5)));

                return button;
        }

        /**
         * Converts a Rank enum into the short text
         * displayed on a playing card.
         *
         * @param rank card rank
         * @return short rank text
         */
        private String getRankText(Rank rank) {
                switch (rank) {
                        case TWO:
                                return "2";
                        case THREE:
                                return "3";
                        case FOUR:
                                return "4";
                        case FIVE:
                                return "5";
                        case SIX:
                                return "6";
                        case SEVEN:
                                return "7";
                        case EIGHT:
                                return "8";
                        case NINE:
                                return "9";
                        case TEN:
                                return "10";
                        case JACK:
                                return "J";
                        case QUEEN:
                                return "Q";
                        case KING:
                                return "K";
                        case ACE:
                                return "A";
                        default:
                                return "";
                }
        }

        /**
         * GenAi was used to write the symbol then copied and pasted from there.
         * Converts a Suit enum into a playing-card symbol.
         *
         * @param suit card suit
         * @return suit symbol
         */
        private String getSuitSymbol(Suit suit) {
                switch (suit) {
                        case CLUBS:
                                return "♣";
                        case DIAMONDS:
                                return "♦";
                        case HEARTS:
                                return "♥";
                        case SPADES:
                                return "♠";
                        default:
                                return "";
                }
        }

        /**
         * Returns the colour used for a card suit.
         * Hearts and diamonds are red.
         * Spades and clubs are black.
         *
         * @param suit card suit
         * @return suit colour
         */
        private Color getSuitColor(Suit suit) {
                if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) {
                        return RED_SUIT;
                }
                return Color.BLACK;
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
                setHandEnabled(northHandPanel, seat.getPosition() == Position.NORTH);
                setHandEnabled(eastHandPanel, seat.getPosition() == Position.EAST);
                setHandEnabled(southHandPanel, seat.getPosition() == Position.SOUTH);
                setHandEnabled(westHandPanel, seat.getPosition() == Position.WEST);
        }

        /**
         * Enables or disables every card button
         * inside one player's hand.
         *
         * @param handPanel panel containing cards
         * @param enabled   whether cards can be clicked
         */
        private void setHandEnabled(JPanel handPanel, boolean enabled) {

                for (java.awt.Component component : handPanel.getComponents()) {
                        if (component instanceof JButton) {
                                component.setEnabled(enabled); // setEnabled(boolean) is given by java.
                        }
                }
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

                        String cardText = getRankText(card.getRank()) + getSuitSymbol(card.getSuit());
                        JLabel cardLabel = new JLabel(cardText, SwingConstants.CENTER);

                        cardLabel.setFont(CARD_FONT);
                        cardLabel.setForeground(getSuitColor(card.getSuit()));
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
         * Clears the centre of the table.
         */
        public void clearTrickPanel() {
                trickPanel.removeAll();
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