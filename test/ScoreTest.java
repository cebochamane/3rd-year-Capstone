import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScoreTest {

    private Contract contract(
            int level, Suit suit, Position declarer, int multiplier) {

        return new Contract(level, suit, new Seat(declarer), multiplier > 1, multiplier == 4);
    }

    @ParameterizedTest
    @CsvSource({
        // level, strain, tricks, multiplier, vulnerability, awarded points
        "1, CLUBS,    7, 1, NONE, 70",
        "2, DIAMONDS, 9, 1, NONE, 110",
        "2, SPADES,  10, 1, NONE, 170",
        "1, NT,       8, 1, NONE, 120",
        "3, NT,       9, 1, NONE, 400",
        "3, NT,       9, 1, NS,   600",
        "4, HEARTS,  10, 1, NONE, 420",
        "4, HEARTS,  10, 1, NS,   620",
        "5, CLUBS,   11, 1, NONE, 400",
        "6, NT,      12, 1, NONE, 990",
        "6, NT,      12, 1, NS,   1440",
        "7, NT,      13, 1, NONE, 1520",
        "7, NT,      13, 1, NS,   2220",
        "1, CLUBS,    7, 2, NONE, 140",
        "2, HEARTS,   8, 2, NONE, 470",
        "2, HEARTS,   9, 2, NONE, 570",
        "2, HEARTS,   9, 2, NS,   870",
        "1, CLUBS,    7, 4, NONE, 230",
        "2, HEARTS,   8, 4, NONE, 640",
        "2, HEARTS,   9, 4, NONE, 840",
        "2, HEARTS,   9, 4, NS,   1240"
    })
    void scoresMadeContracts(int level, Suit suit, int tricks, int multiplier, 
        Vulnerability vulnerability, int expected) {

        Score score = new Score(contract(level, suit, Position.NORTH, multiplier), tricks, vulnerability);

        assertTrue(score.isMade());
        assertEquals(expected, score.getScoreNS());
        assertEquals(0, score.getScoreEW());

        score.calculate();
        assertEquals(expected, score.getScoreNS());
    }

    @Test
    void scoresUndertricksAtBothVulnerabilities() {
        int[] down = {1, 2, 3, 4, 5};
        int[] doubledNotVulnerable = {100, 300, 500, 800, 1100};
        int[] doubledVulnerable = {200, 500, 800, 1100, 1400};

        for (int i = 0; i < down.length; i++) {
            for (int multiplier : new int[] {1, 2, 4}) {
                for (boolean vulnerable : new boolean[] {false, true}) {
                    int expected;

                    if (multiplier == 1) {
                        expected = down[i] * (vulnerable ? 100 : 50);
                    } else {
                        expected = vulnerable
                                ? doubledVulnerable[i]
                                : doubledNotVulnerable[i];

                        if (multiplier == 4) {
                            expected *= 2;
                        }
                    }

                    Score score = new Score(
                            contract(4, Suit.SPADES, Position.SOUTH, multiplier),
                            10 - down[i],
                            vulnerable ? Vulnerability.NS : Vulnerability.NONE);

                    assertFalse(score.isMade());
                    assertEquals(expected, score.getUndertrickPenalty());
                    assertEquals(0, score.getScoreNS());
                    assertEquals(expected, score.getScoreEW());
                }
            }
        }
    }

    @Test
    void usesTheDeclarersPartnershipForVulnerabilityAndAwards() {
        for (Position position : Position.values()) {
            boolean ns = position == Position.NORTH || position == Position.SOUTH;

            for (Vulnerability vulnerability : Vulnerability.values()) {
                boolean vulnerable = vulnerability == Vulnerability.ALL
                    || (ns && vulnerability == Vulnerability.NS)
                    || (!ns && vulnerability == Vulnerability.EW);

                Contract contract = contract(4, Suit.HEARTS, position, 1);

                Score made = new Score(contract, 10, vulnerability);
                int madePoints = vulnerable ? 620 : 420;

                assertEquals(ns ? madePoints : 0, made.getScoreNS());
                assertEquals(ns ? 0 : madePoints, made.getScoreEW());

                Score down = new Score(contract, 9, vulnerability);
                int penalty = vulnerable ? 100 : 50;

                assertEquals(ns ? 0 : penalty, down.getScoreNS());
                assertEquals(ns ? penalty : 0, down.getScoreEW());
            }
        }
    }

    @Test
    void overtricksDoNotTurnAPartScoreIntoGameOrAwardAnUnbidSlam() {
        Score score = new Score(contract(2, Suit.SPADES, Position.NORTH, 1), 
            12, Vulnerability.NONE);

        assertEquals(60, score.getTrickScore());
        assertEquals(120, score.getOvertrickScore());
        assertEquals(50, score.getGameOrPartScoreBonus());
        assertEquals(0, score.getSlamBonus());
        assertEquals(230, score.getScoreNS());
    }

    @Test
    void rejectsInvalidInputs() {
        Contract valid = contract(3, Suit.NT, Position.NORTH, 1);

        assertThrows(IllegalArgumentException.class,
            () -> new Score(valid, -1, Vulnerability.NONE));
        assertThrows(IllegalArgumentException.class,
            () -> new Score(valid, 14, Vulnerability.NONE));
        assertThrows(IllegalArgumentException.class,
            () -> new Score(null, 9, Vulnerability.NONE));
        assertThrows(IllegalArgumentException.class,
            () -> new Score(valid, 9, null));
        assertThrows(IllegalArgumentException.class,
            () -> new Score(contract(0, Suit.NT, Position.NORTH, 1),
                9, Vulnerability.NONE));
        assertThrows(IllegalArgumentException.class,
            () -> new Score(contract(8, Suit.NT, Position.NORTH, 1),
                9, Vulnerability.NONE));
        assertThrows(IllegalArgumentException.class,
            () -> new Score(new Contract(1, null, new Seat(Position.NORTH),
                false, false), 7, Vulnerability.NONE));
        assertThrows(IllegalArgumentException.class,
            () -> new Score(new Contract(1, Suit.NT, null, false,
                false), 7, Vulnerability.NONE));
    }

    @Test
    void doesNotScoreUnfinishedGameplay() {
        Deal deal = new Deal(new Seat(Position.NORTH), Vulnerability.NONE);
        deal.dealCards();

        Gameplay gameplay = new Gameplay(deal, contract(1, Suit.NT, Position.NORTH, 1));

        assertThrows(IllegalStateException.class,
            () -> Score.fromCompletedGame(deal, gameplay));
    }
}