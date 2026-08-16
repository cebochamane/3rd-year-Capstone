import java.util.ArrayList;

public class Hand {

    private ArrayList<Card> cards;

    /**
     * Creates an empty hand.
     *
     * Cards can be added to the hand using addCard().
     */
    public Hand(ArrayList<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public Hand() {
        this.cards = new ArrayList<>();
    }

    /**
     * Returns all cards currently in the hand.
     *
     * @return the cards in this hand
     */
    public ArrayList<Card> getCards() {
        return this.cards;
    }

    /**
     * Adds a card to the hand.
     *
     * @param card the card to add
     */
    public void addCard(Card card) {
        this.cards.add(card);
    }

    /**
     * Removes a card from the hand.
     *
     * @param card the card to remove
     */
    public void removeCard(Card card) {
        // TODO: implement
    }

    /**
     * Sorts the cards in the hand according to the ordering
     * defined by Card.compareTo().
     */
    public void sort() {
        cards.sort((a, b) -> {
            int bySuit = b.getSuit().compareTo(a.getSuit());
            if (bySuit != 0) return bySuit;
            return b.getRank().compareTo(a.getRank());
        });
    }

    /**
     * Checks whether a specific card is in the hand.
     *
     * @param card the card to search for
     * @return true if the hand contains the card, otherwise false
     */
    public boolean contains(Card card) {
        if (card == null) return false;

        for (Card c : cards) {
            if (c.getSuit() == card.getSuit() && c.getRank() == card.getRank()) return true;
        }

        return false;
    }

    /**
     * Checks whether the hand contains at least one card
     * belonging to the specified suit.
     *
     * This is useful when enforcing the bridge rule that a
     * player must follow the lead suit if they have one.
     *
     * @param suit the suit to search for
     * @return true if the hand contains a card of that suit
     */
    public boolean contains(Suit suit) {
        for (Card card : cards) if (card.getSuit() == suit) return true;
        return false;
    }

    /**
     * Returns all cards that are legally playable given
     * the lead suit and trump suit.
     *
     * If the player has a card in the lead suit, only cards
     * of that suit are legal. Otherwise, any card may be played.
     *
     * @param leadSuit the suit that was led in the current trick
     * @param trump    the trump suit, or Suit.NT for No Trump
     * @return all cards that can legally be played
     */
    public ArrayList<Card> getLegalCards(Suit leadSuit, Suit trump) {
        // TODO: implement
        return null;
    }

    public String toString() {
        return "Hand: " + cards;
    }
}