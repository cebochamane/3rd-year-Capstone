import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TutorialSessionTest {

    @Test
    void playOnlyLessonWaitsForBothNorthAndSouthCards() {
        TutorialLesson lesson = TutorialLessonParser.parse(
                TutorialLessonParserTest.playOnlyLesson()).get(0);
        TutorialSession session = new TutorialSession(lesson);

        assertEquals(TutorialPhase.PLAYING, session.getPhase());
        assertEquals(1, session.getGameplay().getCurrentTrick().getPlays().size());
        assertEquals(Position.NORTH, session.getExpectedSeat().getPosition());
        assertFalse(session.advanceOneComputerAction());
        assertTrue(session.playLearnerCard(new Seat(Position.NORTH), new Card(Suit.SPADES, Rank.ACE)));
        assertEquals(3, session.getGameplay().getCurrentTrick().getPlays().size());
        assertFalse(session.playSouthCard(new Card(Suit.HEARTS, Rank.KING)));
        assertFalse(session.getFeedback().contains("ACE"));
        assertTrue(session.canRequestHint());
        assertTrue(session.getHint().contains("Play ACE of HEARTS"));

        assertTrue(session.playSouthCard(new Card(Suit.HEARTS, Rank.ACE)));
        assertTrue(session.isComplete());
        assertTrue(session.isClaimed());
        assertFalse(session.isConceded());
    }

    @Test
    void bidAndPlayLessonRejectsTheWrongBidThenProgressesToSouthsCard() {
        TutorialLesson lesson = TutorialLessonParser.parse(
                TutorialLessonParserTest.bidAndPlayLesson()).get(0);
        TutorialSession session = new TutorialSession(lesson);

        assertEquals(TutorialPhase.BIDDING, session.getPhase());
        assertFalse(session.submitSouthBid(new Bid(new Seat(Position.SOUTH), 1, Suit.HEARTS)));
        assertTrue(session.submitSouthBid(new Bid(new Seat(Position.SOUTH), 1, Suit.NT)));

        assertEquals(TutorialPhase.PLAYING, session.getPhase());
        assertEquals(4, session.getAuction().getBids().size());
        assertEquals(1, session.getGameplay().getCurrentTrick().getPlays().size());
        assertEquals(Position.NORTH, session.getExpectedSeat().getPosition());
        assertFalse(session.advanceOneComputerAction());
        assertTrue(session.playLearnerCard(new Seat(Position.NORTH), new Card(Suit.SPADES, Rank.ACE)));
        assertEquals(3, session.getGameplay().getCurrentTrick().getPlays().size());
        assertTrue(session.playSouthCard(new Card(Suit.HEARTS, Rank.ACE)));
        assertTrue(session.isComplete());
    }

    @Test
    void omittedClosingPassesAreAddedWithoutRequiringSouthToEnterThem() {
        String abbreviatedAuction = TutorialLessonParserTest.bidAndPlayLesson()
                .replace("1NT;P;P;P.", "1NT.");
        TutorialLesson lesson = TutorialLessonParser.parse(abbreviatedAuction).get(0);
        TutorialSession session = new TutorialSession(lesson);

        assertTrue(session.submitSouthBid(new Bid(new Seat(Position.SOUTH), 1, Suit.NT)));
        assertEquals(TutorialPhase.PLAYING, session.getPhase());
        assertTrue(session.getAuction().isComplete());
        assertEquals(4, session.getAuction().getBids().size());
        assertTrue(session.getAuction().getBids().get(1).isPass());
        assertTrue(session.getAuction().getBids().get(2).isPass());
        assertTrue(session.getAuction().getBids().get(3).isPass());
    }
}
