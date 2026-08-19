import java.util.ArrayList;

/**
 * Represents a hand of cards in a Bridge game.
 */
public class Hand {

    private ArrayList<Card> cards;

    /**
     * Constructs a Hand object with the given list of cards.
     *
     * @param cards The list of cards to initialize the hand with
     */
    public Hand(ArrayList<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    /**
     * Constructs a Hand object representing a player's hand of cards.
     * The hand is initially empty.
     */
    public Hand() {
        this.cards = new ArrayList<>();
    }

    /**
     * Returns the list of cards held by the player.
     *
     * @return An ArrayList of Card objects representing the player's cards.
     */
    public ArrayList<Card> getCards() {
        return this.cards;
    }

    /**
     * Adds a card to the hand.
     *
     * @param card The card to be added
     */
    public void addCard(Card card) {
        this.cards.add(card);
    }

    /**
     * Removes a card from the hand.
     *
     * @param card The card to remove
     */
    public void removeCard(Card card) {
        // TODO: implement
    }

    /**
     * Sorts the list of cards in descending order by suit and then by rank.
     */
    public void sort() {
        cards.sort((a, b) -> {
            int bySuit = b.getSuit().compareTo(a.getSuit());
            if (bySuit != 0) return bySuit;
            return b.getRank().compareTo(a.getRank());
        });
    }

    /**
     * Checks if the given card is present in the collection of cards.
     *
     * @param card The card to check for in the collection
     * @return true if the card is present, false otherwise
     */
    public boolean contains(Card card) {
        if (card == null) return false;

        for (Card c : cards) {
            if (c.getSuit() == card.getSuit() && c.getRank() == card.getRank()) return true;
        }

        return false;
    }

    /**
     * Checks if the collection of cards contains a card with the specified suit.
     *
     * @param suit The suit to check for in the collection of cards.
     * @return true if a card with the specified suit is found, false otherwise.
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

    /**
     * Returns a string representation of the Hand object.
     *
     * @return A string containing the cards in the hand
     */
    public String toString() {
        return "Hand: " + cards;
    }
}