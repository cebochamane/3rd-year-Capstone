import java.util.ArrayList;

/**
 * The GameController class manages the game logic and state for a Bridge game.
 * It includes methods for starting a new game, loading and saving games,
 * dealing cards, conducting auctions, playing the game, and managing game
 * phases.
 */
public class GameController {

    private GameView view;
    private Storage storage;
    private Deal currentDeal;
    private Auction auction;
    private Contract contract;
    private Gameplay gameplay;
    private User currentUser;

    private GamePhase phase = GamePhase.MENU;

    private Seat currentBidder;

    /**
     * Starts a new bridge game by creating a new deal
     * and preparing the game for the auction phase.
     */
    public void startNewGame() {

        Seat dealer = new Seat(Position.NORTH);

        Vulnerability vulnerability = Vulnerability.NONE;

        startRandomDeal(dealer, vulnerability);
    }

    /**
     * Ends the current game and returns to the main menu.
     * 
     */
    public void quitGame() {
        this.currentDeal = null;
        this.auction = null;
        this.contract = null;
        this.gameplay = null;
        this.phase = GamePhase.MENU;

    }

    /**
     * Creates a new user and makes that user
     * the current application user.
     *
     * @param username name entered by the user
     * @return the created user
     */
    public User createUser(String username) {
        if (storage == null) {
            throw new IllegalStateException("Storage has not been connected.");
        }

        User user = storage.createUser(username);
        this.currentUser = user;
        return user;
    }

    /**
     * Returns all users stored on this computer.
     *
     * @return the available users
     */
    public ArrayList<User> getUsers() {
        if (storage == null) {
            return new ArrayList<>();
        }
        return storage.getUsers();
    }

    /**
     * Returns the saved games belonging to the current user.
     *
     * @return current user's saved games
     */
    public ArrayList<SavedGame> getSavedGames() {
        if (storage == null || currentUser == null) {
            return new ArrayList<>();
        }

        return storage.getSavedGames(currentUser);
    }

    /**
     * Loads a game previously saved by user.
     *
     * @param gameId the ID of the game to load
     */
    public void loadGame(String gameId) {

        if (storage == null) {
            if (view != null) {
                view.showMessage("Storage has not been connected yet.");
            }
            return;
        }

        if (currentUser == null) {
            if (view != null) {
                view.showMessage("Please select a user before loading.");
            }
            return;
        }
        if (gameId == null || gameId.trim().isEmpty()) {
            if (view != null) {
                view.showMessage("Please select a saved game.");
            }
            return;
        }
        try {
            /*
             * Storage reconstructs a temporary controller containing the saved game state.
             */
            GameController loadedController = storage.loadGame(currentUser, gameId);

            /*
             * Copy the loaded game state into this controller.
             * We do not replace this controller because it is already connected to the
             * GameView and Storage.
             */
            this.currentDeal = loadedController.getCurrentDeal();
            this.auction = loadedController.getAuction();
            this.contract = loadedController.getContract();
            this.gameplay = loadedController.getGameplay();
            this.phase = loadedController.getPhase();
            this.currentBidder = loadedController.getCurrentBidder();

            if (view != null) {
                view.showLoadedGame();
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            if (view != null) {
                view.showMessage("The game could not be loaded.\n" + exception.getMessage());
            }
        }
    }

    /**
     * Saves the current game state using the storage system.
     */
    public void saveGame() {

        /*
         * Storage has not been implemented yet.
         */
        if (storage == null) {

            if (view != null) {
                view.showMessage("Storage has not been connected yet.");
            }
            return;
        }
        /*
         * Data base must track the user before saving
         */

        if (currentUser == null) {
            if (view != null) {
                view.showMessage("Please select a user before saving.");
            }
            return;
        }
        /*
         * There is nothing to save if a game has not started
         */
        if (currentDeal == null) {
            if (view != null) {
                view.showMessage("There is no current game to save");
            }
            return;
        }
        try {
            /*
             * 'this' is the current GameController object.
             * Storage reads its deal, auction, contract, gameplay and phase.
             */
            String gameId = storage.saveGame(currentUser, this);
            if (view != null) {
                view.showMessage("Game saved successfully.\n" + "Game ID:" + gameId);
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            if (view != null) {
                view.showMessage("The game could not be saved.\n" + exception.getMessage());
            }
        }
    }

    /**
     * Starts a random deal with the specified dealer and vulnerability.
     *
     * @param dealer        the dealer
     * @param vulnerability the vulnerability
     */
    public void startRandomDeal(Seat dealer, Vulnerability vulnerability) {

        if (dealer == null) {
            throw new IllegalArgumentException("Dealer cannot be null.");
        }

        if (vulnerability == null) {
            throw new IllegalArgumentException("Vulnerability cannot be null.");
        }

        Deal deal = new Deal(dealer, vulnerability);
        deal.dealCards();
        startLoadedDeal(deal);
    }

    /**
     * Starts a loaded deal if it contains four hands
     * of thirteen cards.
     *
     * @param deal the deal to start
     */
    public void startLoadedDeal(Deal deal) {

        if (!isCompleteDeal(deal)) {
            throw new IllegalArgumentException("A loaded deal must contain four hands of 13 cards.");
        }
        this.currentDeal = deal;

        /*
         * Clear any old game information.
         */
        this.auction = null;
        this.contract = null;
        this.gameplay = null;

        startAuction();
    }

    /**
     * Starts the auction phase for the current deal.
     */
    public void startAuction() {

        if (currentDeal == null) {
            throw new IllegalStateException("Cannot start auction without a deal.");
        }

        /*
         * Create a new auction.
         */
        this.auction = new Auction();

        /*
         * The dealer bids first.
         */
        this.currentBidder = currentDeal.getDealer();
        this.phase = GamePhase.BIDDING;

        /*
         * Display the deal and auction.
         */
        if (view != null) {
            view.showDeal(currentDeal);
            view.showAuction(auction);
        }
    }

    /**
     * Selects the user currently using the applicaton.
     * Saved games created by this controller will belong.
     * 
     * @param user the selected user
     */
    public void setCurrentUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("The user cannot be null");
        }
        this.currentUser = user;
    }

