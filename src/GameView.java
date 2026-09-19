
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
 * GameView handles communication between the GameController and the graphical user interface.
 * The auction is displayed in the GameWindow.
 */
public class GameView {

    private GameController controller;
    private GameWindow window;
    private final java.util.ArrayList<LearningNote> learningNotes = new java.util.ArrayList<>();

    // Main's swap-mode fields
    private boolean swapMode;
    private Seat firstSwapSeat;
    private Card firstSwapCard;

    // Tutorial fields
    private TutorialCatalog tutorialCatalog;
    private TutorialEntry activeTutorial;
    private TutorialSession tutorialSession;
    private boolean tutorialCompletionShown;
    private static final int TUTORIAL_ACTION_DELAY_MS = 2000;
    private Timer tutorialStepTimer;
    private boolean tutorialActionDelayActive;
    private Trick completedTutorialTrick;
    private boolean waitingForTutorialNextTrick;
    private LearningNote tutorialLearningNote;

    /**
     * Connects this view to the game controller.
     * 
     * @param controller the controller that manages the game
     */
    public void setController(GameController controller) {
        this.controller = controller;
    }

    /**
     * Returns the current game controller connected to this view.
     * 
     * @return the current GameController instance
     */
    public GameController getController() {
        return controller;
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
     * Displays the current auction in the centre of the table.
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

        /*
         * Claim is available while the game is still played.
         */
        window.setClaimEnabled(!gameplay.isFinished());

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
        window.updateCurrentPlayer(gameplay.isFinished() ? null : currentPlayer);
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

        if (tutorialSession != null) {
            if (tutorialActionDelayActive) {
                showMessage("Please wait for the next tutorial action.");
                return;
            }

            Trick trickBeforePlay = tutorialSession.getGameplay().getCurrentTrick();

            if (seat.getPosition() != Position.SOUTH
                    || !tutorialSession.playSouthCard(card)) {
                showMessage(tutorialSession.getFeedback());
                return;
            }

            if (trickBeforePlay != null && trickBeforePlay.isComplete()) {
                pauseAfterCompletedTutorialTrick(trickBeforePlay);
            } else {
                renderTutorialSession();
                scheduleNextTutorialStep();
            }

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
     * Displays a centered informational message dialog to the user.
     *
     * @param message message to display
     */
    // chat gpt helped to center the pop up message.
    /**
     * Displays a centered informational message dialog to the user.
     * @param message message to display
     */
    public void showMessage(String message) {
        if (message == null) {
            message = "No message to display.";
        }

        this.initialiseWindow();
        JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = optionPane.createDialog(window.getMainFrame(), "BridgeTut");

        /*
         * Ensure the dialog has its final width and height before calculating its position.
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
            /**
             * Handles component shown events, centering the message dialog when displayed.
             * 
             * @param event TODO
             */
            public void componentShown(ComponentEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    /**
                     * Executes GUI-related updates asynchronously to center dialogs after display.
                     */
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
     * Called by GameWindow when the user clicks a bid button.
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

        if (tutorialSession != null) {
            if (tutorialActionDelayActive) {
                showMessage("Please wait for the next tutorial action.");
                return;
            }

            if (!tutorialSession.submitSouthBid(bid)) {
                showMessage(tutorialSession.getFeedback());
                return;
            }

            renderTutorialSession();
            scheduleNextTutorialStep();
            return;
        }

        try {
            controller.submitBid(bid); // submit bid to the controller.
        } catch (IllegalArgumentException | IllegalStateException e) {
            showMessage(e.getMessage()); // popup message
        }
    }

    /**
     * Creates the GameWindow if it does not already exist.
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
     * Displays a completed trick before refreshing the table for the next trick.
     *
     * @param trick the completed trick
     */
    public void showCompletedTrick(Trick trick) {

        if (trick == null) {
            return;
        }

        this.initialiseWindow();

        /*
         * Refresh the hands first so that the fourth played card is removed from its
         * player's displayed hand.
         */
        this.updateGameplayDisplay();

        /*
         * updateGameplayDisplay() normally displays the new empty trick. Replace it
         * with the completed trick so
         * the user can inspect all four cards.
         */
        window.updateTrick(trick);

        /*
         * Nobody may play while the completed trick is displayed.
         */
        window.updateCurrentPlayer(null);

        /*
         * Trick thirteen has no next trick, so its button will display "Show Result".
         */
        boolean finalTrick = trick.getTrickNo() == 13;
        window.showNextTrickButton(finalTrick);
    }

    /**
     * Continues the game after the user has inspected
     * a completed trick.
     */
    public void nextTrickClicked() {

        if (tutorialSession != null) {
            nextTutorialTrickClicked();
            return;
        }

        if (controller == null || controller.getGameplay() == null) {
            return;
        }

        Gameplay gameplay = controller.getGameplay();

        /*
         * Remove the Next Trick button from the bottom panel.
         */
        window.hideNextTrickButton();

        if (gameplay.isFinished()) {
            showFinalScore("Game complete!");
            return;
        }

        /*
         * Display the new empty trick and enable the cards belonging to the next
         * leader.
         */
        updateGameplayDisplay();
    }

    /**
     * Pauses the tutorial session after a trick has been completed to wait for user to proceed to the next trick.
     * 
     * @param trick the completed tutorial trick
     */
    private void pauseAfterCompletedTutorialTrick(Trick trick) {
        completedTutorialTrick = trick;
        waitingForTutorialNextTrick = true;
        renderTutorialSession();
    }

    /**
     * Advances the tutorial to the next trick after the user has inspected a completed trick.
     */
    private void nextTutorialTrickClicked() {
        if (tutorialSession == null || !waitingForTutorialNextTrick) {
            return;
        }

        waitingForTutorialNextTrick = false;
        completedTutorialTrick = null;
        window.hideNextTrickButton();

        renderTutorialSession();

        if (!tutorialSession.isComplete() && !isSouthTutorialTurn()) {
            scheduleNextTutorialStep();
        }
    }

    /**
     * Creates a learning note from the provided tutorial lesson, including contract details if applicable.
     * 
     * @param lesson the tutorial lesson to create a note from
     * @return a LearningNote instance containing the lesson's note and contract
     */
    private LearningNote createTutorialLearningNote(TutorialLesson lesson) {
        LearningNote note = new LearningNote(NotePhase.GENERAL);
        note.setNoteText(lesson.getNote());

        Contract contract = lesson.getContract();
        if (contract != null) {
            note.setContract(contract.getLevel() + " " + contract.getDenomination());
        }

        return note;
    }

    /**
     * Called when the user clicks New Game from the home page.
     */
    public void newGameClicked() {
        cancelTutorialDelay();

        // Clearing tutorial state
        tutorialLearningNote = null;
        activeTutorial = null;
        tutorialSession = null;
        tutorialCompletionShown = false;

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
        cancelTutorialDelay();

        if (controller == null) {
            return;
        }
        initialiseWindow();
        int choice = JOptionPane.showConfirmDialog(window.getMainFrame(),
                "Are you sure you want to quit the current game?\n" + "Unsaved progress will be lost.",
                "Quit Game", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            controller.quitGame();
            tutorialLearningNote = null;
            window.showHomePage();
        }
    }

    /**
     * Attempts to claim all remaining tricks.
     */
    public void claimClicked() {

        if (controller == null || controller.getGameplay() == null) {
            return;
        }

        initialiseWindow();
        /*
         * Ask the user to confirm before attempting the claim.
         */
        int choice = JOptionPane.showConfirmDialog(window.getMainFrame(), "Claim all remaining tricks?",
                "Claim", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        /*
         * Ask the controller to process the claim.
         */
        boolean claimed = controller.claim();

        if (claimed) {
            updateGameplayDisplay();
            showFinalScore("Claim accepted.");
        } else {
            showMessage(
                    "Claim rejected.\n"
                    + "The claiming side could not win all remaining tricks.");
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
            showContract(controller.getContract());
            updateGameplayDisplay();

            if (phase == GamePhase.FINISHED) {
                showFinalScore("Completed saved game");
            }
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
        cancelTutorialDelay();

        // Clearing tutorial state
        tutorialLearningNote = null;
        activeTutorial = null;
        tutorialSession = null;
        tutorialCompletionShown = false;

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

            int tutorialCount = controller.getSavedTutorials().size();

            this.showMessage("Profile created for " + createdUser.getUsername() + ".\n"
                    + tutorialCount + " tutorial(s) are ready in Load Tutorial.");

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

        try {
            int addedTutorials = controller.syncTutorials();

            if (addedTutorials > 0) {
                showMessage(addedTutorials
                        + " newly available tutorial(s) were added to this profile.");
            }
        } catch (IllegalStateException exception) {
            showMessage("Tutorials could not be synchronised.\n" + exception.getMessage());
        }
    }

    /**
     * Displays all current learning notes, including tutorial notes and user-created quick notes.
     */
    public void showLearningNotes() {
        ArrayList<LearningNote> notesToShow = new ArrayList<>();

        /*
        * When a scripted tutorial is active, show its teaching note first.
        */
        if (tutorialLearningNote != null) {
            notesToShow.add(tutorialLearningNote);
        }

        /*
        * Then include any quick notes the user has created in this session.
        */
        notesToShow.addAll(learningNotes);

        if (notesToShow.isEmpty()) {
            showMessage("No learning notes yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < notesToShow.size(); i++) {
            sb.append(i + 1).append(". ").append(notesToShow.get(i)).append("\n\n");
        }

        showMessage(sb.toString());
    }
    
    /**
     * Called by a "Quick Note" button on Gameview
     * Prompts the user to add a quick note for a specified phase and saves it with contextual information.
     * 
     * @param phase the phase of the game for the note
     */
    public void addQuickNote(NotePhase phase) {
        String text = javax.swing.JOptionPane.showInputDialog(
                window != null ? window.getMainFrame() : null,
                "Add a " + phase.name().toLowerCase().replace('_', ' ') + " note:");

        if (text == null || text.trim().isEmpty())
            return;

        LearningNote note = new LearningNote(phase);
        note.setNoteText(text.trim());

        if (controller != null && controller.getContract() != null) {
            note.setContract(controller.getContract().getLevel() + " " + controller.getContract().getDenomination());
        }

        if (controller != null && controller.getGameplay() != null) {
            note.setTrickNumber(controller.getGameplay().getTrickNo());

        }
        learningNotes.add(note);
        showMessage("Note saved: " + note);
    }

    /**
     * Formats the result for either a normal game or a tutorial.
     */
    private String formatScore(Score score) {
        Contract contract = score.getContract();

        String doubling = contract.isRedoubled() ? " XX" : contract.isDoubled() ? " X" : "";

        int difference = score.getDeclarerTricks() - contract.getRequiredTricks();

        String outcome;
        if (difference == 0) {
            outcome = "Made exactly";
        } else if (difference > 0) {
            outcome = "Made with " + difference + " overtrick(s)";
        } else {
            outcome = "Down " + (-difference) + " trick(s)";
        }

        StringBuilder text = new StringBuilder();

        text.append("Contract: ")
            .append(contract.getLevel()).append(" ")
            .append(contract.getDenomination())
            .append(doubling)
            .append("\nDeclarer: ").append(contract.getDeclarer())
            .append("\nDeclarer's side vulnerable: ")
            .append(score.isDeclarerVulnerable() ? "Yes" : "No")
            .append("\n\n").append(outcome)
            .append("\nDeclarer-side tricks: ")
            .append(score.getDeclarerTricks())
            .append("\nDefender-side tricks: ")
            .append(score.getDefenderTricks())
            .append("\n");

        if (score.isMade()) {
            text.append("\nContract trick points: ")
                .append(score.getTrickScore())
                .append("\nOvertrick points: ")
                .append(score.getOvertrickScore())
                .append("\nGame / part-score bonus: ")
                .append(score.getGameOrPartScoreBonus())
                .append("\nSlam bonus: ")
                .append(score.getSlamBonus())
                .append("\nDouble / redouble bonus: ")
                .append(score.getInsultBonus());
        } else {
            text.append("\nUndertrick penalty awarded to defenders: ")
                .append(score.getUndertrickPenalty());
        }

        text.append("\n\nPoints awarded this deal")
                .append("\nNorth–South: ").append(score.getScoreNS())
                .append("\nEast–West: ").append(score.getScoreEW());

        return text.toString();
    }

    /**
     * Displays a completed normal game's result.
     */
    private void showFinalScore(String heading) {
        try {
            Score score = controller.getScore();

            initialiseWindow();
            window.hideNextTrickButton();
            window.setClaimEnabled(false);
            window.updateCurrentPlayer(null);

            showMessage(heading + "\n\n" + formatScore(score));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showMessage("The score could not be calculated.\n"
                    + exception.getMessage());
        }
    }

    /**
     * Initializes the game view and controller, sets up storage, and shows the home page with profiles.
     * 
     * @param args command line arguments
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

    /**
     * Saves the current game or tutorial session depending on context, showing feedback messages.
     */
    public void saveGameClicked() {
        try {
            if (tutorialSession != null) {
                String tutorialId = controller.saveTutorial(activeTutorial);
                showMessage("Tutorial saved. Loading it will restart this lesson.\nID: " + tutorialId);
            } else {
                controller.saveGame();
            }
        } catch (IllegalStateException exception) {
            showMessage(exception.getMessage());
        }
    }

    /**
     * Allows the user to load a tutorial or practice deal, choosing from files or saved tutorials.
     */
    public void loadTutorialClicked() {
        if (controller == null || controller.getCurrentUser() == null) {
            showMessage("Please select a profile before loading a tutorial.");
            return;
        }

        try {
            tutorialCatalog = TutorialCatalog.load(Path.of("tutorials"));

            String[] options = { "Browse tutorial/practice files", "My Tutorials" };

            int option = JOptionPane.showOptionDialog(
                    window.getMainFrame(),
                    "Choose a tutorial option:",
                    "Load Tutorial",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (option == 0) {
                TutorialEntry selected = (TutorialEntry) JOptionPane.showInputDialog(
                        window.getMainFrame(),
                        "Choose a tutorial or practice deal:",
                        "Load Tutorial",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        tutorialCatalog.getEntries().toArray(),
                        tutorialCatalog.getEntries().isEmpty() ? null : tutorialCatalog.getEntries().get(0));

                if (selected != null) {
                    startTutorialEntry(selected);
                }
            } else if (option == 1) {
                loadSavedTutorial();
            }
        } catch (IOException | IllegalArgumentException exception) {
            showMessage("Tutorials could not be loaded.\n" + exception.getMessage());
        }
    }

    /**
     * Loads a saved tutorial selected by the user and starts it, throwing IOException on failure.
     * 
     * @throws IOException if an I/O error occurs while loading tutorials or saved data
     */
    private void loadSavedTutorial() throws IOException {
        ArrayList<SavedTutorial> saved = controller.getSavedTutorials();

        if (saved.isEmpty()) {
            showMessage("This profile has no saved tutorials.");
            return;
        }

        SavedTutorial selected = (SavedTutorial) JOptionPane.showInputDialog(
                window.getMainFrame(),
                "Choose a tutorial to start:",
                "Saved Tutorials",
                JOptionPane.PLAIN_MESSAGE,
                null,
                saved.toArray(),
                saved.get(0));

        if (selected == null) {
            return;
        }

        TutorialEntry entry = tutorialCatalog.findScripted(
                selected.getSourceFile(), selected.getLessonIndex());

        if (entry == null) {
            showMessage("That tutorial source file or lesson is no longer available.");
            return;
        }

        startTutorialEntry(entry);
    }

    /**
     * Starts a tutorial or practice deal entry, quitting any active game and initializing the lesson.
     * 
     * @param entry the tutorial entry to start
     * @throws IOException if an error occurs reading tutorial data
     */
    private void startTutorialEntry(TutorialEntry entry) throws IOException {
        controller.quitGame();
        window.showGameTable();

        if (entry.isScriptedTutorial()) {
            tutorialLearningNote = createTutorialLearningNote(entry.getLesson());
            completedTutorialTrick = null;
            waitingForTutorialNextTrick = false;
            window.hideNextTrickButton();
            activeTutorial = entry;
            cancelTutorialDelay();
            tutorialSession = new TutorialSession(entry.getLesson(), false);
            tutorialCompletionShown = false;
            renderTutorialSession();

            /*
            * If West, North, or East must act first, wait two seconds
            * before showing that action.
            */
            if (!isSouthTutorialTurn()) {
                scheduleNextTutorialStep();
            }
            return;
        }

        activeTutorial = null;
        tutorialSession = null;
        tutorialLearningNote = null;
        completedTutorialTrick = null;
        waitingForTutorialNextTrick = false;
        controller.startLoadedDeal(HtmlDealParser.parse(entry.getSource()));
    }

    /**
     * Updates the game window to display the current state of the active tutorial session.
     */
    private void renderTutorialSession() {
        Deal deal = tutorialSession.getLesson().getDeal();
        window.updateInfo(deal.getDealer());
        window.setSwapCardsEnabled(false);

        if (tutorialSession.getPhase() == TutorialPhase.BIDDING) {
            Seat expected = tutorialSession.getExpectedSeat();

            for (Position position : Position.values()) {
                Seat seat = new Seat(position);
                Hand hand = deal.getHand(seat);

                if (seat.equals(expected)) {
                    window.updateHand(position, hand);
                } else {
                    window.updateHiddenHand(position, hand.getCards().size());
                }
            }

            window.updateAuction(tutorialSession.getAuction(), deal.getDealer(), expected);
            return;
        }

        Gameplay gameplay = tutorialSession.getGameplay();
        window.updateContract(tutorialSession.getLesson().getContract());

        for (Position position : Position.values()) {
            Seat seat = new Seat(position);
            Hand hand = gameplay.getRemainingHand(seat);

            if (gameplay.isHandVisible(seat)) {
                window.updateHand(position, hand);
            } else {
                window.updateHiddenHand(position, hand.getCards().size());
            }
        }

        if (waitingForTutorialNextTrick && completedTutorialTrick != null) {
            window.updateTrick(completedTutorialTrick);
            window.updateCurrentPlayer(null);
            window.showNextTrickButton(completedTutorialTrick.getTrickNo() == 13);
            return;
        }

        window.hideNextTrickButton();
        window.updateTrick(gameplay.getCurrentTrick());
        window.updateCurrentPlayer(
            tutorialSession.isComplete() || tutorialActionDelayActive ? null : tutorialSession.getExpectedSeat());

        if (tutorialSession.isComplete() && !tutorialCompletionShown) {
            tutorialCompletionShown = true;

            String message = tutorialSession.getFeedback();

            try {
                Score score = tutorialSession.getScore();

                if (score == null) {
                    message += "\n\nScore unavailable: this lesson ended early " + "without a final declarer trick total.";
                } else {
                    message += "\n\n" + formatScore(score);
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                message += "\n\nThe score could not be calculated.\n" + exception.getMessage();
            }

            showMessage(message);
        }
    }

    /**
     * Returns whether it is South's turn in the current tutorial session.
     * 
     * @return true if it is South's turn; false otherwise
     */
    private boolean isSouthTutorialTurn() {
        if (tutorialSession == null) {
            return false;
        }

        Seat expected = tutorialSession.getExpectedSeat();

        return expected != null
                && expected.getPosition() == Position.SOUTH;
    }

    /**
     * Schedules the next automatic tutorial step with a delay, updating the GUI accordingly.
     */
    private void scheduleNextTutorialStep() {
        if (tutorialSession == null || tutorialSession.isComplete()) {
            return;
        }

        /*
        * South is the learner. Do not delay a learner's card-play turn.
        */
        if (tutorialSession.getPhase() == TutorialPhase.PLAYING
                && isSouthTutorialTurn()) {
            tutorialActionDelayActive = false;
            renderTutorialSession();
            return;
        }

        tutorialActionDelayActive = true;
        renderTutorialSession();

        tutorialStepTimer = new Timer(TUTORIAL_ACTION_DELAY_MS, event -> {
            tutorialStepTimer = null;
            tutorialActionDelayActive = false;

            try {
                /*
                * After the delay, enable South if it is South's turn.
                */
                if (isSouthTutorialTurn()) {
                    renderTutorialSession();
                    return;
                }

                Trick trickBeforeAction = tutorialSession.getPhase() == TutorialPhase.PLAYING
                    ? tutorialSession.getGameplay().getCurrentTrick()
                    : null;

                boolean actionCompleted = tutorialSession.advanceOneComputerAction();

                if (actionCompleted && trickBeforeAction != null && trickBeforeAction.isComplete()) {
                    pauseAfterCompletedTutorialTrick(trickBeforeAction);
                    return;
                }

                renderTutorialSession();

                /*
                * Every completed computer action gets another two-second pause
                * before the following action.
                */
                if (actionCompleted && !tutorialSession.isComplete()) {
                    scheduleNextTutorialStep();
                }
            } catch (IllegalStateException exception) {
                showMessage(exception.getMessage());
            }
        });

        tutorialStepTimer.setRepeats(false);
        tutorialStepTimer.start();
    }

    /**
     * Cancels any delayed tutorial action currently scheduled.
     */
    private void cancelTutorialDelay() {
        if (tutorialStepTimer != null) {
            tutorialStepTimer.stop();
            tutorialStepTimer = null;
        }

        tutorialActionDelayActive = false;
    }
}
