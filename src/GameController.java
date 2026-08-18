/**
 * The GameController class manages the game logic and state for a Bridge game.
 * It includes methods for starting a new game, loading and saving games,
 * dealing cards, conducting auctions, playing the game, and managing game phases.
 */
public class GameController {

    private GameView view;
    private Storage storage;
    private Deal currentDeal;
    private Auction auction;
    private Contract contract;
    private Gameplay gameplay;
    private GamePhase phase = GamePhase.MENU;
    private Seat currentBidder;

    /**
     * Starts a new bridge game by creating a new deal
     * and preparing the game for the auction phase.
     */
    public void startNewGame() {
        // TODO: implement
    }

    /**
     * Loads a previously saved game using its game ID.
     *
     * @param gameId the ID of the game to load
     */
    public void loadGame(String gameId) {
        // TODO: implement
    }

    /**
     * Saves the current game state using the storage system.
     */
    public void saveGame() {
        // TODO: implement
    }


    /**
     * Starts a random deal with the specified dealer and vulnerability.
     *
     * @param dealer The seat of the dealer for the deal
     * @param vulnerability The vulnerability for the deal
     */
    public void startRandomDeal(Seat dealer, Vulnerability vulnerability) {
        Deal deal = new Deal(dealer, vulnerability);
        deal.dealCards();
        startLoadedDeal(deal);
    }

    /**
     * Starts a loaded deal if it contains four hands of 13 cards.
     *
     * @param deal The deal to start
     * @throws IllegalArgumentException if the deal does not contain four hands of 13 cards
     */
    public void startLoadedDeal(Deal deal) {
        if (!isCompleteDeal(deal)) throw new IllegalArgumentException("A loaded deal must contain four hands of 13 cards.");
        this.currentDeal = deal;
        startAuction();
    }

    /**
     * Starts the auction phase for the current deal.
     */
    public void startAuction() {
        // TODO: implement
    }

    /**
     * Submits a bid made by a player to the current auction.
     *
     * @param bid the bid being submitted
     */
    public void submitBid(Bid bid) {
        // TODO: implement
    }
    
    /**
     * Starts the gameplay with the completed contract if the game phase is AUCTION_COMPLETE and the completed contract is not null.
     *
     * @param completedContract The completed contract to start the gameplay with.
     * @return true if the gameplay started successfully, false otherwise.
     */
    public boolean startPlay(Contract completedContract) {
        if (phase != GamePhase.AUCTION_COMPLETE || completedContract == null) return false;

        contract = completedContract;
        gameplay = new Gameplay(currentDeal, contract);
        phase = GamePhase.PLAYING;
        return true;
    }

    /**
     * Undoes the most recent game action when possible.
     */
    public void undo() {
        // TODO: implement
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
     * Retrieves the current phase of the game.
     *
     * @return The current phase of the game.
     */
    public GamePhase getPhase() {
        return phase;
    }

    /**
     * Retrieves the current deal.
     *
     * @return The current deal object.
     */
    public Deal getCurrentDeal() {
        return currentDeal;
    }

    /**
     * Retrieves the auction object associated with this instance.
     *
     * @return The auction object.
     */
    public Auction getAuction() {
        return auction;
    }

    /**
     * Retrieves the current bidder of the auction.
     *
     * @return The Seat object representing the current bidder.
     */
    public Seat getCurrentBidder() {
        return currentBidder;
    }

    /**
     * Retrieves the contract associated with this instance.
     *
     * @return The contract object associated with this instance.
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Returns the gameplay object associated with this instance.
     *
     * @return The gameplay object.
     */
    public Gameplay getGameplay() {
        return gameplay;
    }

    /**
     * Checks if a deal is complete, meaning all hands are present and have 13 cards each.
     *
     * @param deal The deal to check completeness for
     * @return true if the deal is complete, false otherwise
     */
    private boolean isCompleteDeal(Deal deal) {
        if (deal == null || deal.getAllHands().size() != Position.values().length) return false;
        
        for (Position position : Position.values()) {
            Hand hand = deal.getHand(new Seat(position));
            if (hand == null || hand.getCards().size() != 13) return false;
        }

        return true;
    }
}