    /**
     * Returns the user currently using the application.
     * 
     * @return the current user, or null if none has been selected
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Submits a bid made by a player to the current auction.
     *
     * @param bid the bid being submitted
     */
    public void submitBid(Bid bid) {

        if (phase != GamePhase.BIDDING) {
            throw new IllegalStateException("Cannot submit bid when not in bidding phase.");
        }

        if (bid == null) {
            throw new IllegalArgumentException("Bid cannot be null.");
        }

        if (currentBidder == null) {
            throw new IllegalStateException("There is currently no bidder.");
        }

        /*
         * Make sure the bid belongs to the player whose turn
         * it currently is.
         */
        if (!currentBidder.equals(
                bid.getBidder())) {
            throw new IllegalArgumentException("It's not the turn of the seat making the bid.");
        }

        /*
         * Add the bid to the auction.
         */
        boolean added = auction.addBid(bid);

        if (!added) {
            throw new IllegalArgumentException("The bid is not valid according to the auction rules.");
        }

        /*
         * Check whether the auction has finished.
         */
        if (auction.isComplete()) {
            phase = GamePhase.AUCTION_COMPLETE;
            currentBidder = null;

            /*
             * Create the contract if there was a final bid.
             */
            if (!auction.isPassedOut()) {
                this.contract = auction.createContract();
                if (view != null) {
                    view.showAuction(auction);
                    view.showContract(contract);
                }

                /*
                 * Automatically start the playing phase.
                 */
                startPlay(contract);

            } else {
                phase = GamePhase.PASSED_OUT;
                if (view != null) {
                    view.showAuction(auction);
                    view.showMessage("The auction has ended without a contract.");
                }
            }

        } else {

            /*
             * Move to the next player.
             */
            currentBidder = currentBidder.next();
            if (view != null) {
                view.showAuction(auction);
            }
        }
    }

    /**
     * Starts gameplay with the completed contract.
     *
     * @param completedContract the contract
     * @return true if gameplay started successfully
     */
    public boolean startPlay(Contract completedContract) {
        if (phase != GamePhase.AUCTION_COMPLETE) {
            return false;
        }

        if (completedContract == null) {
            return false;
        }

        if (currentDeal == null) {
            return false;
        }

        /*
         * Store the contract.
         */
        this.contract = completedContract;

        /*
         * Create the gameplay object.
         */
        this.gameplay = new Gameplay(currentDeal, contract);

        /*
         * Change the game phase.
         */
        this.phase = GamePhase.PLAYING;

        /*
         * Tell the view to display the playing table.
         */
        if (view != null) {
            view.startGameplay();
        }
        return true;
    }

