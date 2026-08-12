public class Contract {

    private int level;
    private Suit denomination;
    private Seat declarer;
    private boolean doubled;
    private boolean redoubled;

    /**
     * Creates a contract with the specified level, denomination,
     * declarer, and doubling status.
     *
     * @param level        the contract level (1 to 7)
     * @param denomination the suit or NT of the contract
     * @param declarer     the seat that is the declarer
     * @param doubled      whether the contract has been doubled
     * @param redoubled    whether the contract has been redoubled
     */
    public Contract(int level, Suit denomination, Seat declarer,
            boolean doubled, boolean redoubled) {
        // TODO: implement
    }

    /**
     * Checks whether this is a No Trump contract.
     *
     * @return true if the denomination is NT, otherwise false
     */
    public boolean isNoTrump() {
        // TODO: implement
        return false;
    }

    /**
     * Checks whether this contract has a trump suit.
     *
     * @return true if the contract has a suit denomination,
     *         otherwise false
     */
    public boolean isTrumpContract() {
        // TODO: implement
        return false;
    }

    /**
     * Calculates the number of tricks required to make the contract.
     *
     * @return the number of tricks required
     */
    public int getRequiredTricks() {
        // TODO: implement
        return 0;
    }

    /**
     * Checks whether the contract has been doubled.
     *
     * @return true if doubled, otherwise false
     */
    public boolean isDoubled() {
        // TODO: implement
        return false;
    }

    /**
     * Checks whether the contract has been redoubled.
     *
     * @return true if redoubled, otherwise false
     */
    public boolean isRedoubled() {
        // TODO: implement
        return false;
    }

    /**
     * Returns the declarer of the contract.
     *
     * @return the seat of the declarer
     */
    public Seat getDeclarer() {
        // TODO: implement
        return null;
    }

    /**
     * Returns the denomination of the contract.
     *
     * @return the contract's suit or NT
     */
    public Suit getDenomination() {
        // TODO: implement
        return null;
    }

    /**
     * Returns the level of the contract.
     *
     * @return the contract level
     */
    public int getLevel() {
        // TODO: implement
        return 0;
    }
}