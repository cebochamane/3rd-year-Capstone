public class Card {

    private Suit suit;
    private Rank rank;

    /**
     * Creates a card with the specified suit and rank.
     *
     * @param suit the suit of the card
     * @param rank the rank of the card
     */
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    /**
     * Returns the suit of this card.
     *
     * @return the card's suit
     */
    public Suit getSuit() {
        return this.suit;
    }

    /**
     * Returns the rank of this card.
     *
     * @return the card's rank
     */
    public Rank getRank() {
        return this.rank;
    }

    /**
     * Compares this card with another card in the context of
     * the current trick.
     *
     * The comparison takes the trump suit and lead suit into
     * account because these determine which card has priority.
     *
     * @param other    the card to compare with this card
     * @param trump    the trump suit, or Suit.NT for No Trump
     * @param leadSuit the suit that was led in the trick
     * @return a positive value if this card wins, a negative
     *         value if the other card wins, or 0 if they are equal
     */
    public int compareTo(Card other, Suit trump, Suit leadSuit) {
        // TODO: implement
        return 0;
    }

    /**
     * Returns a human-readable representation of the card.
     *
     * For example, an Ace of Spades could be represented as "A♠".
     *
     * @return a string representation of this card
     */
    @Override
    public String toString() {
        return "Card: " + this.rank + " of " + this.suit;
    }
}