import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Deal {

    private Seat dealer;
    private Vulnerability vulnerability;
    private HashMap<Position, Hand> hands;
    private ArrayList<Card> deck = new ArrayList<>();

    /**
     * Creates a new deal with the specified dealer and vulnerability.
     *
     * @param dealer        the seat responsible for dealing
     * @param vulnerability the vulnerability for this deal
     */
    public Deal(Seat dealer, Vulnerability vulnerability) {
        this.dealer = dealer;
        this.vulnerability = vulnerability;
        this.hands = new HashMap<>();

        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                if (suit != Suit.NT) {
                    deck.add(new Card(suit, rank));
                }
            }
        }
    }

    /**
     * Returns the dealer for this deal.
     *
     * @return the dealer's seat
     */
    public Seat getDealer() {
        return this.dealer;
    }

    /**
     * Returns the vulnerability for this deal.
     *
     * @return the vulnerability of the deal
     */
    public Vulnerability getVulnerability() {
        return this.vulnerability;
    }

    /**
     * Returns the hand belonging to the specified seat.
     *
     * @param seat the seat whose hand is requested
     * @return the hand belonging to that seat
     */
    public Hand getHand(Seat seat) {
        return hands.get(seat.getPosition());
    }

    /**
     * Returns all player hands in the deal.
     *
     * The map associates each seat with the hand belonging
     * to that seat.
     *
     * @return a map containing all seats and their hands
     */
    public Map<Position, Hand> getAllHands() {
        return hands;
    }

    // Additions to creating own hands:
    // Validating the validity of created deals
    public static Deal fromHands(Seat dealer, Vulnerability vulnerability, Map<Position, List<Card>> suppliedHands) {
        validateSuppliedHands(dealer, vulnerability, suppliedHands);

        Deal deal = new Deal(dealer, vulnerability);
        for (Position position : Position.values()) {
            Hand hand = new Hand(new ArrayList<>(suppliedHands.get(position)));
            hand.sort();
            deal.hands.put(position, hand);
        }
        return deal;
    }

    private static void validateSuppliedHands(Seat dealer, Vulnerability vulnerability, Map<Position, List<Card>> suppliedHands) {
        if (dealer == null || dealer.getPosition() == null) throw new IllegalArgumentException("A deal must have a dealer.");
        if (vulnerability == null) throw new IllegalArgumentException("A deal must have a vulnerability.");
        if (suppliedHands == null || suppliedHands.size() != Position.values().length) throw new IllegalArgumentException("A deal must contain hands for North, East, South, and West.");

        Set<String> seenCards = new HashSet<>();
        for (Position position : Position.values()) {
            List<Card> hand = suppliedHands.get(position);

            if (hand == null) throw new IllegalArgumentException("Missing hand for " + position + ".");
            if (hand.size() != 13) throw new IllegalArgumentException(position + " must have exactly 13 cards; found " + hand.size() + ".");
            
            for (Card card : hand) {
                if (card == null || card.getSuit() == null || card.getRank() == null) throw new IllegalArgumentException("Every entry must be a card with a suit and rank.");                
                if (card.getSuit() == Suit.NT) throw new IllegalArgumentException("No-trump is a contract denomination, not a card suit.");
                
                String key = card.getSuit() + "-" + card.getRank();
                if (!seenCards.add(key)) throw new IllegalArgumentException("Duplicate card in supplied deal: " + card + ".");
            }
        }
    }

    // End of addtitions

    public void dealCards() {
        ArrayList<Card> shuffledDeck = new ArrayList<>(deck);
        Collections.shuffle(shuffledDeck);
        Position[] positions = Position.values();

        // Round-robin distribution of cards to each Seat
        for (Position position : positions) {
            hands.put(position, new Hand());
        }
 
        for (int i = 0; i < shuffledDeck.size(); i++) {
            Position position = positions[i % positions.length];
            hands.get(position).addCard(shuffledDeck.get(i));;
        }

        // Sort dealt cards
        for (Position position : positions) {
            hands.get(position).sort();
        }
    }
}
