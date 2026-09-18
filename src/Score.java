public final class Score {

    private final Contract contract;
    private final int declarerTricks;
    private final Vulnerability vulnerability;
    private final boolean declarerNS;
    private final boolean declarerVulnerable;

    private int trickScore;
    private int overtrickScore;
    private int gameOrPartScoreBonus;
    private int slamBonus;
    private int insultBonus;
    private int undertrickPenalty;

    private int scoreNS;
    private int scoreEW;

    public Score(Contract contract, int declarerTricks, Vulnerability vulnerability) {
        if (contract == null) {
            throw new IllegalArgumentException("A contract is required.");
        }

        if (contract.getLevel() < 1 || contract.getLevel() > 7) {
            throw new IllegalArgumentException("The contract level must be between 1 and 7.");
        }

        if (contract.getDenomination() == null) {
            throw new IllegalArgumentException("The contract must have a denomination.");
        }

        if (contract.getDeclarer() == null || contract.getDeclarer().getPosition() == null) {
                    throw new IllegalArgumentException("The contract must have a declarer.");
        }

        if (declarerTricks < 0 || declarerTricks > 13) {
            throw new IllegalArgumentException("Declarer-side tricks must be between 0 and 13.");
        }

        if (vulnerability == null) {
            throw new IllegalArgumentException("Vulnerability is required.");
        }

        this.contract = contract;
        this.declarerTricks = declarerTricks;
        this.vulnerability = vulnerability;

        Position declarer = contract.getDeclarer().getPosition();

        this.declarerNS = declarer == Position.NORTH || declarer == Position.SOUTH;

        this.declarerVulnerable = vulnerability == Vulnerability.ALL
                || (declarerNS && vulnerability == Vulnerability.NS)
                || (!declarerNS && vulnerability == Vulnerability.EW);

        calculate();
    }

    /**
     * Creates a score only when gameplay has a complete final result.
     * This also supports an accepted normal-game claim.
     */
    public static Score fromCompletedGame(Deal deal, Gameplay gameplay) {
        if (deal == null || gameplay == null) {
            throw new IllegalArgumentException("A deal and gameplay are required.");
        }

        if (!gameplay.isFinished()) {
            throw new IllegalStateException("The game must finish before it can be scored.");
        }

        int declarer = gameplay.getDeclarerTricks();
        int defenders = gameplay.getDefenderTricks();

        if (declarer < 0 || declarer > 13 || defenders < 0 || defenders > 13 || declarer + defenders != 13) {
            throw new IllegalStateException("The final partnership trick counts must total 13.");
        }

        return new Score(gameplay.getContract(), declarer, deal.getVulnerability());
    }

    /**
     * Recalculates from the original inputs.
     * Repeated calls do not accumulate points.
     */
    public void calculate() {
        trickScore = 0;
        overtrickScore = 0;
        gameOrPartScoreBonus = 0;
        slamBonus = 0;
        insultBonus = 0;
        undertrickPenalty = 0;
        scoreNS = 0;
        scoreEW = 0;

        // An auction can mark a redoubled contract as both doubled
        // and redoubled. Redouble therefore takes precedence.
        int multiplier = contract.isRedoubled()
                ? 4 : contract.isDoubled() ? 2 : 1;

        int difference = declarerTricks - contract.getRequiredTricks();

        if (difference >= 0) {
            int level = contract.getLevel();
            Suit strain = contract.getDenomination();

            int baseTrickScore = switch (strain) {
                case CLUBS, DIAMONDS -> 20 * level;
                case HEARTS, SPADES -> 30 * level;
                case NT -> 40 + 30 * (level - 1);
            };

            trickScore = baseTrickScore * multiplier;

            gameOrPartScoreBonus = trickScore >= 100
                    ? (declarerVulnerable ? 500 : 300) : 50;

            if (level == 6) {
                slamBonus = declarerVulnerable ? 750 : 500;
            } else if (level == 7) {
                slamBonus = declarerVulnerable ? 1500 : 1000;
            }

            if (multiplier == 1) {
                int overtrickValue =strain == Suit.CLUBS || strain == Suit.DIAMONDS
                    ? 20 : 30;

                overtrickScore = difference * overtrickValue;
            } else {
                int doubledOvertrickValue = declarerVulnerable ? 200 : 100;

                overtrickScore = difference * doubledOvertrickValue * (multiplier == 4 ? 2 : 1);

                insultBonus = multiplier == 4 ? 100 : 50;
            }

            award(declarerNS, trickScore + overtrickScore + gameOrPartScoreBonus + slamBonus + insultBonus);

        } else {
            int undertricks = -difference;

            if (multiplier == 1) {
                undertrickPenalty = undertricks * (declarerVulnerable ? 100 : 50);

            } else {
                int doubledPenalty;

                if (declarerVulnerable) {
                    doubledPenalty = 200 + 300 * (undertricks - 1);
                } else {
                    doubledPenalty = 100 + 200 * Math.min(undertricks - 1, 2) + 300 * Math.max(undertricks - 3, 0);
                }

                undertrickPenalty = doubledPenalty * (multiplier == 4 ? 2 : 1);
            }

            award(!declarerNS, undertrickPenalty);
        }
    }

    private void award(boolean northSouth, int points) {
        if (northSouth) {
            scoreNS = points;
        } else {
            scoreEW = points;
        }
    }

    public Contract getContract() {
        return contract;
    }

    public Vulnerability getVulnerability() {
        return vulnerability;
    }

    public boolean isDeclarerVulnerable() {
        return declarerVulnerable;
    }

    public boolean isMade() {
        return declarerTricks >= contract.getRequiredTricks();
    }

    public int getDeclarerTricks() {
        return declarerTricks;
    }

    public int getDefenderTricks() {
        return 13 - declarerTricks;
    }

    public int getTrickScore() {
        return trickScore;
    }

    public int getOvertrickScore() {
        return overtrickScore;
    }

    public int getGameOrPartScoreBonus() {
        return gameOrPartScoreBonus;
    }

    public int getSlamBonus() {
        return slamBonus;
    }

    public int getInsultBonus() {
        return insultBonus;
    }

    public int getUndertrickPenalty() {
        return undertrickPenalty;
    }

    public int getScoreNS() {
        return scoreNS;
    }

    public int getScoreEW() {
        return scoreEW;
    }
}