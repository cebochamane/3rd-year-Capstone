public class Bid {

    private Seat bidder;
    private int level;
    private Suit denomination;
    private boolean isPass;
    private boolean isDouble;
    private boolean isRedouble;

    /**
     * Creates a normal contract bid.
     *
     * @param bidder       the seat/player making the bid
     * @param level        the bid level (1 to 7)
     * @param denomination the suit or NT being bid
     */
    public Bid(Seat bidder, int level, Suit denomination) {
        // TODO: implement
    }

    /**
     * Creates a Pass bid for the specified bidder.
     *
     * This is a static factory method because Pass is a special
     * type of bid and does not require a level or denomination.
     *
     * @param bidder the seat/player passing
     * @return a Bid representing Pass
     */
    public static Bid BidPass(Seat bidder) {
        // TODO: implement
        return null;
    }

    /**
     * Creates a Double bid for the specified bidder.
     *
     * Double is a special bidding action and does not require
     * a level or denomination.
     *
     * @param bidder the seat/player making the double
     * @return a Bid representing Double
     */
    public static Bid BidDouble(Seat bidder) {
        // TODO: implement
        return null;
    }

    /**
     * Creates a Redouble bid for the specified bidder.
     *
     * Redouble is a special bidding action and does not require
     * a level or denomination.
     *
     * @param bidder the seat/player making the redouble
     * @return a Bid representing Redouble
     */
    public static Bid BidRedouble(Seat bidder) {
        // TODO: implement
        return null;
    }

    /**
     * Checks whether this bid is a Pass.
     *
     * @return true if this bid represents Pass, otherwise false
     */
    public boolean isPass() {
        // TODO: implement
        return false;
    }

    /**
     * Checks whether this bid is a Double.
     *
     * @return true if this bid represents Double, otherwise false
     */
    public boolean isDouble() {
        // TODO: implement
        return false;
    }

    /**
     * Checks whether this bid is a Redouble.
     *
     * @return true if this bid represents Redouble, otherwise false
     */
    public boolean isRedouble() {
        // TODO: implement
        return false;
    }
}