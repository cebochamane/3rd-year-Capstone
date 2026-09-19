import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TutorialScoringTest {

    private static final String NOTE = "Review the final result.";

    /*
     * Each player holds an entire suit.
     *
     * This makes ownership, legal plays and trick winners
     * predictable without using the lecturer's unfinished files.
     */
    private static final String HANDS = """
            Cards held: spades;hearts;diamonds;clubs
            N: AKQJT98765432;-;-;-
            S: -;AKQJT98765432;-;-
            E: -;-;AKQJT98765432;-
            W: -;-;-;AKQJT98765432
            """;

    /*
     * HEARTS:
     *   South wins all tricks; East plays the final card.
     *
     * SPADES:
     *   North, South's partner, wins all tricks;
     *   West plays the final card.
     *
     * NT:
     *   West wins all tricks; South plays the final card.
     *
     * Test both automatic sessions and the stepped mode
     * used by the tutorial GUI.
     */
    @ParameterizedTest
    @CsvSource({
        "HEARTS, true,  13, 260, 0",
        "HEARTS, false, 13, 260, 0",
        "SPADES, true,  13, 260, 0",
        "SPADES, false, 13, 260, 0",
        "NT,     true,   0,   0, 350",
        "NT,     false,  0,   0, 350"
    })
    void fullPlayOnlyLessonScoresWithoutAnEndingMarker(
            Suit strain,
            boolean automatic,
            int expectedDeclarerTricks,
            int expectedNS,
            int expectedEW) {

        TutorialLesson lesson = parse(
                fullLesson(strain, false, "", ""));

        assertEquals(
                52,
                lesson.getActions().stream()
                        .filter(action ->
                                action.getType() == TutorialAction.Type.CARD)
                        .count());

        TutorialSession session = finish(lesson, automatic);

        assertTrue(session.isComplete());
        assertTrue(session.getGameplay().isFinished());
        assertFalse(session.isClaimed());
        assertFalse(session.isConceded());
        assertEquals(NOTE, session.getFeedback());

        assertEquals(
                expectedDeclarerTricks,
                session.getGameplay().getDeclarerTricks());

        assertEquals(
                13 - expectedDeclarerTricks,
                session.getGameplay().getDefenderTricks());

        assertEquals(
                13,
                session.getGameplay().getTricks().size());

        assertTrue(
                session.getGameplay().getTricks().stream()
                        .allMatch(Trick::isComplete));

        for (Position position : Position.values()) {
            assertTrue(
                    session.getGameplay()
                            .getRemainingHand(new Seat(position))
                            .getCards()
                            .isEmpty());
        }

        assertScore(
                session,
                expectedDeclarerTricks,
                expectedNS,
                expectedEW);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void fullBidAndPlayLessonScoresWithoutAnEndingMarker(
            boolean automatic) {

        TutorialLesson lesson = parse(
                fullLesson(Suit.HEARTS, true, "", ""));

        assertEquals(TutorialLessonType.BID_AND_PLAY, lesson.getType());
        assertEquals(
                Position.SOUTH,
                lesson.getContract().getDeclarer().getPosition());

        TutorialSession session = finish(lesson, automatic);

        assertTrue(session.getAuction().isComplete());
        assertTrue(session.isComplete());
        assertFalse(session.isClaimed());
        assertFalse(session.isConceded());
        assertEquals(NOTE, session.getFeedback());

        assertScore(session, 13, 260, 0);
    }

    /*
     * One trick is played before the ending marker.
     * In 3NT, West wins that trick.
     *
     * RESULT: 9 means the declaring side's final total is nine,
     * not nine additional tricks.
     */
    @ParameterizedTest
    @CsvSource({
        "Claim,   true,  9, 400, 0",
        "Claim,   false, 9, 400, 0",
        "Concede, true,  6,   0, 150",
        "Concede, false, 6,   0, 150"
    })
    void earlyEndingUsesTheExplicitFinalResult(
            String marker,
            boolean automatic,
            int finalTricks,
            int expectedNS,
            int expectedEW) {

        TutorialLesson lesson = parse(
                earlyLesson(
                        "3NT",
                        "",
                        marker,
                        Integer.toString(finalTricks)));

        TutorialSession session = finish(lesson, automatic);

        assertTrue(session.isComplete());

        // The script stopped early; it did not play all 52 cards.
        assertFalse(session.getGameplay().isFinished());
        assertEquals(0, session.getGameplay().getDeclarerTricks());
        assertEquals(1, session.getGameplay().getDefenderTricks());

        assertEquals(
                marker.equals("Claim"),
                session.isClaimed());

        assertEquals(
                marker.equals("Concede"),
                session.isConceded());

        assertScore(session, finalTricks, expectedNS, expectedEW);
    }

    @ParameterizedTest
    @CsvSource({
        "Claim, true",
        "Claim, false",
        "Concede, true",
        "Concede, false"
    })
    void earlyEndingWithoutAResultCompletesWithoutInventingAScore(
            String marker,
            boolean automatic) {

        TutorialLesson lesson = parse(
                earlyLesson("3NT", "", marker, null));

        TutorialSession session = finish(lesson, automatic);

        assertTrue(session.isComplete());
        assertFalse(session.getGameplay().isFinished());
        assertEquals(NOTE, session.getFeedback());
        assertNull(session.getScore());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void resultMetadataDoesNotMakeAnUnfinishedLessonScoreable(
            boolean automatic) {

        TutorialLesson lesson = parse(
                earlyLesson("3NT", "", "Claim", "9"));

        TutorialSession session = new TutorialSession(lesson, automatic);

        // South has not supplied its scripted card yet.
        assertFalse(session.isComplete());
        assertNull(session.getScore());
    }

    @Test
    void rejectsAResultBelowTheDeclarerTricksAlreadyWon() {
        /*
         * In hearts, South wins the first trick.
         * A final total of zero is therefore impossible.
         */
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parse(earlyLesson("1H", "", "Claim", "0")));

        assertTrue(exception.getMessage().contains("RESULT"));
        assertTrue(exception.getMessage().contains("contradicts"));
    }

    @Test
    void rejectsAResultThatTakesAwayATrickAlreadyWonByDefenders() {
        /*
         * In no trump, West wins the first trick.
         * Declarer can therefore finish with at most 12 tricks.
         */
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parse(earlyLesson("3NT", "", "Claim", "13")));

        assertTrue(exception.getMessage().contains("RESULT"));
        assertTrue(exception.getMessage().contains("contradicts"));
    }

    @Test
    void rejectsAResultThatDisagreesWithACompletedHand() {
        /*
         * South wins all 13 tricks in the hearts fixture.
         */
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parse(
                        fullLesson(
                                Suit.HEARTS,
                                false,
                                "",
                                "RESULT: 12\n")));

        assertTrue(exception.getMessage().contains("RESULT"));
        assertTrue(exception.getMessage().contains("contradicts"));
    }

    @Test
    void acceptsAResultThatAgreesWithACompletedHand() {
        TutorialLesson lesson = parse(
                fullLesson(
                        Suit.HEARTS,
                        false,
                        "",
                        "RESULT: 13\n"));

        TutorialSession session = finish(lesson, false);

        assertScore(session, 13, 260, 0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "14", "nine", "9.5", ""})
    void rejectsInvalidResultValues(String result) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parse(
                        earlyLesson("3NT", "", "Claim", result)));

        assertTrue(exception.getMessage().contains("RESULT"));
    }

    /*
     * South is declarer.
     * NS and BOTH make South vulnerable; EW does not.
     */
    @ParameterizedTest
    @CsvSource({
        "NONE VUL, NONE, false, 400",
        "NS VUL,   NS,   true,  600",
        "EW VUL,   EW,   false, 400",
        "BOTH VUL, ALL,  true,  600"
    })
    void playOnlyVulnerabilityReachesTheScoringCalculation(
            String vulnerabilityLine,
            Vulnerability expectedVulnerability,
            boolean expectedVulnerable,
            int expectedPoints) {

        TutorialLesson lesson = parse(
                earlyLesson(
                        "3NT",
                        vulnerabilityLine,
                        "Claim",
                        "9"));

        assertEquals(
                expectedVulnerability,
                lesson.getDeal().getVulnerability());

        TutorialSession session = finish(lesson, false);
        Score score = session.getScore();

        assertNotNull(score);
        assertEquals(expectedVulnerable, score.isDeclarerVulnerable());
        assertScore(session, 9, expectedPoints, 0);
    }

    @Test
    void omittedPlayOnlyVulnerabilityDefaultsToNonVulnerable() {
        TutorialLesson lesson = parse(
                earlyLesson("3NT", "", "Claim", "9"));

        assertEquals(
                Vulnerability.NONE,
                lesson.getDeal().getVulnerability());

        TutorialSession session = finish(lesson, false);

        assertNotNull(session.getScore());
        assertFalse(session.getScore().isDeclarerVulnerable());
        assertScore(session, 9, 400, 0);
    }

    @Test
    void rejectsAnUnknownPlayOnlyVulnerability() {
        assertThrows(
                IllegalArgumentException.class,
                () -> parse(
                        earlyLesson(
                                "3NT",
                                "UNKNOWN VUL",
                                "Claim",
                                "9")));
    }

    private TutorialLesson parse(String text) {
        var lessons = TutorialLessonParser.parse(text);
        assertEquals(1, lessons.size());
        return lessons.get(0);
    }

    /**
     * Supplies South's scripted actions.
     *
     * In automatic mode, the session executes the other seats itself.
     * In stepped mode, this helper advances them one action at a time,
     * as the GUI's timer does.
     */
    private TutorialSession finish(
            TutorialLesson lesson,
            boolean automatic) {

        TutorialSession session = new TutorialSession(lesson, automatic);

        for (TutorialAction action : lesson.getActions()) {
            boolean south = action.getSeat() != null
                    && action.getSeat().getPosition() == Position.SOUTH;

            if (south) {
                if (action.getType() == TutorialAction.Type.BID) {
                    assertTrue(
                            session.submitSouthBid(action.getBid()),
                            session.getFeedback());
                } else {
                    assertEquals(
                            TutorialAction.Type.CARD,
                            action.getType());

                    assertTrue(
                            session.playSouthCard(action.getCard()),
                            session.getFeedback());
                }
            } else if (!automatic) {
                assertTrue(
                        session.advanceOneComputerAction(),
                        "Expected the next scripted computer action to advance.");
            }
        }

        assertTrue(session.isComplete());
        return session;
    }

    private void assertScore(
            TutorialSession session,
            int declarerTricks,
            int expectedNS,
            int expectedEW) {

        Score score = session.getScore();

        assertNotNull(score);
        assertEquals(declarerTricks, score.getDeclarerTricks());
        assertEquals(13 - declarerTricks, score.getDefenderTricks());
        assertEquals(expectedNS, score.getScoreNS());
        assertEquals(expectedEW, score.getScoreEW());
    }

    /**
     * Produces a complete legal 13-trick script.
     *
     * The first trick always starts with West, because South
     * is declarer. Subsequent tricks begin with the known winner.
     */
    private String fullLesson(
            Suit strain,
            boolean bidAndPlay,
            String vulnerabilityLine,
            String ending) {

        String strainCode = switch (strain) {
            case HEARTS -> "H";
            case SPADES -> "S";
            case NT -> "NT";
            default -> throw new IllegalArgumentException(
                    "This fixture supports hearts, spades and NT.");
        };

        StringBuilder text = new StringBuilder(HANDS);

        if (bidAndPlay) {
            text.append("D: S\n")
                    .append(vulnerabilityLine.isBlank()
                            ? "NONE VUL" : vulnerabilityLine)
                    .append("\n1").append(strainCode)
                    .append(";P;P;P.\n");
        } else {
            text.append("PLAY 1").append(strainCode).append("\n");

            if (!vulnerabilityLine.isBlank()) {
                text.append(vulnerabilityLine).append("\n");
            }
        }

        char[] ranks = "AKQJT98765432".toCharArray();

        for (int index = 0; index < ranks.length; index++) {
            char rank = ranks[index];

            String suitOrder;
            if (index == 0 || strain == Suit.NT) {
                suitOrder = "CSDH"; // West, North, East, South
            } else if (strain == Suit.HEARTS) {
                suitOrder = "HCSD"; // South, West, North, East
            } else {
                suitOrder = "SDHC"; // North, East, South, West
            }

            for (int card = 0; card < suitOrder.length(); card++) {
                if (card > 0) {
                    text.append(",");
                }

                text.append(rank).append(suitOrder.charAt(card));
            }

            text.append("\n");
        }

        text.append(ending);
        text.append("NOTE: ").append(NOTE).append("\n");

        return text.toString();
    }

    /**
     * Produces a lesson that stops after one complete trick.
     */
    private String earlyLesson(
            String contract,
            String vulnerabilityLine,
            String marker,
            String result) {

        StringBuilder text = new StringBuilder(HANDS);

        text.append("PLAY ").append(contract).append("\n");

        if (!vulnerabilityLine.isBlank()) {
            text.append(vulnerabilityLine).append("\n");
        }

        text.append("AC,AS,AD,AH\n")
                .append(marker).append("\n");

        if (result != null) {
            text.append("RESULT: ").append(result).append("\n");
        }

        text.append("NOTE: ").append(NOTE).append("\n");

        return text.toString();
    }
}