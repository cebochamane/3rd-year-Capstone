public class Storage {

    private String filePath;

    /**
     * Creates a storage object using the specified file path.
     *
     * @param filePath the location used for persistent game data
     */
    public Storage(String filePath) {
        // TODO: implement
    }

    /**
     * Saves the complete game state and returns its game ID.
     *
     * @param controller the game controller containing the current state
     * @return the ID assigned to the saved game
     */
    public String saveGame(GameController controller) {
        // TODO: implement
        return "";
    }

    /**
     * Loads a previously saved game using its game ID.
     *
     * @param gameId the ID of the game to load
     * @return the loaded game controller
     */
    public GameController loadGame(String gameId) {
        // TODO: implement
        return null;
    }

    /**
     * Saves the current deal.
     *
     * @param deal the deal to save
     */
    public void saveDeal(Deal deal) {
        // TODO: implement
    }

    /**
     * Saves the current auction.
     *
     * @param auction the auction to save
     */
    public void saveAuction(Auction auction) {
        // TODO: implement
    }

    /**
     * Saves the current contract.
     *
     * @param contract the contract to save
     */
    public void saveContract(Contract contract) {
        // TODO: implement
    }

    /**
     * Saves the current gameplay state.
     *
     * @param gameplay the gameplay state to save
     */
    public void saveGameplay(Gameplay gameplay) {
        // TODO: implement
    }

    /**
     * Saves the score for the completed game.
     *
     * @param score the score to save
     */
    public void saveScore(Score score) {
        // TODO: implement
    }
}