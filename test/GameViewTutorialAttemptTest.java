import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests view logic without opening JFrame or JOptionPane windows. */
class GameViewTutorialAttemptTest {
    @TempDir Path directory;

    static class CapturingView extends GameView {
        String message;
        @Override public void showMessage(String message) { this.message = message; }
    }

    private CapturingView view(TutorialLesson lesson, boolean complete) throws Exception {
        CapturingView view = new CapturingView();
        TutorialTestSupport.set(view, "activeTutorial", TutorialTestSupport.entry(lesson));
        TutorialTestSupport.set(view, "tutorialSession", complete
                ? TutorialTestSupport.finish(lesson) : new TutorialSession(lesson, false));
        return view;
    }

    private String report(CapturingView view) throws Exception {
        return (String) TutorialTestSupport.invoke(view, "buildCompletedTutorialReport");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void fullReportPreservesHandsAllPlaysAuctionAndNotesWithoutChangingSession(boolean auction)
            throws Exception {
        TutorialLesson lesson = TutorialTestSupport.full(auction);
        CapturingView view = view(lesson, true);
        LearningNote note = new LearningNote(NotePhase.POST_PLAY);
        note.setTimestamp(LocalDateTime.of(2026, 1, 2, 12, 30));
        note.setNoteText("Declarer's plan: finesse ♠");
        note.setHandAnalysis("Count entries");
        note.setBiddingSequence("SOUTH 1 HEARTS");
        note.setLeadCard("ACE of CLUBS");
        TutorialTestSupport.notes(view).add(note);

        String report = report(view);
        assertTrue(report.contains("Source: lesson.txt\nLesson: 2"));
        assertTrue(report.contains("Original hands"));
        for (Position position : Position.values()) {
            assertTrue(report.contains(position + ": "
                    + lesson.getDeal().getHand(new Seat(position)).getCards()));
            assertEquals(13, lesson.getDeal().getHand(new Seat(position)).getCards().size());
        }
        assertEquals(52, report.lines().filter(line ->
                line.matches("  (NORTH|EAST|SOUTH|WEST): .+")).count());
        assertEquals(13, report.lines().filter(line -> line.startsWith("Trick ")).count());
        assertTrue(report.contains("  WEST: ACE of CLUBS\n  NORTH: ACE of SPADES\n"
                + "  EAST: ACE of DIAMONDS\n  SOUTH: ACE of HEARTS\n  Winner: SOUTH"));
        assertTrue(report.contains("Ending: Played out"));
        assertTrue(report.contains("North–South: 260"));
        assertTrue(report.contains("All thirteen tricks."));
        assertTrue(report.contains("2026-01-02T12:30"));
        assertTrue(report.contains("Declarer's plan: finesse ♠"));
        assertTrue(report.contains("Count entries"));
        assertTrue(report.contains("Opening lead: ACE of CLUBS"));
        if (auction) {
            assertTrue(report.contains("SOUTH 1 HEARTS\nWEST Pass\nNORTH Pass\nEAST Pass"),
                    "Include the implicit closing passes executed by the session");
        } else {
            assertTrue(report.contains("Play-only lesson: contract supplied."));
        }
        assertEquals(report, report(view));
        assertEquals(1, TutorialTestSupport.notes(view).size());
        TutorialSession session = (TutorialSession) TutorialTestSupport.get(view, "tutorialSession");
        assertTrue(session.isComplete());
        assertEquals(13, session.getGameplay().getDeclarerTricks());
    }

    @ParameterizedTest
    @CsvSource({"Claim,9,400,0", "Concede,6,0,150"})
    void earlyReportDistinguishesPlayedTricksFromFinalAllocation(
            String ending, int total, int ns, int ew) throws Exception {
        String report = report(view(TutorialTestSupport.early(ending, total), true));
        assertTrue(report.contains("Ending: " + ending));
        assertTrue(report.contains("recorded play — declarer: 0, defenders: 1"));
        assertTrue(report.contains("Explicit final RESULT: " + total));
        assertTrue(report.contains("North–South: " + ns));
        assertTrue(report.contains("East–West: " + ew));
        assertFalse(report.contains("Trick 2"), "Do not report the next empty trick as played");
        assertEquals(4, report.lines().filter(line ->
                line.matches("  (NORTH|EAST|SOUTH|WEST): .+")).count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Claim", "Concede"})
    void unknownResultRemainsUnknownInReport(String ending) throws Exception {
        String report = report(view(TutorialTestSupport.early(ending, null), true));
        assertTrue(report.contains("Score unavailable"));
        assertFalse(report.contains("North–South:"));
        assertFalse(report.contains("Explicit final RESULT:"));
        assertTrue(report.contains("No learner notes recorded."));
    }

    @Test
    void reportRequiresAnActiveCompletedTutorial() throws Exception {
        CapturingView empty = new CapturingView();
        assertThrows(IllegalStateException.class, () -> report(empty));
        CapturingView incomplete = view(TutorialTestSupport.early("Claim", 9), false);
        assertThrows(IllegalStateException.class, () -> report(incomplete));
        CapturingView missingEntry = view(TutorialTestSupport.early("Claim", 9), true);
        TutorialTestSupport.set(missingEntry, "activeTutorial", null);
        assertThrows(IllegalStateException.class, () -> report(missingEntry));
    }

    @Test
    void saveControlBookmarksIncompleteLessonsButPersistsCompletedReports() throws Exception {
        Storage storage = new Storage(directory.resolve("view.db").toString());
        User user = storage.createUser("Learner");
        GameController controller = new GameController();
        controller.setStorage(storage);
        controller.setCurrentUser(user);
        CapturingView view = view(TutorialTestSupport.early("Claim", 9), false);
        view.setController(controller);
        view.saveGameClicked();
        assertEquals(1, storage.getSavedTutorials(user).size());
        assertTrue(storage.getTutorialAttempts(user).isEmpty());
        assertTrue(view.message.contains("current progress is not saved"));

        TutorialTestSupport.set(view, "tutorialSession",
                TutorialTestSupport.finish(TutorialTestSupport.early("Claim", 9)));
        view.saveGameClicked();
        assertEquals(1, storage.getTutorialAttempts(user).size());
        String original = storage.getTutorialAttempts(user).get(0).reportText();
        assertTrue(original.contains("North–South: 400"));
        LearningNote note = new LearningNote(NotePhase.POST_PLAY);
        note.setNoteText("Added after the first save");
        TutorialTestSupport.notes(view).add(note);
        view.saveGameClicked();
        var snapshots = storage.getTutorialAttempts(user);
        assertEquals(2, snapshots.size());
        assertTrue(snapshots.get(0).reportText().contains(note.getNoteText()));
        assertEquals(original, snapshots.get(1).reportText());
        assertEquals(1, storage.getSavedTutorials(user).size());
    }

    @Test
    void resetClearsPreviousNotesTutorialSwapAndPendingTimer() throws Exception {
        TutorialTestSupport.onEdt(() -> {
            CapturingView view = view(TutorialTestSupport.early("Claim", 9), true);
            TutorialTestSupport.notes(view).add(new LearningNote());
            TutorialTestSupport.set(view, "tutorialLearningNote", new LearningNote());
            TutorialTestSupport.set(view, "tutorialCompletionShown", true);
            TutorialTestSupport.set(view, "waitingForTutorialNextTrick", true);
            TutorialTestSupport.set(view, "completedTutorialTrick", new Trick(1, new Seat(Position.WEST)));
            TutorialTestSupport.set(view, "swapMode", true);
            TutorialTestSupport.set(view, "firstSwapSeat", new Seat(Position.NORTH));
            TutorialTestSupport.set(view, "firstSwapCard", new Card(Suit.SPADES, Rank.ACE));
            TutorialTestSupport.set(view, "tutorialActionDelayActive", true);
            AtomicBoolean fired = new AtomicBoolean();
            Timer timer = new Timer(60_000, event -> fired.set(true));
            try {
                timer.start();
                TutorialTestSupport.set(view, "tutorialStepTimer", timer);
                TutorialTestSupport.invoke(view, "clearSessionViewState");
                assertTrue(TutorialTestSupport.notes(view).isEmpty());
                for (String name : new String[]{"tutorialSession", "activeTutorial", "tutorialLearningNote",
                        "completedTutorialTrick", "firstSwapSeat", "firstSwapCard", "tutorialStepTimer"}) {
                    assertNull(TutorialTestSupport.get(view, name), name);
                }
                for (String name : new String[]{"tutorialCompletionShown", "waitingForTutorialNextTrick",
                        "swapMode", "tutorialActionDelayActive"}) {
                    assertEquals(false, TutorialTestSupport.get(view, name), name);
                }
                assertFalse(timer.isRunning());
                assertFalse(fired.get());
            } finally {
                timer.stop();
            }
            return null;
        });
    }

    @Test
    void changingProfileClearsNotesButSelectingSameProfilePreservesThem() throws Exception {
        GameController controller = new GameController() {
            @Override public int syncTutorials() { return 0; }
        };
        User first = new User("one", "One");
        controller.setCurrentUser(first);
        controller.startNewGame();
        CapturingView view = view(TutorialTestSupport.early("Claim", 9), true);
        view.setController(controller);
        TutorialTestSupport.notes(view).add(new LearningNote());

        view.userSelected(new User("one", "One"));
        assertEquals(1, TutorialTestSupport.notes(view).size());
        assertNotNull(TutorialTestSupport.get(view, "tutorialSession"));
        assertEquals(GamePhase.BIDDING, controller.getPhase());

        view.userSelected(new User("two", "Two"));
        assertTrue(TutorialTestSupport.notes(view).isEmpty());
        assertNull(TutorialTestSupport.get(view, "tutorialSession"));
        assertEquals(GamePhase.MENU, controller.getPhase());
        assertEquals("two", controller.getCurrentUser().getUserId());
    }
}
