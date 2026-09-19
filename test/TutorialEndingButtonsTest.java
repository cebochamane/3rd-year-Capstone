import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TutorialEndingButtonsTest {
    private TutorialSession pending(TutorialLesson lesson) {
        TutorialSession session = new TutorialSession(lesson, false, true);
        for (TutorialAction action : lesson.getActions()) {
            if (action.getType() == TutorialAction.Type.CLAIM || action.getType() == TutorialAction.Type.CONCEDE) break;
            if (action.getSeat().getPosition() == Position.SOUTH) {
                assertTrue(action.getType() == TutorialAction.Type.BID
                        ? session.submitSouthBid(action.getBid()) : session.playSouthCard(action.getCard()));
            } else assertTrue(session.advanceOneComputerAction());
        }
        return session;
    }

    @Test
    void lecturerFirstLessonWaitsForClaimThenScoresElevenTricks() throws Exception {
        TutorialLesson lesson = TutorialCatalog.load(Path.of("tutorials"))
                .findScripted("exampleBidAndPlayV3.txt", 1).getLesson();
        TutorialSession session = pending(lesson);
        assertTrue(session.isAwaitingClaim());
        assertFalse(session.isComplete());
        assertFalse(session.advanceOneComputerAction());
        assertNull(session.getScore());
        assertEquals(8, session.getGameplay().getDeclarerTricks());
        DdsSolver.Analysis analysis = session.getGameplay().analyzeRemainingTricks();
        assertEquals(3, analysis.declarerTricks());
        assertFalse(session.confirmEnding(true, analysis), "Wrong button must not end the lesson");
        assertTrue(session.confirmEnding(false, analysis));
        assertEquals(11, session.getScore().getDeclarerTricks());
        assertEquals(100, session.getScore().getScoreEW());
        assertTrue(session.isClaimed());
        assertFalse(session.confirmEnding(false, analysis));
        assertEquals(36, session.getGameplay().getTricks().stream().mapToInt(t -> t.getPlays().size()).sum());
    }

    @Test
    void concessionWaitsForCorrectButtonAndSurrendersRemainingTricks() {
        TutorialSession session = pending(TutorialTestSupport.early("Concede", null));
        assertTrue(session.isAwaitingConcede());
        assertFalse(session.advanceOneComputerAction());
        DdsSolver.Analysis analysis = session.getGameplay().analyzeRemainingTricks();
        assertFalse(session.confirmEnding(false, analysis));
        assertTrue(session.confirmEnding(true, analysis));
        assertTrue(session.isComplete());
        assertTrue(session.isConceded());
        assertEquals(0, session.getScore().getDeclarerTricks());
        assertEquals(13, session.getScore().getDefenderTricks());
    }

    @Test
    void impossibleLecturerResultIsRejectedWithoutFinishing() {
        TutorialSession session = pending(TutorialTestSupport.early("Claim", 9));
        DdsSolver.Analysis analysis = session.getGameplay().analyzeRemainingTricks();
        assertEquals(0, analysis.declarerTricks(), "West can run all clubs in this NT fixture");
        assertThrows(IllegalStateException.class, () -> session.confirmEnding(false, analysis));
        assertTrue(session.isAwaitingClaim());
        assertFalse(session.getGameplay().isFinished());
        assertEquals(0, session.getGameplay().getDeclarerTricks());
    }

    @Test
    void compatibleResultCanVoluntarilyAwardFewerThanTheMaximum() {
        TutorialLesson lesson = TutorialLessonParser.parse(TutorialTestSupport.HANDS
                + "PLAY 1H\nAC,AS,AD,AH\nClaim\nRESULT: 10\nNOTE: A partial claim.\n").get(0);
        TutorialSession session = pending(lesson);
        DdsSolver.Analysis analysis = session.getGameplay().analyzeRemainingTricks();
        assertEquals(12, analysis.declarerTricks());
        assertEquals(9, session.proposedEndingTricks(analysis));
        assertTrue(session.confirmEnding(false, analysis));
        assertEquals(10, session.getScore().getDeclarerTricks());
        assertEquals(3, session.getScore().getDefenderTricks());
    }

    @Test
    void naturalCompletionStillNeedsNoEndingButton() {
        TutorialSession session = pending(TutorialTestSupport.full(false));
        assertTrue(session.isComplete());
        assertFalse(session.isAwaitingEnding());
        assertEquals(260, session.getScore().getScoreNS());
    }
}
