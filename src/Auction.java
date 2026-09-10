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
    public boolean addBid(Bid bid) {
        if (this.isValidBid(bid)) {
            bids.add(bid);
            return true;
        }
        return false;
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
        /*
         * A null bid, a bid without a player, or a bid
         * submitted after completion is invalid.
         */
        if (bid == null || bid.getBidder() == null || this.isComplete()) {
            return false;
        }

        /*
         * A Pass is legal while the auction is still active.
         */
        if (bid.isPass()) {
            return true;
        }

        /*
         * Validate a Double.
         */
        if (bid.isDouble()) {
            Bid lastNonPassBid = this.getLastNonPassBid();

            /*
             * Nothing can be doubled before a contract bid.
             */
            if (lastNonPassBid == null) {
                return false;
            }

            /*
             * The last meaningful call must be a contract.
             * A Double cannot follow a Double or Redouble.
             */
            if (lastNonPassBid.isDouble() || lastNonPassBid.isRedouble()) {
                return false;
            }

            /*
             * A player may only Double an opponent's contract.
             */
            return !this.areOnSameSide(bid.getBidder(), lastNonPassBid.getBidder());
        }

        /*
         * Validate a Redouble.
         */
        if (bid.isRedouble()) {
            Bid lastNonPassBid = this.getLastNonPassBid();

            /*
             * A Redouble must follow a Double.
             * Intervening Passes are ignored.
             */
            if (lastNonPassBid == null || !lastNonPassBid.isDouble()) {
                return false;
            }

            Bid lastContractBid = this.getFinalBid();

            /*
             * There must be a contract that was doubled.
             */
            if (lastContractBid == null) {
                return false;
            }

            /*
             * The Redouble must come from the partnership that made the current contract.
             */
            boolean bidderOwnsContract = this.areOnSameSide(bid.getBidder(), lastContractBid.getBidder());

            /*
             * The player who Redoubles must be an opponent of the player who Doubled.
             */
            boolean doublerIsOpponent = !this.areOnSameSide(bid.getBidder(), lastNonPassBid.getBidder());

            return bidderOwnsContract && doublerIsOpponent;
        }

        /*
         * Everything remaining is a normal contract bid.
         */
        if (bid.getLevel() < 1 || bid.getLevel() > 7) {
            return false;
        }

        if (bid.getDenomination() == null) {
            return false;
        }

        Bid lastContractBid = this.getFinalBid();

        /*
         * Every new contract must be higher than
         * the current contract.
         */
        if (lastContractBid != null
                && !this.isHigherBid(
                        bid,
                        lastContractBid)) {
            return false;
        }

        return true;
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
        if (this.getFinalBid() == null) {
            return consecutivePasses >= 4;
        }
        return consecutivePasses >= 3;
    }

    public boolean isPassedOut() {
        return this.isComplete() && this.getFinalBid() == null;
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
     * Returns the most recent bid that was not a Pass.
     *
     * @return most recent non-Pass bid, or null
     */
    private Bid getLastNonPassBid() {
        for (int index = bids.size() - 1; index >= 0; index--) {

            Bid bid = bids.get(index);

            if (!bid.isPass()) {
                return bid;
            }
        }

        return null;
    }

    /**
     * Checks whether two players are on the same side.
     *
     * @param first  first player
     * @param second second player
     * @return true if they belong to the same partnership
     */
    private boolean areOnSameSide(Seat first, Seat second) {
        if (first == null || second == null) {
            return false;
        }

        return first.equals(second) || first.isPartner(second);
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
        if (newBid.getLevel() == currentBid.getLevel()) {
            return newBid.getDenomination().ordinal() > currentBid.getDenomination().ordinal();
        }
        return newBid.getLevel() > currentBid.getLevel();
    }

    /**
     * Creates a Contract object based on the final bid of the auction.
     *
     * @return a Contract object representing the final contract of the auction
     */
    public Contract createContract() {
        Bid finalBid = this.getFinalBid();
        Seat declarer = this.findDeclarer(finalBid);

        boolean doubled = false;
        boolean reDoubled = false;
        int indexOfFinalBid = bids.indexOf(finalBid);
        for (int i = indexOfFinalBid + 1; i < bids.size(); i++) {
            if (bids.get(i).isDouble()) {
                doubled = true;
            }
            if (bids.get(i).isRedouble()) {
                reDoubled = true;
                break;
            }
        }

        Contract contract = new Contract(finalBid.getLevel(), finalBid.getDenomination(), declarer, doubled,
                reDoubled);
        return contract;
    }

    /**
     * Finds the declarer for the fhe final contract bid in the auction.
     * The declarer is the first player from the winning partnership to bid the
     * denomination of the final contract.
     * 
     * @param finalBid the final contract bid of the auction
     * @return the Seat of the declarer
     */
    private Seat findDeclarer(Bid finalBid) {
        for (Bid bid : bids) {
            if (bid.getDenomination() == finalBid.getDenomination()
                    && (bid.getBidder().isPartner(finalBid.getBidder())
                            || bid.getBidder().equals(finalBid.getBidder()))) {
                return bid.getBidder();
            }
        }
        return finalBid.getBidder();
    }
}