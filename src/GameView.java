
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * GameView handles communication between the GameController
 * and the graphical user interface.
 * The auction is displayed in the GameWindow.
 */
public class GameView {

    private GameController controller;
    private int lastMenuChoice;
    private GameWindow window;

    /**
     * Connects this view to the game controller.
     * 
     * @param controller the controller that manages the game
     */
    public void setController(GameController controller) {
        this.controller = controller;
    }

    /**
     * Displays the main game menu.
     *
     * public void showMenu() {
     * String[] options = { "Start New Game", "Load Game", "Exit" };
     * lastMenuChoice = JOptionPane.showOptionDialog(
     * null,
     * "Choose an option:",
     * "Main Menu",
     * JOptionPane.DEFAULT_OPTION,
     * JOptionPane.INFORMATION_MESSAGE,
     * null,
     * options,
     * options[0]);
     * }
     */

    /**
     * Displays the current deal in the GameWindow.
     * 
     * @param deal the deal to display
     */
    public void showDeal(Deal deal) {

        if (deal == null) {
            showMessage("No deal available.");
            return;
        }

        initialiseWindow();

        // Update dealer information.
        window.updateInfo(deal.getDealer());

        Seat currentBidder = controller.getCurrentBidder();
        // Display all four hands.
        for (Position position : Position.values()) {
            Seat seat = new Seat(position);
            Hand hand = deal.getHand(seat);
            if (seat.equals(currentBidder)) {
                window.updateHand(position, hand); // update the GUI.
            } else {
                window.updateHiddenHand(position, hand.getCards().size()); // Hide other hands.
            }
        }
    }

    /**
     * Displays the current auction in the centre
     * of the table.
     *
     * @param auction the current auction
     */
    public void showAuction(Auction auction) {
        if (auction == null) {
            showMessage("No auction available.");
            return;
        }
        initialiseWindow();

        if (controller == null || controller.getCurrentDeal() == null) {
            return;
        }
        Deal deal = controller.getCurrentDeal();
        Seat currentBidder = controller.getCurrentBidder();

        /*
         * Reveal only the current bidder's hand, hide the other hands.
         */
        for (Position position : Position.values()) {
            Seat seat = new Seat(position);
            Hand hand = deal.getHand(seat);
            if (seat.equals(currentBidder)) {
                window.updateHand(position, hand); // update the GUI.
            } else {
                window.updateHiddenHand(position, hand.getCards().size()); // Hide other hands.
            }
        }

        window.updateAuction(auction, deal.getDealer(), currentBidder);
    }

    /**
     * Displays the completed contract.
     *
     * @param contract the completed contract
     */
    public void showContract(Contract contract) {

        if (contract == null) {
            return;
        }

        initialiseWindow();

        /*
         * Show the declarer in the main game window.
         * No contract popup is displayed.
         */
        window.updateDeclarer(contract.getDeclarer());
    }

    /**
     * Starts the play phase.
     * Cards are displayed as clickable buttons.
     * There is NO card-selection popup.
     */
    public void startGameplay() {
        if (controller == null) {
            showMessage("Controller has not been connected.");
            return;
        }

        Gameplay gameplay = controller.getGameplay();
        if (gameplay == null) {
            showMessage("Gameplay has not been started.");
            return;
        }

        initialiseWindow();

        /*
         * Give the GameWindow access to the
         * current Gameplay object.
         */
        window.setGameplay(gameplay);

        /*
         * Display the cards.
         * The cards themselves are clickable.
         */
        updateGameplayDisplay();
    }

    /**
     * GameController calls showGameplay(), so this method
     * simply starts/refreshes the gameplay display.
     */
    public void showGameplay() {
        startGameplay();
    }