    /**
     * Plays a card selected from the graphical interface.
     *
     * @param seat the player playing the card
     * @param card the card being played
     * @return true if the card was successfully played
     */
    public boolean playCard(Seat seat, Card card) {
        if (phase != GamePhase.PLAYING || gameplay == null) {
            return false;
        }

        // Remember this trick before the card is played.
        Trick trickBeforePlay = gameplay.getCurrentTrick();

        boolean completingTrick = trickBeforePlay != null && trickBeforePlay.getPlays().size() == 3;
        /*
         * Ask Gameplay to process the card.
         */
        boolean played = gameplay.playCard(seat, card);
        if (!played) {
            return false;
        }

        /*
         * Refresh the GUI.
         */
        if (view != null) {
            if (completingTrick) {
                /*
                 * the trick object is the is complete.
                 */
                view.showCompletedTrick(trickBeforePlay);
            } else {
                view.updateGameplayDisplay();
            }
        }

        if (gameplay.isFinished()) {
            phase = GamePhase.FINISHED;
        }

        return true;
    }

    /**
     * Attempts to claim all remaining tricks for the
     * player whose turn it currently is.
     *
     * @return true if the claim is valid and accepted,
     *         otherwise false
     */
    public boolean claim() {

        /*
         * A claim can only be made during gameplay.
         */
        if (phase != GamePhase.PLAYING || gameplay == null) {
            return false;
        }

        /*
         * The player whose turn it is makes the claim.
         */
        Seat claimant = gameplay.getCurrentPlayer();

        if (claimant == null) {
            return false;
        }

        /*
         * Ask Gameplay to validate and process the claim.
         */
        boolean claimed = this.gameplay.claim(claimant);

        if (!claimed) {
            return false;
        }

        /*
         * A successful claim ends the game.
         */
        phase = GamePhase.FINISHED;

        return true;
    }

    /**
     * Undoes the most recent game action when possible.
     *
     * Undo is not implemented yet.
     */
    public void undo() {
        if (view != null) {
            view.showMessage("Undo has not been implemented yet.");
        }
    }

    /**
     * Returns a description of the current state of the game.
     *
     * @return the current game state
     */
    public String getCurrentState() {
        return phase.toString();
    }

    /**
     * Returns the current phase.
     *
     * @return current game phase
     */
    public GamePhase getPhase() {
        return phase;
    }

    /**
     * Returns the current deal.
     *
     * @return current deal
     */
    public Deal getCurrentDeal() {
        return currentDeal;
    }

    /**
     * Returns the auction.
     *
     * @return auction
     */
    public Auction getAuction() {
        return auction;
    }

    /**
     * Returns the current bidder.
     *
     * @return current bidder
     */
    public Seat getCurrentBidder() {
        return currentBidder;
    }

    /**
     * Returns the contract.
     *
     * @return contract
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Returns the gameplay object.
     *
     * @return gameplay
     */
    public Gameplay getGameplay() {
        return gameplay;
    }

    /**
     * Checks whether a deal contains four hands
     * with thirteen cards each.
     *
     * @param deal the deal
     * @return true if complete
     */
    private boolean isCompleteDeal(Deal deal) {
        if (deal == null || deal.getAllHands().size() != Position.values().length) {
            return false;
        }

        for (Position position : Position.values()) {
            Hand hand = deal.getHand(new Seat(position));
            if (hand == null || hand.getCards().size() != 13) {
                return false;
            }
        }

        return true;
    }

    /**
     * Connects the controller to the view.
     *
     * @param view the game view
     */
    public void setView(GameView view) {
        this.view = view;
    }

    /**
     * Connects the controller to the storage system.
     *
     * @param storage storage system
     */
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    /**
     * Checks whether cards may currently be swapped.
     * Cards may be swapped repeatedly, but only before the first bid has been made.
     * 
     * @return true if swapping is currently allowed.
     */
    public boolean canSwapCards() {
        boolean check = phase == GamePhase.BIDDING && currentDeal != null && auction != null
                && auction.getBids().isEmpty();
        return check;

    }

    /**
     * Swaps two cards belonging to two different players.
     * 
     * @param firstSeat  owner of the first card.
     * @param firstCard  first selected card
     * @param secondSeat owner of the second card.
     * @param secondCard second selected card
     */
    public boolean swapCards(Seat firstSeat, Card firstCard, Seat secondSeat, Card secondCard) {
        if (!canSwapCards()) {
            return false;
        }
        boolean check = currentDeal.swapCards(firstSeat, firstCard, secondSeat, secondCard);
        return check;
    }
}
