public class Score {

    private Contract contract;
    private int declarerTricks;
    private int defenderTricks;
    private int scoreNS;
    private int scoreEW;

    /**
     * Creates a score for a completed contract.
     *
     * @param contract       the contract that was played
     * @param declarerTricks the number of tricks won by the declarer side
     */
    public Score(Contract contract, int declarerTricks) {
        // TODO: implement
    }

    /**
     * Calculates the final scores for both partnerships.
     *
     * The calculation should use the contract, the number of tricks
     * won by the declarer side, and the number of tricks won by
     * the defenders.
     */
    public void calculate() {
        // TODO: implement
    }

    /**
     * Returns the score for the North-South partnership.
     *
     * @return the North-South score
     */
    public int getScoreNS() {
        // TODO: implement
        return 0;
    }

    /**
     * Returns the score for the East-West partnership.
     *
     * @return the East-West score
     */
    public int getScoreEW() {
        // TODO: implement
        return 0;
    }
}