import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a deal draft in a Bridge game, where players are assigned cards.
 * The deal draft ensures that each player has exactly 13 cards before creating a deal.
 * The deal draft enables the creation of a custom deal instead of a random one.
 */
public class DealDraft {

    private final Seat dealer;
    private final Vulnerability vulnerability;
    private final Map<Position, ArrayList<Card>> handsByPosition;

    /**
     * Constructs a DealDraft object with a specified dealer and vulnerability.
     *
     * @param dealer The seat representing the dealer for the deal
     * @param vulnerability The vulnerability setting for the deal
     * @throws IllegalArgumentException if dealer is null or dealer's position is null, or if vulnerability is null
     */
    public DealDraft(Seat dealer, Vulnerability vulnerability) {
        if (dealer == null || dealer.getPosition() == null) throw new IllegalArgumentException("A deal must have a dealer.");
        if (vulnerability == null) throw new IllegalArgumentException("A deal must have a vulnerability.");        

        this.dealer = dealer;
        this.vulnerability = vulnerability;
        handsByPosition = new EnumMap<>(Position.class);
        for (Position position : Position.values()) handsByPosition.put(position, new ArrayList<>());
    }

    /**
     * Assigns a card to a specific position in the game.
     *
     * @param position The position where the card will be assigned
     * @param card The card to be assigned
     * @throws IllegalArgumentException if the position already has 13 cards or if the card has already been assigned
     */
    public void assignCard(Position position, Card card) {
        validateCard(card);
        ArrayList<Card> hand = handAt(position);

        if (hand.size() == 13) throw new IllegalArgumentException(position + " already has 13 cards.");
        if (isAssigned(card)) throw new IllegalArgumentException(card + " has already been assigned.");        
        
        hand.add(card);
    }

    /**
     * Removes a specific card from the hand at the given position.
     *
     * @param position The position of the hand from which to remove the card
     * @param card The card to be removed
     * @return true if the card was successfully removed, false otherwise
     */
    public boolean removeCard(Position position, Card card) {
        ArrayList<Card> hand = handAt(position);
        for (int index = 0; index < hand.size(); index++) {
            if (sameCard(hand.get(index), card)) {
                hand.remove(index);
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a list of cards at the specified position.
     *
     * @param position The position from which to retrieve the cards
     * @return An immutable list of cards at the specified position
     */
    public List<Card> getCards(Position position) {
        return List.copyOf(handAt(position));
    }

    /**
     * Retrieves a list of available cards that have not been assigned.
     *
     * @return A list of available cards that have not been assigned yet.
     */
    public List<Card> getAvailableCards() {
        ArrayList<Card> available = new ArrayList<>();
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                if (suit == Suit.NT) continue;                
                Card card = new Card(suit, rank);
                if (!isAssigned(card)) available.add(card);                
            }
        }
        return List.copyOf(available);
    }

    /**
     * Checks if all hands in the game are complete, i.e., each player has 13 cards.
     *
     * @return true if all hands are complete, false otherwise
     */
    public boolean isComplete() {
        for (Position position : Position.values()) {
            if (handsByPosition.get(position).size() != 13) return false;
        }
        return true;
    }

    /**
     * Creates a deal based on the completed hands of each player.
     *
     * @return The created deal object
     * @throws IllegalStateException if each player does not have exactly 13 cards before loading the deal
     */
    public Deal createDeal() {
        if (!isComplete()) throw new IllegalStateException("Each player must have exactly 13 cards before loading the deal.");

        Map<Position, List<Card>> completedHands = new EnumMap<>(Position.class);
        for (Position position : Position.values()) completedHands.put(position, new ArrayList<>(handsByPosition.get(position)));
        return Deal.fromHands(dealer, vulnerability, completedHands);
    }

    /**
     * Retrieves the hand of cards at the specified position.
     *
     * @param position The position of the player whose hand is to be retrieved
     * @return The list of cards in the player's hand at the specified position
     * @throws IllegalArgumentException if the position is null
     */
    private ArrayList<Card> handAt(Position position) {
        if (position == null) throw new IllegalArgumentException("A player position is required.");
        return handsByPosition.get(position);
    }

    /**
     * Checks if a card is already assigned to a player's hand.
     *
     * @param card The card to check if it is assigned
     * @return true if the card is already assigned, false otherwise
     */
    private boolean isAssigned(Card card) {
        for (ArrayList<Card> hand : handsByPosition.values()) {
            for (Card assignedCard : hand) {
                if (sameCard(assignedCard, card)) return true;
            }
        }
        return false;
    }

    /**
     * Checks if two cards are the same by comparing their suit and rank.
     *
     * @param first The first card to compare
     * @param second The second card to compare
     * @return true if both cards are not null and have the same suit and rank, false otherwise
     */
    private static boolean sameCard(Card first, Card second) {
        return first != null && second != null && first.getSuit() == second.getSuit() && first.getRank() == second.getRank();
    }

    /**
     * Validates a card to ensure it has a suit and rank, and that the suit is not No-trump.
     *
     * @param card The card to validate
     * @throws IllegalArgumentException if the card is null, or if it has no suit or rank, or if the suit is No-trump
     */
    private static void validateCard(Card card) {
        if (card == null || card.getSuit() == null || card.getRank() == null) throw new IllegalArgumentException("A card must have a suit and rank.");
        if (card.getSuit() == Suit.NT) throw new IllegalArgumentException("No-trump is a contract denomination, not a card suit.");
    }
}
