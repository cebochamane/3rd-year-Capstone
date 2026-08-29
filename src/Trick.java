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
        if (trickNo < 1 || trickNo > 13) {
            throw new IllegalArgumentException("Trick number must be between 1 and 13");
        }
        if (leader == null) {
            throw new IllegalArgumentException("Leader cannot be null");
        }
        this.trickNo = trickNo;
        this.leader = leader;
        this.plays = new HashMap<>();

        this.winner = null;

    }

    /**
     * Records a card played by a player in this trick.
     *
     * @param seat the player who played the card
     * @param card the card that was played
     */
    public void addPlay(Seat seat, Card card) {
        if (seat == null || card == null) {
            System.out.println("Error: Seat or card is null.");
            return;
        }
        if (this.plays.containsKey(seat)) {
            System.out.println("Error: Player has already played a card in this trick.");
            return;
        }
        if (this.plays.size() >= 4) {
            // Trick is complete
            isComplete();
            System.err.println("Error: Cannot add more plays to a completed trick.");
            return;
        } else if (this.plays.size() < 4) {
            this.plays.put(seat, card);
            return;
        } else {
            System.err.println("Error: More than 4 plays made in the trick.");
        }

        winner = null;
        this.plays.put(seat, card);

    }

    /**
     * Returns all cards played in this trick, associated with
     * the player who played each card.
     *
     * @return the map of players to cards
     */
    public Map<Seat, Card> getPlays() {
        return this.plays;
    }

    /**
     * Checks whether all four players have played a card.
     *
     * @return true if the trick contains four plays, otherwise false
     */
    public boolean isComplete() {
        return this.plays.size() == Position.values().length;

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
        if (!isComplete()) {
            throw new IllegalStateException("Cannot determine winner: trick is not complete.");
        }
        Seat bestSeat = leader;
        Card bestCard = plays.get(leader);
        Suit leadSuit = bestCard.getSuit();

        for (Map.Entry<Seat, Card> entry : plays.entrySet()) {
            Seat seat = entry.getKey();
            Card card = entry.getValue();

            if (card.getSuit() == trump) {
                if (bestCard.getSuit() != trump || card.getRank().ordinal() > bestCard.getRank().ordinal()) {
                    bestSeat = seat;
                    bestCard = card;
                }
            } else if (card.getSuit() == leadSuit) {
                if (bestCard.getSuit() != trump && card.getRank().ordinal() > bestCard.getRank().ordinal()) {
                    bestSeat = seat;
                    bestCard = card;
                }
            }
        }
        return bestSeat;
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