    /**
     * Updates the GUI after a card has been played.
     */
    public void updateGameplayDisplay() {
        if (controller == null || controller.getGameplay() == null) {
            return;
        }
        initialiseWindow();
        Gameplay gameplay = controller.getGameplay();
        /*
         * Make sure the GameWindow knows which
         * Gameplay object is active.
         */
        window.setGameplay(gameplay);
        Seat currentPlayer = getCurrentPlayer();
        Seat declarer = gameplay.getContract().getDeclarer();
        Seat dummy = this.getDummy(gameplay);
        boolean dummyVisible = this.isDummyVisible(gameplay);

        /*
         * Update all four hands.
         * GameWindow displays these cards as buttons.
         */
        for (Position position : Position.values()) {
            Seat seat = new Seat(position);
            Hand hand = gameplay.getRemainingHand(seat);

            boolean showHand = seat.equals(currentPlayer);

            /*
             * After the opening lead, dummy remains visible.
             */

            if (dummyVisible && seat.equals(dummy)) {
                showHand = true;
            }

            /*
             * When it is dummy's turn, declarer also sees
             * their own hand while controlling dummy.
             */

            if (dummyVisible && currentPlayer.equals(dummy) && seat.equals(declarer)) {
                showHand = true;
            }
            if (showHand) {
                window.updateHand(position, hand);
            } else {
                window.updateHiddenHand(position, hand.getCards().size());
            }
        }

        /*
         * Display the current trick.
         * GameWindow expects a Trick object.
         */
        Trick currentTrick = gameplay.getCurrentTrick();
        window.updateTrick(currentTrick);

        /*
         * Highlight whose turn it is.
         */
        window.updateCurrentPlayer(getCurrentPlayer());
    }

    /**
     * Returns the seat whose turn it currently is.
     * 
     * @return the current player's seat
     */
    private Seat getCurrentPlayer() {

        if (controller == null || controller.getGameplay() == null) {
            return null;
        }

        Gameplay gameplay = controller.getGameplay();

        Trick trick = gameplay.getCurrentTrick();

        if (trick == null) {
            return gameplay.getCurrentLeader();
        }

        /*
         * The leader plays first.
         */
        Seat current = trick.getLeader();

        /*
         * Move clockwise once for every card
         * already played in the trick.
         */
        int numberOfPlays = trick.getPlays().size();

        for (int i = 0; i < numberOfPlays; i++) {
            current = current.next();
        }

        return current;
    }

    /**
     * Called by GameWindow when the user clicks a card.
     * The clicked card is sent to the controller,
     * which handles the game logic.
     *
     * @param seat the player who clicked the card
     * @param card the card that was clicked
     */
    public void cardClicked(Seat seat, Card card) {
        if (controller == null) {
            showMessage("Controller has not been connected.");
            return;
        }
        /*
         * Send the card to the controller.
         * The controller decides whether the card can actually be played.
         * If the card is not legal, nothing is played and no popup is shown.
         */
        controller.playCard(seat, card);
    }

    /**
     * Displays the cards that have been played
     * in a trick.
     *
     * Kept for compatibility with the existing program.
     *
     * @param trickNo number of the trick
     * @param trick   cards played in the trick
     */
    public void showTrick(int trickNo, List<Card> trick) {

        if (trick == null) {
            showMessage("No trick available.");
            return;
        }

        initialiseWindow();

        /*
         * GameWindow works with the actual Trick
         * object because it needs to know which
         * Seat played each card.
         */
        if (controller != null && controller.getGameplay() != null) {

            Trick currentTrick = controller.getGameplay().getCurrentTrick();
            window.updateTrick(currentTrick);
        } else {
            window.clearTrickPanel();
        }
    }

