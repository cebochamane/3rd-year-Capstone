import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TutorialSessionTest {

    @Test
    void playOnlyLessonAutomaticallyPlaysTheComputerCardsAndRequiresSouthsCard() {
        TutorialLesson lesson = TutorialLessonParser.parse(
                TutorialLessonParserTest.playOnlyLesson()).get(0);
        TutorialSession session = new TutorialSession(lesson);

        assertEquals(TutorialPhase.PLAYING, session.getPhase());
        assertEquals(3, session.getGameplay().getCurrentTrick().getPlays().size());
        assertFalse(session.playSouthCard(new Card(Suit.HEARTS, Rank.KING)));
        assertTrue(session.getFeedback().contains("Play ACE of HEARTS"));

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
        assertEquals(3, session.getGameplay().getCurrentTrick().getPlays().size());
        assertTrue(session.playSouthCard(new Card(Suit.HEARTS, Rank.ACE)));
        assertTrue(session.isComplete());
    }
}