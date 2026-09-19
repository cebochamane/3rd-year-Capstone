import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TutorialLearnerControlTest {
    static TutorialLesson lesson(Position declarer) {
        String order = switch (declarer) {
            case SOUTH -> "CSDH";
            case NORTH -> "DHCS";
            case EAST -> "HCSD";
            case WEST -> "SDHC";
        };
        StringBuilder text = new StringBuilder(TutorialTestSupport.HANDS)
                .append("D: ").append(declarer.name().charAt(0))
                .append("\nNONE VUL\n1NT;P;P;P.\n");
        for (char rank : "AK".toCharArray()) {
            for (int i = 0; i < 4; i++) {
                if (i > 0) text.append(',');
                text.append(rank).append(order.charAt(i));
            }
            text.append('\n');
        }
        return TutorialLessonParser.parse(text + "Claim\nNOTE: Review both hands.\n").get(0);
    }

    static boolean learnerAction(TutorialSession session, TutorialAction action) {
        return action.getType() == TutorialAction.Type.BID
                && action.getSeat().getPosition() == Position.SOUTH
                || action.getType() == TutorialAction.Type.CARD && session.controlsHand(action.getSeat());
    }

    @ParameterizedTest
    @CsvSource({"SOUTH,true", "SOUTH,false", "NORTH,true", "NORTH,false"})
    void learnerMustPlayBothHandsWithEitherPartnerDeclaring(Position declarer, boolean automatic) {
        TutorialLesson lesson = lesson(declarer);
        TutorialSession session = new TutorialSession(lesson, automatic, true);
        int northCards = 0, southCards = 0;
        for (TutorialAction action : lesson.getActions()) {
            if (action.getType() == TutorialAction.Type.CLAIM) break;
            if (learnerAction(session, action)) {
                assertTrue(session.isLearnerTurn());
                assertEquals(action.getSeat(), session.getExpectedSeat());
                assertFalse(session.advanceOneComputerAction(), "Computer must stop at either learner hand");
                if (action.getType() == TutorialAction.Type.BID) {
                    assertTrue(session.submitSouthBid(action.getBid()));
                } else {
                    assertTrue(session.getGameplay().isHandVisible(action.getSeat()), "Hand being played must be visible");
                    assertTrue(session.playLearnerCard(action.getSeat(), action.getCard()));
                    assertFalse(session.getGameplay().getRemainingHand(action.getSeat()).contains(action.getCard()));
                    if (action.getSeat().getPosition() == Position.NORTH) northCards++; else southCards++;
                }
            } else if (!automatic) {
                assertFalse(session.isLearnerTurn());
                assertTrue(session.advanceOneComputerAction());
            }
        }
        assertEquals(2, northCards); assertEquals(2, southCards);
        assertTrue(session.isAwaitingClaim());
        assertFalse(session.isLearnerTurn());
        assertFalse(session.canRequestHint());
        assertFalse(session.advanceOneComputerAction());
        assertEquals(2, session.getGameplay().getDeclarerTricks() + session.getGameplay().getDefenderTricks());
    }

    @ParameterizedTest @EnumSource(value = Position.class, names = {"EAST", "WEST"})
    void whenOpponentsDeclareNorthStillPlaysAutomatically(Position declarer) {
        TutorialLesson lesson = lesson(declarer);
        TutorialSession session = new TutorialSession(lesson, true, true);
        assertFalse(session.controlsHand(new Seat(Position.NORTH)));
        assertTrue(session.controlsHand(new Seat(Position.SOUTH)));
        for (TutorialAction action : lesson.getActions()) {
            if (!learnerAction(session, action)) continue;
            assertEquals(Position.SOUTH, session.getExpectedSeat().getPosition());
            if (action.getType() == TutorialAction.Type.BID) assertTrue(session.submitSouthBid(action.getBid()));
            else assertTrue(session.playSouthCard(action.getCard()));
        }
        assertTrue(session.isAwaitingClaim());
        assertEquals(2, session.getGameplay().getTricks().stream().filter(Trick::isComplete).count());
    }

    @Test void northDeclarerCanFinishAllThirteenTricksWithoutAnEndingKeyword() {
        StringBuilder text = new StringBuilder(TutorialTestSupport.HANDS)
                .append("D: N\nNONE VUL\n1S;P;2S;P;3S;P;P;P.\n");
        int trick = 0;
        for (char rank : "AKQJT98765432".toCharArray()) {
            String order = trick++ == 0 ? "DHCS" : "SDHC";
            for (int i = 0; i < 4; i++) {
                if (i > 0) text.append(',');
                text.append(rank).append(order.charAt(i));
            }
            text.append('\n');
        }
        TutorialLesson lesson = TutorialLessonParser.parse(text + "NOTE: Thirteen tricks.\n").get(0);
        TutorialSession session = TutorialTestSupport.finish(lesson);
        assertEquals(Position.NORTH, lesson.getContract().getDeclarer().getPosition());
        assertEquals(13, session.getScore().getDeclarerTricks());
        assertEquals(260, session.getScore().getScoreNS());
        assertFalse(session.canRequestHint());
    }

    @Test void biddingRemainsSouthOnlyEvenWhenNorthWillDeclare() {
        TutorialSession session = new TutorialSession(lesson(Position.NORTH), false, true);
        assertEquals(Position.NORTH, session.getExpectedSeat().getPosition());
        assertFalse(session.isLearnerTurn());
        assertFalse(session.submitSouthBid(new Bid(new Seat(Position.NORTH), 1, Suit.NT)));
        assertFalse(session.canRequestHint());
        assertTrue(session.advanceOneComputerAction());
        assertTrue(session.advanceOneComputerAction()); // East passes.
        assertEquals(Position.SOUTH, session.getExpectedSeat().getPosition());
        assertTrue(session.isLearnerTurn());
    }

    @Test void wrongHandCannotPlayOrUnlockAHint() {
        TutorialSession session = new TutorialSession(lesson(Position.SOUTH), true, true);
        assertTrue(session.submitSouthBid(new Bid(new Seat(Position.SOUTH), 1, Suit.NT)));
        assertEquals(Position.NORTH, session.getExpectedSeat().getPosition());
        int cards = session.getGameplay().getCurrentTrick().getPlays().size();
        assertFalse(session.playSouthCard(new Card(Suit.HEARTS, Rank.ACE)));
        assertFalse(session.playLearnerCard(new Seat(Position.EAST), new Card(Suit.DIAMONDS, Rank.ACE)));
        assertFalse(session.playLearnerCard(null, new Card(Suit.SPADES, Rank.ACE)));
        assertFalse(session.playLearnerCard(new Seat(Position.NORTH), null));
        assertFalse(session.canRequestHint());
        assertEquals(cards, session.getGameplay().getCurrentTrick().getPlays().size());
    }
    @Test void everyAcceptedLecturerLessonCanBePlayedWithTheNewControls() throws Exception {
        TutorialCatalog catalog = TutorialCatalog.load(java.nio.file.Path.of("tutorials"));
        int scripted = 0;
        for (TutorialEntry entry : catalog.getEntries()) {
            if (!entry.isScriptedTutorial()) continue;
            scripted++;
            TutorialSession session = new TutorialSession(entry.getLesson(), false, true);
            for (TutorialAction action : entry.getLesson().getActions()) {
                if (action.getType() == TutorialAction.Type.CLAIM || action.getType() == TutorialAction.Type.CONCEDE) break;
                if (learnerAction(session, action)) {
                    assertTrue(session.isLearnerTurn(), entry.toString());
                    assertFalse(session.advanceOneComputerAction());
                    assertTrue(action.getType() == TutorialAction.Type.BID
                            ? session.submitSouthBid(action.getBid())
                            : session.playLearnerCard(action.getSeat(), action.getCard()), entry.toString());
                } else assertTrue(session.advanceOneComputerAction(), entry.toString());
                assertFalse(session.canRequestHint());
            }
            assertTrue(session.isComplete() || session.isAwaitingEnding(), entry.toString());
        }
        assertTrue(scripted > 0, "The fixture catalog must contain scripted lessons");
    }

}