    /**
     * Displays a message to the user.
     *
     * @param message message to display
     */
    public void showMessage(String message) {
        if (message == null) {
            message = "No message to display.";
        }
        initialiseWindow();
        JOptionPane.showMessageDialog(window.getMainFrame(), message, "BridgeTut", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Gets the user's choice from the main menu.
     *
     * @return selected menu option
     */
    public int getMenuChoice() {
        return lastMenuChoice;
    }

    /**
     * Called by GameWindow when the user clicks
     * a bid button.
     *
     * @param bid the bid selected by the user
     */
    public void bidClicked(Bid bid) {

        if (controller == null) {
            showMessage("Controller has not been connected.");
            return;
        }

        if (bid == null) {
            return;
        }

        try {
            controller.submitBid(bid); // submit bid to the controller.
        } catch (IllegalArgumentException | IllegalStateException e) {
            showMessage(e.getMessage()); // popup message
        }
    }

    /**
     * Creates the GameWindow if it does not
     * already exist.
     */
    public void initialiseWindow() {

        if (window == null) {
            window = new GameWindow();

            /*
             * Give GameWindow access to this
             * GameView.
             *
             * GameWindow uses this when a card
             * button is clicked.
             */
            window.setView(this);
        }
    }

    /**
     * Updates the currently highlighted player.
     * 
     * @param seat current player
     */
    public void updateCurrentPlayer(Seat seat) {
        if (window != null) {
            window.updateCurrentPlayer(seat);
        }
    }

    /**
     * Returns the current GameWindow.
     * 
     * @return the game window
     */
    public GameWindow getWindow() {
        return window;
    }

    /**
     * GenAi hepled here.
     * Displays a completed trick for a short time
     * before refreshing the table for the next trick.
     *
     * @param trick the completed trick
     */
    public void showCompletedTrick(Trick trick) {

        if (trick == null) {
            return;
        }
        initialiseWindow();

        /*
         * Display all four cards from the completed trick.
         */
        window.updateTrick(trick);

        /*
         * Wait two seconds before displaying
         * the next trick.
         */
        Timer timer = new Timer(2000, e -> {
            updateGameplayDisplay();
        });

        /*
         * Only run the timer once.
         */
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Returns the declarer's partener , who is the dummy.
     * 
     * @param gameplay the current gameplay
     * @return the dummy's seat
     */
    private Seat getDummy(Gameplay gameplay) {
        if (gameplay == null || gameplay.getContract().getDeclarer() == null) {
            return null;
        }
        Seat declarer = gameplay.getContract().getDeclarer();
        return declarer.next().next(); // The dummy is the partner of the declarer.
    }

    /**
     * Checks whether the dummy's cards should be visible.
     * Dummy becomes visible after the opening lead is made.
     * and remains visible for the rest of the play phase.
     * 
     * @param gameplay the current gameplay
     * @return true if the dummy's cards should be visible, otherwise false
     */
    private boolean isDummyVisible(Gameplay gameplay) {
        if (gameplay == null || gameplay.getContract() == null) {
            return false;
        }
        if (gameplay.getTrickNo() > 1) {
            return true; // Dummy is visible after the first trick.
        }
        Trick currentTrick = gameplay.getCurrentTrick();

        return currentTrick != null && currentTrick.getPlays().size() > 0; // Dummy is visible after the opening lead.
    }

    /**
     * Called when the user clicks New Game from the home page.
     */
    public void newGameClicked() {
        if (controller == null) {
            showMessage("Controller has not been connected.");
            return;
        }
        initialiseWindow();

        /*
         * Replace the home page with the game table.
         */
        window.showGameTable();

        /**
         * Create the deal and begin the auction.
         */
        controller.startNewGame();
    }

    /**
     * Called when the user selects Quit Game.
     */
    public void quitGameClicked() {
        if (controller == null) {
            return;
        }
        initialiseWindow();
        int choice = JOptionPane.showConfirmDialog(window.getMainFrame(),
                "Are you sure you want to quit the current game?\n" + "Unsaved progress will be lost.",
                "Quit Game", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            controller.quitGame();
            window.showHomePage();
        }
    }

    /**
     * Main method used to start the GUI.
     */
    public static void main(String[] args) {
        GameView view = new GameView();
        GameController controller = new GameController();
        view.setController(controller);
        controller.setView(view);
        view.initialiseWindow();
        view.getWindow().showHomePage();
    }
}
