import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Deal {

    private Seat dealer;
    private Vulnerability vulnerability;
    private HashMap<Position, Hand> hands;
    private ArrayList<Card> deck = new ArrayList<>();

    /**
     * Creates a new random deal with the specified dealer and vulnerability.
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

    /** Tester main method */
    public static void main(String args[]) {
        Deal thisDeal = new Deal(new Seat(Position.EAST), Vulnerability.NONE);
        thisDeal.dealCards();
        System.out.println(thisDeal.hands.get(Position.NORTH).contains(Suit.HEARTS));
    }
}
