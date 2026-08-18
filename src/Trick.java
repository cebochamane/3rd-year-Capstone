import java.util.HashMap;
import java.util.Map;

public class Trick {

    private int trickNo;
    private Seat leader;
    private HashMap<Seat, Card> plays;
    private Seat winner;

    /**
     * Creates a new trick.
     *
     * @param trickNo the number of this trick
     * @param leader  the player who leads the trick
     */
    public Trick(int trickNo, Seat leader) {
        // TODO: implement
    }

    /**
     * Records a card played by a player in this trick.
     *
     * @param seat the player who played the card
     * @param card the card that was played
     */
    public void addPlay(Seat seat, Card card) {
        // TODO: implement
    }

    /**
     * Returns all cards played in this trick, associated with
     * the player who played each card.
     *
     * @return the map of players to cards
     */
    public Map<Seat, Card> getPlays() {
        // TODO: implement
        return null;
    }

    /**
     * Checks whether all four players have played a card.
     *
     * @return true if the trick contains four plays, otherwise false
     */
    public boolean isComplete() {
        // TODO: implement
        return false;
    }

    /**
     * Determines which player won the trick.
     *
     * The winner is determined using the lead suit and the
     * contract's trump suit.
     *
     * @param trump the trump suit, or Suit.NT for a No Trump contract
     * @return the seat that won the trick
     */
    public Seat getWinner(Suit trump) {
        // TODO: implement
        return null;
    }

    /**
     * Determines which card won the trick.
     *
     * @param trump the trump suit, or Suit.NT for a No Trump contract
     * @return the winning card
     */
    public Card getWinningCard(Suit trump) {
        // TODO: implement
        return null;
    }
}