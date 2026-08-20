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

public class GameView {

    private GameController controller;
    private int lastMenuChoice;

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
        // TODO: implement
        String[] options = new String[] { "Start New Game", "Load Game", "Exit" };
        lastMenuChoice = JOptionPane.showOptionDialog(null, "Choose an option:", "Main Menu",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        this.lastMenuChoice = lastMenuChoice;
    }

    /**
     * Displays the current deal.
     *
     * @param deal the deal to display
     */
    public void showDeal(Deal deal) {
        // TODO: implement
        String text = "";
        text += "Dealer: " + deal.getDealer() + "\n";
        text += "Vulnerability" + deal.getVulnerability() + "\n";

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
        // TODO: implement
        String text = "";
        for (Bid bid : auction.getBids()) {
            if (bid.isPass()) {
                text += bid.getBidder() + " Pass\n";
            } else if (bid.isDouble()) {
                text += bid.getBidder() + " Doubls\n";
            } else if (bid.isRedouble()) {
                text += bid.getBidder() + " Redouble\n";
            } else {
                text += bid.getBidder() + " bids " + bid.getLevel() + "" + bid.getSuit() + "\n";
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
        // TODO: implement
        String text = "Declarer: " + contract.getDeclarer() + "\n";
        text += "Level: " + contract.getLevel() + "\n";
        if (contract.isRedoubled()) {
            text += "(Redoubed)";
        } else if (contract.isDoubled()) {
            text += "(Doubled)";
        }
    }

    /**
     * Displays the cards played in a trick.
     *
     * @param trickNo the number of the trick
     * @param trick   the cards played in the trick
     */
    public void showTrick(int trickNo, List<Card> trick) {
        // TODO: implement
        String text = "Trick" + trickNo + ":\n";
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
        // TODO: implement
        String text = "NS: " + score.getScoreNS() + "\n";
        text += "EW: " + score.getScoreEW();

        JOptionPane.showMessageDialog(null, text, "Current Score", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays a message to the user.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        // TODO: implement
        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * Gets a bid chosen by the user.
     *
     * @return the user's selected bid
     */
    public Bid getUserBid() {
        // TODO: implement
        Bid selectedBid = null;

        JPanel panel = new JPanel();

        return null;
    }

    /**
     * Gets the card the user wants to play from the legal cards.
     *
     * @param hand       the user's current hand
     * @param legalCards the cards the user is legally allowed to play
     * @return the card selected by the user
     */
    public Card getUserCardToPlay(Hand hand, List<Card> legalCards) {
        // TODO: implement

        return null;
    }

    /**
     * Gets the user's choice from the main menu.
     *
     * @return the selected menu option
     */
    public int getMenuChoice() {
        // TODO: implement
        return lastMenuChoice;
    }
}