import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TutorialHintTest {
    @ParameterizedTest
    @CsvSource(value = {
        "S|1NT;P;P;P.|Bid 1 NT.",
        "N|1NT;P;P;P.|Pass.",
        "N|P;1NT;DBL;P;P;P.|Double.",
        "N|1NT;DBL;RDBL;P;P;P.|Redouble."
    }, delimiter = '|')
    void bidHintsRequireAnErrorAndNeverAdvanceTheAuction(String dealer, String auction, String hint) {
        TutorialLesson lesson = TutorialLessonParser.parse(TutorialTestSupport.HANDS
                + "D: " + dealer + "\nNONE VUL\n" + auction + "\nClaim\nNOTE: Bidding practice.\n").get(0);
        TutorialSession session = new TutorialSession(lesson, true, true);
        assertTrue(session.isLearnerTurn());
        assertFalse(session.canRequestHint());
        assertNotEquals(hint, session.getHint());
        int bids = session.getAuction().getBids().size();
        assertFalse(session.submitSouthBid(new Bid(new Seat(Position.SOUTH), 7, Suit.NT)));
        assertEquals("That is not the lesson bid. Try again, or use Hint.", session.getFeedback());
        assertTrue(session.canRequestHint());
        assertEquals(hint, session.getHint());
        assertEquals(hint, session.getHint()); // Repeated viewing is harmless.
        assertEquals(bids, session.getAuction().getBids().size());
        Bid expected = lesson.getActions().stream().filter(a -> a.getType() == TutorialAction.Type.BID
                && a.getSeat().getPosition() == Position.SOUTH).findFirst().orElseThrow().getBid();
        assertTrue(session.submitSouthBid(expected));
        assertFalse(session.canRequestHint());
        assertTrue(session.isAwaitingClaim());
        assertNotEquals(hint, session.getHint());
    }

    @ParameterizedTest
    @CsvSource({"SOUTH,NORTH", "SOUTH,SOUTH", "NORTH,NORTH", "NORTH,SOUTH"})
    void cardHintNamesTheCorrectHandButOnlyAfterAMistake(Position declarer, Position hand) {
        TutorialLesson lesson = TutorialLearnerControlTest.lesson(declarer);
        TutorialSession session = new TutorialSession(lesson, true, true);
        TutorialAction expected = advanceToHand(session, hand);
        assertTrue(session.getLearnerPrompt().contains(hand == Position.NORTH ? "North" : "South"));
        assertTrue(session.getLearnerPrompt().contains(hand == declarer ? "declarer" : "dummy"));
        assertFalse(session.canRequestHint());
        String before = DdsSolver.encode(session.getGameplay());
        assertFalse(session.playLearnerCard(expected.getSeat(), new Card(expected.getCard().getSuit(), Rank.KING)));
        assertEquals("That is not the lesson card. Try again, or use Hint.", session.getFeedback());
        assertFalse(session.getFeedback().contains("ACE"));
        assertFalse(session.getFeedback().contains(expected.getCard().getSuit().name()));
        assertTrue(session.canRequestHint());
        assertEquals("Play " + expected.getCard() + " from " + hand + "'s hand.", session.getHint());
        assertEquals(before, DdsSolver.encode(session.getGameplay()));
        assertTrue(session.playLearnerCard(expected.getSeat(), expected.getCard()));
        assertFalse(session.canRequestHint(), "The next action needs its own mistake to unlock a hint");
        assertFalse(session.getHint().contains("Play ACE"));
    }

    static TutorialAction advanceToHand(TutorialSession session, Position hand) {
        for (TutorialAction action : session.getLesson().getActions()) {
            if (!TutorialLearnerControlTest.learnerAction(session, action)) continue;
            if (action.getType() == TutorialAction.Type.CARD && action.getSeat().getPosition() == hand) return action;
            if (action.getType() == TutorialAction.Type.BID) assertTrue(session.submitSouthBid(action.getBid()));
            else assertTrue(session.playLearnerCard(action.getSeat(), action.getCard()));
        }
        throw new AssertionError("Target hand was not found");
    }

    @Test void hintsDoNotCarryIntoAnotherSession() {
        TutorialSession first = new TutorialSession(TutorialLearnerControlTest.lesson(Position.SOUTH), true, true);
        assertFalse(first.submitSouthBid(new Bid(new Seat(Position.SOUTH), 7, Suit.NT)));
        assertTrue(first.canRequestHint());
        TutorialSession second = new TutorialSession(first.getLesson(), true, true);
        assertFalse(second.canRequestHint());
        assertNotEquals(first.getHint(), second.getHint());
    }

    private static final class RecordingView extends GameView {
        String message;
        @Override public void showMessage(String message) { this.message = message; }
    }

    @Test void viewRoutesNorthMistakesAndOnlyRevealsHintsOnRequest() throws Exception {
        TutorialTestSupport.onEdt(() -> {
            TutorialSession session = new TutorialSession(TutorialLearnerControlTest.lesson(Position.SOUTH), true, true);
            advanceToHand(session, Position.NORTH);
            RecordingView view = new RecordingView(); view.setController(new GameController());
            TutorialTestSupport.set(view, "tutorialSession", session);
            assertEquals(true, TutorialTestSupport.invoke(view, "isLearnerTutorialTurn"));
            view.tutorialHintClicked(); assertNull(view.message);
            view.cardClicked(new Seat(Position.NORTH), new Card(Suit.SPADES, Rank.KING));
            assertEquals("That is not the lesson card. Try again, or use Hint.", view.message);
            view.tutorialHintClicked(); assertEquals("Play ACE of SPADES from NORTH's hand.", view.message);
            for (String pause : new String[]{"waitingForTutorialNextTrick", "tutorialActionDelayActive", "settlementAnalysisActive"}) {
                view.message = "unchanged";
                TutorialTestSupport.set(view, pause, true);
                assertEquals(false, TutorialTestSupport.invoke(view, "canUseTutorialHint"));
                view.tutorialHintClicked(); assertEquals("unchanged", view.message);
                TutorialTestSupport.set(view, pause, false);
            }
            TutorialTestSupport.invoke(view, "clearSessionViewState");
            assertEquals(false, TutorialTestSupport.invoke(view, "canUseTutorialHint"));
            view.message = "unchanged"; view.tutorialHintClicked(); assertEquals("unchanged", view.message);
            return null;
        });
    }

    @Test void viewBidRejectionDoesNotGiveAwayAnswer() throws Exception {
        TutorialTestSupport.onEdt(() -> {
            TutorialSession session = new TutorialSession(TutorialLearnerControlTest.lesson(Position.SOUTH), true, true);
            RecordingView view = new RecordingView();view.setController(new GameController());
            TutorialTestSupport.set(view, "tutorialSession", session);
            view.bidClicked(new Bid(new Seat(Position.SOUTH), 7, Suit.NT));
            assertEquals("That is not the lesson bid. Try again, or use Hint.", view.message);
            view.tutorialHintClicked(); assertEquals("Bid 1 NT.", view.message);
            return null;
        });
    }
    @Test void pausedPromptsPointToNextTrickOrShowResultInsteadOfAnUnplayableHand() throws Exception {
        TutorialTestSupport.onEdt(() -> {
            RecordingView view = new RecordingView(); view.setController(new GameController());
            TutorialSession session = new TutorialSession(TutorialLearnerControlTest.lesson(Position.SOUTH), true, true);
            advanceToHand(session, Position.NORTH);
            TutorialTestSupport.set(view, "tutorialSession", session);
            assertEquals("Play North's hand (dummy).", TutorialTestSupport.invoke(view, "tutorialStatusText"));
            TutorialTestSupport.set(view, "tutorialActionDelayActive", true);
            assertEquals("Please wait for the next tutorial action.", TutorialTestSupport.invoke(view, "tutorialStatusText"));
            TutorialTestSupport.set(view, "tutorialActionDelayActive", false);
            TutorialTestSupport.set(view, "waitingForTutorialNextTrick", true);
            TutorialTestSupport.set(view, "completedTutorialTrick", new Trick(1, new Seat(Position.WEST)));
            assertEquals("Review the trick, then click Next Trick.", TutorialTestSupport.invoke(view, "tutorialStatusText"));
            TutorialTestSupport.set(view, "completedTutorialTrick", new Trick(13, new Seat(Position.WEST)));
            assertEquals("Review the trick, then click Show Result.", TutorialTestSupport.invoke(view, "tutorialStatusText"));
            return null;
        });
    }

}
