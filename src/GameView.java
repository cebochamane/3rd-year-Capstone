import java.util.List;

public class GameView {

    private GameController controller;

    /**
     * Connects this view to the game controller.
     *
     * @param controller the controller that manages the game
     */
    public void setController(GameController controller) {
        // TODO: implement
    }

    /**
     * Displays the main game menu to the user.
     */
    public void showMenu() {
        // TODO: implement
    }

    /**
     * Displays the current deal.
     *
     * @param deal the deal to display
     */
    public void showDeal(Deal deal) {
        // TODO: implement
    }

    /**
     * Displays the current auction and its bids.
     *
     * @param auction the auction to display
     */
    public void showAuction(Auction auction) {
        // TODO: implement
    }

    /**
     * Displays the contract that was established after the auction.
     *
     * @param contract the current contract
     */
    public void showContract(Contract contract) {
        // TODO: implement
    }

    /**
     * Displays the cards played in a trick.
     *
     * @param trickNo the number of the trick
     * @param trick   the cards played in the trick
     */
    public void showTrick(int trickNo, List<Card> trick) {
        // TODO: implement
    }

    /**
     * Displays the current score.
     *
     * @param score the score to display
     */
    public void showScores(Score score) {
        // TODO: implement
    }

    /**
     * Displays a message to the user.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        // TODO: implement
    }

    /**
     * Gets a bid chosen by the user.
     *
     * @return the user's selected bid
     */
    public Bid getUserBid() {
        // TODO: implement
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
        return 0;
    }
}