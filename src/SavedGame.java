/**
 * Contains summary information about one saved game.
 *
 * This is used when displaying the list of games that
 * a user can load.
 */
public class SavedGame {

    private final String gameId;
    private final GamePhase phase;
    private final Seat dealer;
    private final String savedAt;

    public SavedGame(String gameId, GamePhase phase, Seat dealer, String savedAt) {

        this.gameId = gameId;
        this.phase = phase;
        this.dealer = dealer;
        this.savedAt = savedAt;
    }

    public String getGameId() {
        return gameId;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public Seat getDealer() {
        return dealer;
    }

    public String getSavedAt() {
        return savedAt;
    }

    /**
     * Swing uses this text when a SavedGame is displayed
     * inside a list or combo box.
     */
    @Override
    public String toString() {
        return savedAt + " | " + phase + " | Dealer: " + dealer;
    }
}