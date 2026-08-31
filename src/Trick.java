
import java.util.HashMap;
import java.util.Map;

/**
 * Represents one trick during the play of a Bridge game.
 *
 * A trick consists of up to four cards, one played by each
 * player. The trick also keeps track of the player who led it.
 */
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

        this.trickNo = trickNo;
        this.leader = leader;
        this.plays = new HashMap<>();
        this.winner = null;
    }

    /**
     * Returns the number of this trick.
     *
     * @return trick number
     */
    public int getTrickNo() {
        return trickNo;
    }

    /**
     * Returns the player who leads this trick.
     *
     * @return the leader
     */
    public Seat getLeader() {
        return leader;
    }

    /**
     * Records a card played by a player in this trick.
     *
     * @param seat the player who played the card
     * @param card the card that was played
     */
    public void addPlay(Seat seat, Card card) {

        if (seat == null || card == null) {
            System.out.println(
                    "Error: Seat or card is null.");
            return;
        }

        /*
         * A player can only play one card in a trick.
         */
        if (plays.containsKey(seat)) {
            System.out.println(
                    "Error: Player has already played a card in this trick.");
            return;
        }

        /*
         * A trick can contain a maximum of four cards.
         */
        if (plays.size() >= Position.values().length) {
            System.out.println(
                    "Error: Cannot add more plays to a completed trick.");
            return;
        }

        /*
         * Add the card to the trick.
         */
        plays.put(seat, card);
    }

    /**
     * Returns all cards played in this trick, associated with
     * the player who played each card.
     *
     * @return the map of players to cards
     */
    public Map<Seat, Card> getPlays() {
        return plays;
    }

    /**
     * Checks whether all four players have played a card.
     *
     * @return true if the trick contains four plays
     */
    public boolean isComplete() {
        return plays.size() == Position.values().length;
    }

    /**
     * Determines which player won the trick.
     *
     * The winner is determined using the lead suit and
     * the contract's trump suit.
     *
     * @param trump the trump suit, or Suit.NT for a No Trump contract
     * @return the seat that won the trick
     */
    public Seat getWinner(Suit trump) {

        if (!isComplete()) {
            throw new IllegalStateException(
                    "Cannot determine winner: trick is not complete.");
        }

        /*
         * If the winner has already been calculated,
         * return it.
         */
        if (winner != null) {
            return winner;
        }

        /*
         * The player who led the trick has the first card.
         */
        Seat bestSeat = leader;
        Card bestCard = plays.get(leader);

        if (bestCard == null) {
            throw new IllegalStateException(
                    "The leader has no card in this trick.");
        }

        Suit leadSuit = bestCard.getSuit();

        /*
         * Check every card played in the trick.
         */
        for (Map.Entry<Seat, Card> entry : plays.entrySet()) {

            Seat seat = entry.getKey();
            Card card = entry.getValue();

            /*
             * A trump card beats a non-trump card.
             */
            if (card.getSuit() == trump) {

                if (bestCard.getSuit() != trump
                        || card.getRank().ordinal() > bestCard.getRank().ordinal()) {

                    bestSeat = seat;
                    bestCard = card;
                }
            }

            /*
             * If the card is not trump, it can only win if
             * it follows the lead suit.
             */
            else if (card.getSuit() == leadSuit) {

                if (bestCard.getSuit() != trump
                        && card.getRank().ordinal() > bestCard.getRank().ordinal()) {

                    bestSeat = seat;
                    bestCard = card;
                }
            }
        }

        /*
         * Remember the winner.
         */
        winner = bestSeat;

        return winner;
    }

    /**
     * Determines which card won the trick.
     *
     * @param trump the trump suit, or Suit.NT for a No Trump contract
     * @return the winning card
     */
    public Card getWinningCard(Suit trump) {

        Seat winningSeat = getWinner(trump);

        return plays.get(winningSeat);
    }
}
