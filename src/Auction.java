import java.util.ArrayList;

public class Auction {

    private ArrayList<Bid> bids;

    /**
     * Creates a new empty auction.
     *
     * The bids will be stored in the order in which
     * they are made.
     */
    public Auction() {
        // TODO: implement
    }

    /**
     * Returns all bids that have been made in this auction.
     *
     * @return the list of bids in chronological order
     */
    public ArrayList<Bid> getBids() {
        // TODO: implement
        return null;
    }

    /**
     * Adds a bid to the auction.
     *
     * @param bid the bid being submitted
     */
    public void addBid(Bid bid) {
        // TODO: implement
    }

    /**
     * Checks whether a proposed bid is legal according
     * to the current state of the auction.
     *
     * This includes checking the rules for normal bids,
     * Pass, Double, and Redouble.
     *
     * @param bid the proposed bid
     * @return true if the bid is legal, otherwise false
     */
    public boolean isValidBid(Bid bid) {
        // TODO: implement
        return false;
    }

    /**
     * Checks whether the auction has ended.
     *
     * An auction ends when the required number of
     * consecutive passes has occurred.
     *
     * @return true if the auction is complete, otherwise false
     */
    public boolean isComplete() {
        // TODO: implement
        return false;
    }

    /**
     * Returns the final contract bid from the auction.
     *
     * This represents the highest valid contract bid made,
     * before considering the final doubling or redoubling status.
     *
     * @return the final contract bid, or null if no contract bid exists
     */
    public Bid getFinalBid() {
        // TODO: implement
        return null;
    }
}