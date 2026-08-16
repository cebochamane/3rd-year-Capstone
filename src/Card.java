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
        boolean thisIsTrump = (trump != Suit.NT) && (this.suit == trump);
        boolean otherIsTrump = (trump != Suit.NT) && (other.suit == trump);

        if (thisIsTrump && !otherIsTrump) return 1;
        if (otherIsTrump && !thisIsTrump) return -1;
        if (thisIsTrump && !otherIsTrump) return this.rank.compareTo(other.rank);

        // Neither card is trump — only the lead suit is in contention
        boolean thisIsLead = (this.suit == leadSuit);
        boolean otherIsLead = (other.suit == leadSuit);

        if (thisIsLead && !otherIsLead) return 1;
        if (otherIsLead && !thisIsLead) return -1;
        if (thisIsLead && otherIsLead) return this.rank.compareTo(other.rank);

        return 0; // neither card can win the trick either way   
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
        return this.rank + " of " + this.suit;
    }
}