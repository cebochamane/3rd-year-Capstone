import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** GUI-independent, in-progress manual Bridge deal. */
public class DealDraft {

    private final Seat dealer;
    private final Vulnerability vulnerability;
    private final Map<Position, ArrayList<Card>> handsByPosition;

    public DealDraft(Seat dealer, Vulnerability vulnerability) {
        if (dealer == null || dealer.getPosition() == null) throw new IllegalArgumentException("A deal must have a dealer.");
        if (vulnerability == null) throw new IllegalArgumentException("A deal must have a vulnerability.");        

        this.dealer = dealer;
        this.vulnerability = vulnerability;
        handsByPosition = new EnumMap<>(Position.class);
        for (Position position : Position.values()) handsByPosition.put(position, new ArrayList<>());
    }

    /** Assigns one selected card to a player. */
    public void assignCard(Position position, Card card) {
        validateCard(card);
        ArrayList<Card> hand = handAt(position);

        if (hand.size() == 13) throw new IllegalArgumentException(position + " already has 13 cards.");
        if (isAssigned(card)) throw new IllegalArgumentException(card + " has already been assigned.");        
        
        hand.add(card);
    }

    /** Removes the matching selected card, if present. */
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

    /** Returns an immutable copy of a player's selected cards. */
    public List<Card> getCards(Position position) {
        return List.copyOf(handAt(position));
    }

    /** Returns the ordinary cards that are not yet assigned. */
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

    /** Returns true only when every player has 13 selected cards. */
    public boolean isComplete() {
        for (Position position : Position.values()) {
            if (handsByPosition.get(position).size() != 13) return false;
        }
        return true;
    }

    /** Validates and creates the completed deal. */
    public Deal createDeal() {
        if (!isComplete()) throw new IllegalStateException("Each player must have exactly 13 cards before loading the deal.");

        Map<Position, List<Card>> completedHands = new EnumMap<>(Position.class);
        for (Position position : Position.values()) completedHands.put(position, new ArrayList<>(handsByPosition.get(position)));
        return Deal.fromHands(dealer, vulnerability, completedHands);
    }

    private ArrayList<Card> handAt(Position position) {
        if (position == null) throw new IllegalArgumentException("A player position is required.");
        return handsByPosition.get(position);
    }

    private boolean isAssigned(Card card) {
        for (ArrayList<Card> hand : handsByPosition.values()) {
            for (Card assignedCard : hand) {
                if (sameCard(assignedCard, card)) return true;
            }
        }
        return false;
    }

    private static boolean sameCard(Card first, Card second) {
        return first != null && second != null && first.getSuit() == second.getSuit() && first.getRank() == second.getRank();
    }

    private static void validateCard(Card card) {
        if (card == null || card.getSuit() == null || card.getRank() == null) throw new IllegalArgumentException("A card must have a suit and rank.");
        if (card.getSuit() == Suit.NT) throw new IllegalArgumentException("No-trump is a contract denomination, not a card suit.");
    }
}
