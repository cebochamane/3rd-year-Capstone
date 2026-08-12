import java.util.HashMap;
import java.util.Map;

public class Deal {

    private Seat dealer;
    private Vulnerability vulnerability;
    private HashMap<Seat, Hand> hands;

    /**
     * Creates a new deal with the specified dealer and vulnerability.
     *
     * @param dealer        the seat responsible for dealing
     * @param vulnerability the vulnerability for this deal
     */
    public Deal(Seat dealer, Vulnerability vulnerability) {
        // TODO: implement
    }

    /**
     * Returns the dealer for this deal.
     *
     * @return the dealer's seat
     */
    public Seat getDealer() {
        // TODO: implement
        return null;
    }

    /**
     * Returns the vulnerability for this deal.
     *
     * @return the vulnerability of the deal
     */
    public Vulnerability getVulnerability() {
        // TODO: implement
        return null;
    }

    /**
     * Returns the hand belonging to the specified seat.
     *
     * @param seat the seat whose hand is requested
     * @return the hand belonging to that seat
     */
    public Hand getHand(Seat seat) {
        // TODO: implement
        return null;
    }

    /**
     * Returns all player hands in the deal.
     *
     * The map associates each seat with the hand belonging
     * to that seat.
     *
     * @return a map containing all seats and their hands
     */
    public Map<Seat, Hand> getAllHands() {
        // TODO: implement
        return null;
    }
}