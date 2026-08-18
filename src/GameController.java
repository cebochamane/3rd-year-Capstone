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


    public void startRandomDeal(Seat dealer, Vulnerability vulnerability) {
        Deal deal = new Deal(dealer, vulnerability);
        deal.dealCards();
        startLoadedDeal(deal);
    }

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

    public GamePhase getPhase() {
        return phase;
    }

    public Deal getCurrentDeal() {
        return currentDeal;
    }

    public Auction getAuction() {
        return auction;
    }

    public Seat getCurrentBidder() {
        return currentBidder;
    }

    public Contract getContract() {
        return contract;
    }

    public Gameplay getGameplay() {
        return gameplay;
    }

    private boolean isCompleteDeal(Deal deal) {
        if (deal == null || deal.getAllHands().size() != Position.values().length) return false;
        
        for (Position position : Position.values()) {
            Hand hand = deal.getHand(new Seat(position));
            if (hand == null || hand.getCards().size() != 13) return false;
        }

        return true;
    }
}