import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GameView {

    private GameController controller;
    private int lastMenuChoice;

    private JFrame mainFrame;
    private JLabel northLabel, eastLabel, southLabel, westLabel;
    private JPanel northPanel, eastPanel, southPanel, westPanel;
    private JLabel infoLabel;

    private JPanel headerBar, tablePanel, trickPanel, notesPanel;
    private JLabel declarerLabel, notesPlaceholderLabel;

    private static final Color FELT_GREEN = new Color(27, 77, 62);
    private static final Color GOLD_ACCENT = new Color(201, 162, 39);

    /**
     * Connects this view to the game controller.
     *
     * @param controller the controller that manages the game
     */
    public void setController(GameController controller) {
        this.controller = controller;
    }

    /**
     * Displays the main game menu to the user.
     */
    public void showMenu() {
        String[] options = new String[] { "Start New Game", "Load Game", "Exit" };
        lastMenuChoice = JOptionPane.showOptionDialog(null, "Choose an option:", "Main Menu",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    }

    /**
     * Displays the current deal.
     *
     * @param deal the deal to display
     */
    public void showDeal(Deal deal) {
        if (deal == null) {
            JOptionPane.showMessageDialog(null, "No deal available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String text = "";
        text += "Dealer: " + deal.getDealer() + "\n";
        text += "Vulnerability: " + deal.getVulnerability() + "\n";

        for (Position position : Position.values()) {
            Seat seat = new Seat(position);
            Hand hand = deal.getHand(seat);
            text += position + ": " + hand.getCards() + "\n";

        }
        JOptionPane.showMessageDialog(null, text, "Current Deal", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays the current auction and its bids.
     *
     * @param auction the auction to display
     */
    public void showAuction(Auction auction) {
        if (auction == null) {
            JOptionPane.showMessageDialog(null, "No auction available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String text = "";
        for (Bid bid : auction.getBids()) {
            if (bid.isPass()) {
                text += bid.getBidder() + " Pass\n";
            } else if (bid.isDouble()) {
                text += bid.getBidder() + " Double\n";
            } else if (bid.isRedouble()) {
                text += bid.getBidder() + " Redouble\n";
            } else {
                text += bid.getBidder() + " bids " + bid.getLevel() + " " + bid.getDenomination() + "\n";
            }
        }
        JOptionPane.showMessageDialog(null, text, "Current Auction", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays the contract that was established after the auction.
     *
     * @param contract the current contract
     */
    public void showContract(Contract contract) {
        if (contract == null) {
            JOptionPane.showMessageDialog(null, "No contract available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String text = "Declarer: " + contract.getDeclarer() + "\n";
        text += "Level: " + contract.getLevel() + "\n";
        if (contract.isRedoubled()) {
            text += "(Redoubled)\n";
        } else if (contract.isDoubled()) {
            text += "(Doubled)\n";
        }
        JOptionPane.showMessageDialog(null, text, "Current Contract", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays the cards played in a trick.
     *
     * @param trickNo the number of the trick
     * @param trick   the cards played in the trick
     */
    public void showTrick(int trickNo, List<Card> trick) {
        if (trick == null) {
            JOptionPane.showMessageDialog(null, "No trick available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String text = "Trick " + trickNo + ":\n";
        for (Card card : trick) {
            text += card + "\n";
        }
        JOptionPane.showMessageDialog(null, text, "Current Trick", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays the current score.
     *
     * @param score the score to display
     */
    public void showScores(Score score) {
        if (score == null) {
            JOptionPane.showMessageDialog(null, "No score available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String text = "NS: " + score.getScoreNS() + "\n";
        text += "EW: " + score.getScoreEW() + "\n";

        JOptionPane.showMessageDialog(null, text, "Current Score", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays a message to the user.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        if (message == null) {
            JOptionPane.showMessageDialog(null, "No message to display.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * Gets a bid chosen by the user.
     *
     * @return the user's selected bid
     */
    public Bid getUserBid() {

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Bid> bids = new ArrayList<>();
        Seat seat = controller.getCurrentBidder();

        labels.add("Pass");
        bids.add(Bid.BidPass(seat));

        labels.add("Double");
        bids.add(Bid.BidDouble(seat));

        labels.add("Redouble");
        bids.add(Bid.BidRedouble(seat));

        for (int level = 1; level <= 7; level++) {
            for (Suit suit : Suit.values()) {
                labels.add(level + "" + suit);
                bids.add(new Bid(seat, level, suit));
            }
        }
        String[] options = labels.toArray(new String[0]);
        int choice = JOptionPane.showOptionDialog(null, "Choose a bid:", "Bid Selection",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == -1) {
            return null;
        } else {
            return bids.get(choice);
        }
    }

    /**
     * Gets the card the user wants to play from the legal cards.
     *
     * @param hand       the user's current hand
     * @param legalCards the cards the user is legally allowed to play
     * @return the card selected by the user
     */
    public Card getUserCardToPlay(Hand hand, List<Card> legalCards) {

        Card[] cards = legalCards.toArray(new Card[0]);
        String[] options = new String[cards.length];

        for (int i = 0; i < cards.length; i++) {
            options[i] = cards[i].toString();

        }
        int choice = JOptionPane.showOptionDialog(null, "Choose a card to play:", "Card Selection",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == -1) {
            return getUserCardToPlay(hand, legalCards);
        }

        return cards[choice];
    }

    /**
     * Gets the user's choice from the main menu.
     *
     * @return the selected menu option
     */
    public int getMenuChoice() {

        return lastMenuChoice;
    }

    public void initialiseWindow() {
        mainFrame = new JFrame("BridgeTut");
        mainFrame.setSize(900, 700);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);

        infoLabel = new JLabel("Dealer: -  Vulnerability: -");
        infoLabel.setForeground(GOLD_ACCENT);
        headerBar.add(infoLabel, BorderLayout.WEST);

        declarerLabel = new JLabel("Declarer: -");
        declarerLabel.setForeground(GOLD_ACCENT);
        headerBar.add(declarerLabel, BorderLayout.CENTER);

        notesPanel = new JPanel(new BorderLayout());
        notesPanel.setOpaque(false);
        notesPlaceholderLabel = new JLabel("Notes: -");
        notesPlaceholderLabel.setForeground(Color.WHITE);
        notesPanel.add(notesPlaceholderLabel);
        headerBar.add(notesPanel, BorderLayout.EAST);

        mainFrame.add(headerBar, BorderLayout.PAGE_START);

        tablePanel = new JPanel(new BorderLayout()) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(FELT_GREEN);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(GOLD_ACCENT);
                g2d.drawOval(2, 2, getWidth() - 4, getHeight() - 4);

            }

        };
        tablePanel.setOpaque(false);

        northPanel = new JPanel();
        northPanel.setOpaque(false);
        northLabel = new JLabel("North: -");
        northLabel.setForeground(Color.WHITE);
        northPanel.add(northLabel);

        eastPanel = new JPanel();
        eastPanel.setOpaque(false);
        eastLabel = new JLabel("East: -");
        eastLabel.setForeground(Color.WHITE);
        eastPanel.add(eastLabel);

        southPanel = new JPanel();
        southPanel.setOpaque(false);
        southLabel = new JLabel("South: -");
        southLabel.setForeground(Color.WHITE);
        southPanel.add(southLabel);

        westPanel = new JPanel();
        westPanel.setOpaque(false);
        westLabel = new JLabel("West: -");
        westLabel.setForeground(Color.WHITE);
        westPanel.add(westLabel);

        tablePanel.add(northPanel, BorderLayout.NORTH);
        tablePanel.add(eastPanel, BorderLayout.EAST);
        tablePanel.add(southPanel, BorderLayout.SOUTH);
        tablePanel.add(westPanel, BorderLayout.WEST);

        trickPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        trickPanel.setOpaque(false);
        tablePanel.add(trickPanel, BorderLayout.CENTER);

        mainFrame.add(tablePanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);

    }

    public static void main(String[] args) {
        GameView view = new GameView();
        view.initialiseWindow();
    }
}