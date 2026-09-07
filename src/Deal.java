import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a deal in a card game.
 *
 * This class contains information about the dealer, vulnerability, hands of
 * players,
 * and the deck of cards used in the deal.
 *
 * @param dealer        The player who is the dealer in this deal.
 * @param vulnerability The vulnerability setting for this deal.
 * @param hands         A mapping of player positions to their respective hands.
 * @param deck          The deck of cards used in the deal.
 *
 *                      The constructor creates a new random deal with the
 *                      specified dealer and vulnerability.
 *                      It distributes the cards from a shuffled deck to each
 *                      player's hand and sorts the hands.
 */
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

    /**
     * Creates a Deal object from the supplied hands, dealer, and vulnerability.
     *
     * @param dealer        The seat of the dealer for the deal.
     * @param vulnerability The vulnerability for the deal.
     * @param suppliedHands A map containing the hands for each position.
     * @return The Deal object created from the supplied information.
     * @throws InvalidDealException if the supplied hands are invalid for the given
     *                              dealer and vulnerability.
     */
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

    /**
     * Validates the supplied hands for a deal in a card game.
     *
     * @param dealer        The dealer of the deal.
     * @param vulnerability The vulnerability of the deal.
     * @param suppliedHands A map containing hands for each position (North, East,
     *                      South, West).
     * @throws IllegalArgumentException if the dealer is null, vulnerability is
     *                                  null, or if the suppliedHands map is
     *                                  invalid.
     */
    private static void validateSuppliedHands(Seat dealer, Vulnerability vulnerability,
            Map<Position, List<Card>> suppliedHands) {
        if (dealer == null || dealer.getPosition() == null)
            throw new IllegalArgumentException("A deal must have a dealer.");
        if (vulnerability == null)
            throw new IllegalArgumentException("A deal must have a vulnerability.");
        if (suppliedHands == null || suppliedHands.size() != Position.values().length)
            throw new IllegalArgumentException("A deal must contain hands for North, East, South, and West.");

        Set<String> seenCards = new HashSet<>();
        for (Position position : Position.values()) {
            List<Card> hand = suppliedHands.get(position);

            if (hand == null)
                throw new IllegalArgumentException("Missing hand for " + position + ".");
            if (hand.size() != 13)
                throw new IllegalArgumentException(
                        position + " must have exactly 13 cards; found " + hand.size() + ".");

            for (Card card : hand) {
                if (card == null || card.getSuit() == null || card.getRank() == null)
                    throw new IllegalArgumentException("Every entry must be a card with a suit and rank.");
                if (card.getSuit() == Suit.NT)
                    throw new IllegalArgumentException("No-trump is a contract denomination, not a card suit.");

                String key = card.getSuit() + "-" + card.getRank();
                if (!seenCards.add(key))
                    throw new IllegalArgumentException("Duplicate card in supplied deal: " + card + ".");
            }
        }
    }

    /**
     * Deals cards to players based on their positions.
     * It shuffles the deck, creates hands for each player, distributes cards, and
     * sorts each player's hand.
     */
    public void dealCards() {
        ArrayList<Card> shuffledDeck = new ArrayList<>(deck);
        Collections.shuffle(shuffledDeck);
        Position[] positions = Position.values();

        for (Position position : positions) {
            hands.put(position, new Hand());
        }

        for (int i = 0; i < shuffledDeck.size(); i++) {
            Position position = positions[i % positions.length];
            hands.get(position).addCard(shuffledDeck.get(i));
            ;
        }

        for (Position position : positions) {
            hands.get(position).sort();
        }
    }

    /**
     * Swaps one card between two different players.
     * 
     * @param firstSeat  first player
     * @param firstCard  card belonging to the first player.
     * @param secondSeat second player
     * @param secondCard card belonging to the second player.
     * @return true if the swap was successful
     */
    public boolean swapCards(Seat firstSeat, Card firstCard, Seat secondSeat, Card secondCard) {
        /*
         * Every argument is required
         */
        if (firstSeat == null || firstCard == null || secondSeat == null || secondCard == null) {
            return false;
        }

        /*
         * Cards must come from two different players
         */
        if (firstSeat.getPosition() == secondSeat.getPosition()) {
            return false;
        }

        Hand firstHand = getHand(firstSeat);
        Hand secondHand = getHand(secondSeat);

        if (firstHand == null || secondHand == null) {
            return false;
        }
        /*
         * Validate both cards befoere changing either hand
         */
        if (!firstHand.contains(firstCard) || !secondHand.contains(secondCard)) {
            return false;
        }

        /*
         * Remove the selected cards
         */
        firstHand.removeCard(firstCard);
        secondHand.removeCard(secondCard);

        /*
         * Give each player the other player's card
         */
        firstHand.addCard(secondCard);
        secondHand.addCard(firstCard);

        /*
         * Restore the rank and suit order.
         */
        firstHand.sort();
        secondHand.sort();

        return true;
    }
}
