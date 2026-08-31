import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Displays the Bridge auction.
 *
 * This panel allows the current bidder to choose
 * a level and suit, or to Pass, Double or Redouble.
 */

public class AuctionPanel extends JPanel {

    private static final Color FELT_GREEN = new Color(27, 77, 62);
    private static final Color GOLD_ACCENT = new Color(201, 162, 39);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 13);

    private GameView view;
    private Seat currentBidder;
    private int selectedLevel; // ranges from 1 -7.
    private Suit selectedSuit; // any denomination of a valid card.

    private JLabel currentBidderLabel;
    private JLabel selectedBidLabel;
    private JPanel historyPanel;

    public AuctionPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        createTitle();
        createCurrentBidderLabel();
        createHistoryPanel();
        createSpecialBidButtons();
        createLevelButtons();
        createSuitButtons();
        createSelectedBidLabel();
        createMakeBidButton();
    }

    /**
     * Connects this panel to the GameView.
     * 
     * @param view the GameView
     */
    public void setView(GameView view) {
        this.view = view;
    }

    /**
     * Creates the auction title.
     */
    private void createTitle() {
        JLabel title = new JLabel("AUCTION", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(GOLD_ACCENT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        add(title);
    }

    /**
     * Creates the label showing whose turn it is to bid.
     */
    private void createCurrentBidderLabel() {
        currentBidderLabel = new JLabel("Current Bidder:", SwingConstants.CENTER);
        currentBidderLabel.setForeground(Color.WHITE);
        currentBidderLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(currentBidderLabel);
    }

    /**
     * Creates the area where previous bids are displayed.
     */
    private void createHistoryPanel() {
        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setOpaque(false);
        add(historyPanel);
    }

    /**
     * Creates Pass, Double and Redouble buttons.
     */
    private void createSpecialBidButtons() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 5));
        panel.setOpaque(false);

        JButton passButton = createButton("Pass");
        JButton doubleButton = createButton("Double");
        JButton redoubleButton = createButton("Redouble");

        passButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pass();
            }
        });

        doubleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doubleBid();
            }
        });

        redoubleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redouble();
            }
        });

        panel.add(passButton);
        panel.add(doubleButton);
        panel.add(redoubleButton);

        add(panel);

    }

    /**
     * Creates buttons for bid levels 1 to 7.
     */
    private void createLevelButtons() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel("Level:");
        label.setForeground(Color.WHITE);

        panel.add(label);

        for (int level = 1; level <= 7; level++) {
            final int chosenLevel = level;

            JButton button = createButton(String.valueOf(level));

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectLevel(chosenLevel);
                }
            });

            panel.add(button);
        }
        add(panel);
    }

    /**
     * Creates buttons for suits and No Trump.
     */
    private void createSuitButtons() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel("Suit:");
        label.setForeground(Color.WHITE);
        panel.add(label);

        addSuitButton(panel, Suit.CLUBS, "♣");
        addSuitButton(panel, Suit.DIAMONDS, "♦");
        addSuitButton(panel, Suit.HEARTS, "♥");
        addSuitButton(panel, Suit.SPADES, "♠");
        addSuitButton(panel, Suit.NT, "NT");

        add(panel);
    }

    /**
     * Adds one suit button.
     * 
     * @param panel panel receiving the button
     * @param suit  represented suit
     * @param text  text shown on the button
     */
    private void addSuitButton(JPanel panel, final Suit suit, String text) {

        JButton button = createButton(text);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(
                    ActionEvent e) {

                selectSuit(suit);
            }
        });

        panel.add(button);
    }

    /**
     * Creates the label showing the bid currently selected.
     */
    private void createSelectedBidLabel() {

        selectedBidLabel = new JLabel("Selected: -", SwingConstants.CENTER);
        selectedBidLabel.setForeground(GOLD_ACCENT);
        selectedBidLabel.setAlignmentX(CENTER_ALIGNMENT);

        add(selectedBidLabel);
    }

    /**
     * Creates the Make Bid button.
     */
    private void createMakeBidButton() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);

        JButton makeBidButton = createButton("Make Bid");
        makeBidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                makeBid();
            }
        });
        panel.add(makeBidButton);
        add(panel);
    }

    /**
     * Stores the level selected by the user.
     * 
     * @param level selected level
     */
    private void selectLevel(int level) {
        selectedLevel = level;
        updateSelectedBidLabel();
    }

    /**
     * Stores the suit selected by the user.
     * 
     * @param suit selected suit
     */
    private void selectSuit(Suit suit) {
        selectedSuit = suit;
        updateSelectedBidLabel();
    }

    /**
     * Updates the selected bid shown to the user.
     */
    private void updateSelectedBidLabel() {

        if (selectedLevel == 0 || selectedSuit == null) {
            selectedBidLabel.setText("Selected: -");
            return;
        }
        selectedBidLabel.setText("Selected: " + selectedLevel + getSuitText(selectedSuit));
    }

    /**
     * Sends a normal contract bid to GameView.
     */
    private void makeBid() {

        if (view == null || currentBidder == null) {
            return;
        }

        if (selectedLevel == 0 || selectedSuit == null) {
            return;
        }

        Bid bid = new Bid(currentBidder, selectedLevel, selectedSuit);
        view.bidClicked(bid);
    }

    /**
     * Sends a Pass bid.
     */
    private void pass() {
        if (view == null || currentBidder == null) {
            return;
        }

        Bid bid = Bid.BidPass(currentBidder);
        view.bidClicked(bid);
    }

    /**
     * Sends a Double bid.
     */
    private void doubleBid() {

        if (view == null || currentBidder == null) {
            return;
        }
        Bid bid = Bid.BidDouble(currentBidder);
        view.bidClicked(bid);
    }

    /**
     * Sends a Redouble bid.
     */
    private void redouble() {
        if (view == null || currentBidder == null) {
            return;
        }

        Bid bid = Bid.BidRedouble(currentBidder);
        view.bidClicked(bid);
    }

    /**
     * Updates the auction information.
     * 
     * @param auction current auction
     * @param dealer  dealer
     * @param bidder  current bidder
     */
    public void updateAuction(Auction auction, Seat dealer, Seat bidder) {
        if (auction == null) {
            return;
        }
        currentBidder = bidder;
        selectedLevel = 0;
        selectedSuit = null;

        currentBidderLabel.setText("Current Bidder: " + bidder);
        selectedBidLabel.setText("Selected: -");
        historyPanel.removeAll();

        for (Bid bid : auction.getBids()) {
            JLabel bidLabel = new JLabel(bid.getBidder() + ": " + bid);
            bidLabel.setForeground(Color.WHITE);

            bidLabel.setAlignmentX(CENTER_ALIGNMENT);
            historyPanel.add(bidLabel);
        }

        historyPanel.revalidate();
        historyPanel.repaint();
    }

    /**
     * Creates a normal auction button.
     * 
     * @param text button text
     * @return new button
     */
    private JButton createButton(String text) {
        JButton button = new JButton(text);

        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(FELT_GREEN);
        button.setFocusPainted(false);

        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GOLD_ACCENT, 1),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));

        return button;
    }

    /**
     * Returns the symbol used for a suit.
     * 
     * @param suit suit
     * @return suit text
     */
    private String getSuitText(Suit suit) {

        switch (suit) {
            case CLUBS:
                return "♣";
            case DIAMONDS:
                return "♦";
            case HEARTS:
                return "♥";
            case SPADES:
                return "♠";
            case NT:
                return "NT";
            default:
                return "";
        }
    }
}
