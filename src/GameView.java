
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.swing.JDialog;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.SwingUtilities;

/**
 * GameView handles communication between the GameController
 * and the graphical user interface.
 * The auction is displayed in the GameWindow.
 */
public class GameView {

    private GameController controller;
    private GameWindow window;
    private boolean swapMode;
    private Seat firstSwapSeat;
    private Card firstSwapCard;

    /**
     * Connects this view to the game controller.
     * 
     * @param controller the controller that manages the game
     */
    public void setController(GameController controller) {
        this.controller = controller;
    }

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
        this.updateBiddingHands(deal, currentBidder);
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

        updateBiddingHands(deal, currentBidder);
        window.updateAuction(auction, deal.getDealer(), currentBidder);
        window.setSwapCardsEnabled(controller.canSwapCards());
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

        window.updateContract(contract);
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
         * Display the cards.
         * The cards themselves are clickable.
         */
        updateGameplayDisplay();
    }

    /**
     * Updates the GUI after a card has been played.
     */
    public void updateGameplayDisplay() {
        if (controller == null || controller.getGameplay() == null) {
            return;
        }
        this.initialiseWindow();
        Gameplay gameplay = controller.getGameplay();

        Seat currentPlayer = gameplay.getCurrentPlayer();

        /*
         * Update all four hands.
         * GameWindow displays these cards as buttons.
         */
        for (Position position : Position.values()) {
            Seat seat = new Seat(position);
            Hand hand = gameplay.getRemainingHand(seat);

            if (gameplay.isHandVisible(seat)) {
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
        window.updateCurrentPlayer(currentPlayer);
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
         * During swapping mode, clicking a card selects it for swapping instead of
         * attempting to attempting to play.
         */
        if (swapMode) {
            handleSwapCardSelection(seat, card);
            return;
        }
        controller.playCard(seat, card);
    }

    /**
     * Displays a message to the user.
     *
     * @param message message to display
     */
    // chat gpt helped to center the pop up message.
    public void showMessage(String message) {
        if (message == null) {
            message = "No message to display.";
        }

        this.initialiseWindow();
        JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = optionPane.createDialog(window.getMainFrame(), "BridgeTut");

        /*
         * Ensure the dialog has its final width and height before calculating its
         * position.
         */
        dialog.pack();

        /*
         * Centre it before displaying it.
         */
        int x = window.getMainFrame().getX() + (window.getMainFrame().getWidth() - dialog.getWidth()) / 2;

        int y = window.getMainFrame().getY() + (window.getMainFrame().getHeight() - dialog.getHeight()) / 2;

        dialog.setLocation(x, y);

        /*
         * WSL's window manager may reposition the dialog while
         * showing it. Centre it again after it becomes visible.
         */
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int centredX = window.getMainFrame().getX()
                                + (window.getMainFrame().getWidth() - dialog.getWidth()) / 2;

                        int centredY = window.getMainFrame().getY()
                                + (window.getMainFrame().getHeight() - dialog.getHeight()) / 2;

                        dialog.setLocation(centredX, centredY);
                    }
                });
            }
        });

        dialog.setVisible(true);
        dialog.dispose();
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
     * Start card-swapping mode.
     * All four hands are temporarily shown so that the user can select two cards.
     */
    public void swapCardsClicked() {
        if (controller == null || !controller.canSwapCards()) {
            showMessage("Cards can only be swapped before the first bid.");
            return;
        }
        initialiseWindow();
        swapMode = true;
        firstSwapCard = null;
        firstSwapSeat = null;

        Deal deal = controller.getCurrentDeal();

        /*
         * Temporarily reveal all four hands
         */
        for (Position position : Position.values()) {
            Seat seat = new Seat(position);
            Hand hand = deal.getHand(seat);
            window.updateHand(position, hand);
        }
        window.setAllHandsEnabled(true);
        showMessage("Select a card from the first player.");
    }

    /**
     * Shows the current bidder's hand and hides the other hands.
     *
     * @param deal          current deal
     * @param currentBidder player whose turn it is to bid
     */
    private void updateBiddingHands(Deal deal, Seat currentBidder) {
        if (deal == null || currentBidder == null) {
            return;
        }

        for (Position position : Position.values()) {
            Seat seat = new Seat(position);
            Hand hand = deal.getHand(seat);

            if (hand == null) {
                continue;
            }

            if (seat.equals(currentBidder)) {
                window.updateHand(position, hand);
            } else {
                window.updateHiddenHand(position, hand.getCards().size());
            }
        }
    }

    /**
     * Handles a card clicked while card-swappind mode is active.
     * 
     * @param seat owner of the selected card
     * @param card selected card
     */
    private void handleSwapCardSelection(Seat seat, Card card) {
        /*
         * The first click records the first card.
         */
        if (firstSwapCard == null) {
            firstSwapSeat = seat;
            firstSwapCard = card;

            showMessage("First card selected:" + card + " from" + seat + ".\nSelect a card from a different player.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(window.getMainFrame(),
                "Swap " + firstSwapCard + " from " + firstSwapSeat + "\nwith " + card + " from " +
                        seat + "?",
                "Confirm Card Swap", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            boolean swapped = controller.swapCards(firstSwapSeat, firstSwapCard, seat, card);

            swapMode = false;
            firstSwapSeat = null;
            firstSwapCard = null;
            showAuction(controller.getAuction());

            /*
             * Restore normal bidding visibility.
             */
            showAuction(controller.getAuction());
            if (swapped) {
                showMessage("The cards were swapped successfully.");
            } else {
                showMessage("The cards could not be swapped.");
            }
        } else {
            /*
             * Cancel this swap and return to normal bidding.
             */
            swapMode = false;
            firstSwapCard = null;
            firstSwapCard = null;
            showAuction(controller.getAuction());
        }
    }

    /**
     * Dispaly a game that has been loaded from storage.
     */
    public void showLoadedGame() {
        if (controller == null || controller.getCurrentDeal() == null) {
            this.showMessage("There is no loaded game to display.");
            return;
        }
        this.initialiseWindow();
        window.showGameTable();

        Deal deal = controller.getCurrentDeal();
        GamePhase phase = controller.getPhase();

        // Update dealer information
        window.updateInfo(deal.getDealer());

        /*
         * An unfinished auction must display the auction and the hand of the current
         * bidder.
         */
        if (phase == GamePhase.BIDDING) {
            this.showAuction(controller.getAuction());
            return;
        }

        /*
         * A game in the playing or finished phase must display the contract, remaining
         * hands and tricks.
         */

        if (phase == GamePhase.PLAYING || phase == GamePhase.FINISHED) {
            this.showContract(controller.getContract());
            this.updateGameplayDisplay();
            return;
        }

        /*
         * A passed out deal has no contract or gameplay.
         * Show the original hands because the deal is over.
         */
        if (phase == GamePhase.PASSED_OUT) {
            for (Position position : Position.values()) {
                Seat seat = new Seat(position);
                Hand hand = deal.getHand(seat);

                window.updateHand(position, hand);
            }
            this.showMessage("This deal was passed out.");
            return;
        }
    }

    /**
     * Called when the user clicks Load Game.
     */
    public void loadGameClicked() {
        if (controller == null) {
            showMessage("Controller has not been connected.");
            return;
        }

        this.initialiseWindow();

        try {
            ArrayList<SavedGame> savedGames = controller.getSavedGames();

            if (savedGames.isEmpty()) {
                showMessage("This profile has no saved games.");
                return;
            }

            /*
             * Display the saved games in a selection dialog.
             */
            SavedGame selectedGame = (SavedGame) JOptionPane.showInputDialog(window.getMainFrame(),
                    "Select a game to load:",
                    "Load Game", JOptionPane.PLAIN_MESSAGE, null, savedGames.toArray(), savedGames.get(0));

            /*
             * A null result means the user clicked Cancel or closed the dialog.
             */
            if (selectedGame == null) {
                return;
            }

            controller.loadGame(selectedGame.getGameId());

        } catch (IllegalStateException exception) {
            showMessage("Saved games could not be retrieved.\n" + exception.getMessage());
        }
    }

    /**
     * Reloads the available user profiles from storage.
     */
    public void refreshUsers() {
        if (controller == null) {
            return;
        }

        this.initialiseWindow();
        window.updateUsers(controller.getUsers());

        /*
         * Keep the current user selected after refreshing the combo box.
         */
        User currentUser = controller.getCurrentUser();

        if (currentUser != null) {
            window.selectUser(currentUser);
        }
    }

    /**
     * Called when the Create Profile button is clicked.
     */
    public void createUserClicked() {
        if (controller == null) {
            showMessage("Controller has not been connected.");
            return;
        }

        this.initialiseWindow();
        String username = JOptionPane.showInputDialog(window.getMainFrame(), "Enter the new profile name:",
                "Create Profile", JOptionPane.PLAIN_MESSAGE);

        /*
         * showInputDialog returns null when Cancel is clicked.
         */
        if (username == null) {
            return;
        }

        try {
            User createdUser = controller.createUser(username);

            /*
             * Reload the profile list so that the new user
             * appears in the combo box.
             */
            window.updateUsers(controller.getUsers());

            /*
             * Automatically select the newly created user.
             */
            window.selectUser(createdUser);

            this.showMessage("Profile created for " + createdUser.getUsername() + ".");

        } catch (IllegalArgumentException | IllegalStateException exception) {
            this.showMessage("The profile could not be created.\n" + exception.getMessage());
        }
    }

    /**
     * Called when a profile is selected on the home page.
     *
     * @param user selected user
     */
    public void userSelected(User user) {
        if (controller == null || user == null) {
            return;
        }
        controller.setCurrentUser(user);
    }

    /**
     * Main method used to start the GUI.
     */
    public static void main(String[] args) {
        GameView view = new GameView();
        GameController controller = new GameController();

        view.setController(controller);
        controller.setView(view);

        try {
            /*
             * Create a permanent BridgeTut data directory
             * inside the current operating-system user's home directory.
             */
            Path dataDirectory = Path.of(System.getProperty("user.home"), ".bridgetut");
            Files.createDirectories(dataDirectory);
            Path databasePath = dataDirectory.resolve("bridgetut.db");

            Storage storage = new Storage(databasePath.toString());

            controller.setStorage(storage);

        } catch (IOException | IllegalStateException exception) {
            view.showMessage("BridgeTut could not open its database.\n" + exception.getMessage());
        }

        view.initialiseWindow();
        view.getWindow().showHomePage();

        /*
         * Read the existing profiles from SQLite
         * and display them on the home page.
         */
        view.refreshUsers();
    }

}
