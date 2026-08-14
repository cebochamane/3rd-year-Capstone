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
        this.bidder = bidder;
        this.level = level;
        this.denomination = denomination;
        this.isPass = false;
        this.isDouble = false;
        this.isRedouble = false;
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
        Bid bid = new Bid(bidder, 0, null);
        bid.isPass = true;
        return bid;
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
        Bid bid = new Bid(bidder, 0, null);
        bid.isDouble = true;
        return bid;
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
        Bid bid = new Bid(bidder, 0, null);
        bid.isRedouble = true;
        return bid;
    }

    /**
     * Checks whether this bid is a Pass.
     *
     * @return true if this bid represents Pass, otherwise false
     */
    public boolean isPass() {
        return isPass;
    }

    /**
     * Checks whether this bid is a Double.
     *
     * @return true if this bid represents Double, otherwise false
     */
    public boolean isDouble() {
        return isDouble;
    }

    /**
     * Checks whether this bid is a Redouble.
     *
     * @return true if this bid represents Redouble, otherwise false
     */
    public boolean isRedouble() {
        return isRedouble;
    }

    /**
     * Returns the seat/player who made this bid.
     * 
     * @return the bidder
     */
    public Seat getBidder() {
        return bidder;
    }

    /**
     * Returns the level of the this bid.
     * 
     * @returns the bid level zero if the the bid is a Pass, Double and Redouble.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the Suit or the Denomination of this Bid.
     * Pass, Double and Redouble have to denomination.
     * returns the suit or denomination or a null.
     */
    public Suit getDenomination(){
        return denomination;
    }
}