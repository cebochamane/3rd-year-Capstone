import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumMap;

public class PlayerHandPanel extends JPanel {
    private Position position;
    private GameView view;

    private static final Color GOLD_ACCENT = new Color(201, 162, 39);
    private static final Color CARD_BACK = new Color(35, 55, 100);
    private static final Font CARD_FONT = new Font("Arial", Font.BOLD, 14);

    private static final Color CARD_BACKGROUND = new Color(250, 248, 240);
    private static final Color RED_SUIT = new Color(190, 30, 45);

    private static final int CARD_WIDTH = 68;
    private static final int CARD_HEIGHT = 36;

    /**
     * Constructs a PlayerHandPanel with the specified player position and orientation.
     * 
     * @param position the player's position
     * @param vertical true for vertical layout, false for horizontal layout
     */
    public PlayerHandPanel(Position position, boolean vertical) {
        this.position = position;
        this.setOpaque(false);
        if (vertical) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        } else {
            this.setLayout(new FlowLayout(FlowLayout.CENTER));
        }
    }

    /**
     * Returns the player position associated with this panel.
     * 
     * @return the player's position
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Sets the GameView instance used by this panel for event handling.
     * 
     * @param view the GameView to associate with this panel
     */
    public void setView(GameView view) {
        this.view = view;
    }

    /**
     * Enables or disables every card button inside one player's hand.
     *
     * @param handPanel panel containing cards
     * @param enabled   whether cards can be clicked
     */
    public void setCardsEnabled(boolean enabled) {
        for (java.awt.Component component : this.getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(enabled);
            }
        }
    }

    /**
     * Displays a given number of face-down cards as card backs.
     * 
     * @param numberOfCards the number of hidden card backs to display
     */
    public void showHiddenCards(int numberOfCards) {
        if (numberOfCards < 0) {
            return;
        }
        this.removeAll();
        for (int i = 0; i < numberOfCards; i++) {
            this.add(createCardBackButton());
        }
        this.revalidate();
        this.repaint();
    }

    /**
     * Creates one disabled face-down card button.
     * 
     * @return a card-back button
     */
    private JButton createCardBackButton() {
        JButton cardButton = new JButton("◆");
        cardButton.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        cardButton.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        cardButton.setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

        cardButton.setBackground(CARD_BACK);
        cardButton.setForeground(GOLD_ACCENT);
        cardButton.setFont(CARD_FONT);
        cardButton.setFocusPainted(false);
        cardButton.setContentAreaFilled(true);

        cardButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD_ACCENT, 1),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)));
        return cardButton;
    }

    /**
     * Converts a Rank enum into the short text displayed on a playing card.
     *
     * @param rank card rank
     * @return short rank text
     */
    public static String getRankText(Rank rank) {
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
    public static String getSuitSymbol(Suit suit) {
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
    public static Color getSuitColor(Suit suit) {
        if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) {
            return RED_SUIT;
        }
        return Color.BLACK;
    }

    /**
     * Creates one clickable card button.
     *
     * @param position position owning the card
     * @param card     card represented by the button
     * @return card button
     */
    private JButton createCardButton(Card card) {

        String cardText = PlayerHandPanel.getRankText(card.getRank()) + PlayerHandPanel.getSuitSymbol(card.getSuit());
        JButton button = new JButton(cardText);

        button.setFont(CARD_FONT);
        button.setForeground(PlayerHandPanel.getSuitColor(card.getSuit()));
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
            /**
             * Handles action events; no implementation provided in the given code.
             * 
             * @param e the action event triggered
             */
            public void actionPerformed(ActionEvent e) {
                if (view != null) {
                    view.cardClicked(new Seat(PlayerHandPanel.this.position), card);
                }
            }
        });
        return button;
    }

    /**
     * Displays the specified hand's cards in the panel in alternating suit order.
     * 
     * @param hand the hand of cards to show
     */
    public void showCards(Hand hand) {
        if (hand == null) {
            return;
        }

        this.removeAll();

        for (Card card : this.getAlternatingCards(hand)) {
            this.add(createCardButton(card));
        }

        this.revalidate();
        this.repaint();
    }

    /**
     * Rearrange cards so that suit alternate.
     * 
     * The order is: Spades, Heart, Clubs, Diamonds.
     * 
     * @param hand hand containing the cards
     * @return cards in alternating-suit order
     */
    private ArrayList<Card> getAlternatingCards(Hand hand) {

        ArrayList<Card> orderedCards = new ArrayList<>();
        EnumMap<Suit, ArrayList<Card>> cardsBySuit = new EnumMap<>(Suit.class);

        /*
         * Display suits in this order: Spades, hearts, Clubs, Diamonds.
         */

        Suit[] suitOrder = { Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS };

        /*
         * Create an empty list for every playing-card suit.
         */
        for (Suit suit : suitOrder) {
            cardsBySuit.put(suit, new ArrayList<>());
        }
        /*
         * Place each card into its list.
         * The existing rank order inside each suit is preserved.
         */
        for (Card card : hand.getCards()) {
            ArrayList<Card> suitCards = cardsBySuit.get(card.getSuit());
            if (suitCards != null) {
                suitCards.add(card);
            }
        }

        /*
         * Add all cards of one suit before miving to the next suit.
         * From the map we take all cards of the hand and put the in order
         * on the same list
         */
        for (Suit suit : suitOrder) {
            orderedCards.addAll(cardsBySuit.get(suit));
        }

        return orderedCards;
    }
}