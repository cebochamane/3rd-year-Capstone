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
        this.bids = new ArrayList<>();
    }

    /**
     * Returns all bids that have been made in this auction.
     *
     * @return the list of bids in chronological order
     */
    public ArrayList<Bid> getBids() {
        return bids;
    }

    /**
     * Adds a bid to the auction.
     *
     * @param bid the bid being submitted
     */
    public void addBid(Bid bid) {
        bids.add(bid);
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
        if (bid == null) {
            return false;
        } else if (bid.isPass()) {
            if (this.isComplete()) {
                return false;
            }
            return true;
        } else if (bid.isDouble()) {
            Bid lastContractBid = this.getFinalBid();

            if (this.getFinalBid() == null) {
                return false;
            } else if (!bid.getBidder().isPartner(lastContractBid.getBidder())) {
                return true;
            } else {
                return false;
            }

        } else if (bid.isRedouble()) {
            if (bids.isEmpty()) {
                return false;
            }
            Bid lastBid = bids.get(bids.size() - 1);

            if (lastBid.isDouble() && !lastBid.getBidder().isPartner(bid.getBidder())) {
                return true;
            }
            return false;
        } else {
            if (bid.getLevel() < 1 || bid.getLevel() > 7) {
                return false;
            }
            if (bid.getDenomination() == null) {
                return false;
            }
            Bid lastContractBid = this.getFinalBid();

            if (lastContractBid != null && !this.isHigherBid(bid, lastContractBid)) {
                return false;
            }
            return true;
        }

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
        if (bids.isEmpty()) {
            return false;
        }
        int consecutivePasses = 0;
        for (int i = bids.size() - 1; i >= 0; i--) {
            if (bids.get(i).isPass()) {
                consecutivePasses++;
            } else {
                break;
            }
        }
        return consecutivePasses >= 3;
    }

    /**
     * Returns the final contract bid from the auction.
     *
     * This epresents the highest val contract bid made,
     * before considering the final doubling or redoubling status.
     * 
     * @return the final contract bid, or null if no contract bid exists
     */
    public Bid getFinalBid() {
        for (int i = bids.size() - 1; i >= 0; i--) {
            Bid bid = bids.get(i);
            if (!bid.isPass() && !bid.isDouble() && !bid.isRedouble()) {
                return bid;
            }
        }
        return null;
    }

    /**
     * Checks whether a proposed contract bid is higher than the current contract
     * bid
     * 
     * A higher level always beats a lower level. If both bids have the same level,
     * the denomination determines the which bid is higher.
     * 
     * @param newBid     the proposed contract bid
     * @param currentBid the current highest contract bid
     * @return true if the new bid is higher, otherwise false
     */
    private boolean isHigherBid(Bid newBid, Bid currentBid) {
        if (newBid.getLevel() >= currentBid.getLevel()) {
            return true;
        } else if (newBid.getDenomination().ordinal() > currentBid.getDenomination().ordinal()) {
            return true;
        } else {
            return false;
        }
    }
}