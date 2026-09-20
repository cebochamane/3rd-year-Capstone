import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javax.swing.SwingUtilities;

/** Deterministic lessons and headless access to the existing private view helpers. */
final class TutorialTestSupport {
    /**
     * Constructs a TutorialTestSupport instance; private to prevent instantiation.
     */
    private TutorialTestSupport() {}

    static final String HANDS = """
            Cards held: spades;hearts;diamonds;clubs
            N: AKQJT98765432;-;-;-
            S: -;AKQJT98765432;-;-
            E: -;-;AKQJT98765432;-
            W: -;-;-;AKQJT98765432
            """;

    /**
     * Creates an early tutorial lesson parsed from a predefined hand and specified ending and result.
     * 
     * @param ending The ending play sequence for the lesson.
     * @param result The integer result to include in the lesson, or null if none.
     * @return A TutorialLesson object representing the parsed early lesson.
     */
    static TutorialLesson early(String ending, Integer result) {
        return TutorialLessonParser.parse(HANDS + "PLAY 3NT\nAC,AS,AD,AH\n"
                + ending + "\n" + (result == null ? "" : "RESULT: " + result + "\n")
                + "NOTE: Review the opening lead.\n").get(0);
    }

    /**
     * Generates a full tutorial lesson with all thirteen tricks, optionally including an auction phase.
     * 
     * @param auction Flag indicating whether to include an auction phase (true) or only play phase (false).
     * @return A TutorialLesson object representing the parsed full lesson.
     */
    static TutorialLesson full(boolean auction) {
        StringBuilder text = new StringBuilder(HANDS);
        text.append(auction ? "D: S\nNONE VUL\n1H.\n" : "PLAY 1H\n");
        int index = 0;
        for (char rank : "AKQJT98765432".toCharArray()) {
            String order = index++ == 0 ? "CSDH" : "HCSD";
            for (int card = 0; card < 4; card++) {
                if (card > 0) text.append(',');
                text.append(rank).append(order.charAt(card));
            }
            text.append('\n');
        }
        return TutorialLessonParser.parse(text + "NOTE: All thirteen tricks.\n").get(0);
    }

    /**
     * Completes a tutorial session by simulating all lesson actions, asserting proper progress and completion.
     * 
     * @param lesson The TutorialLesson to complete.
     * @return A completed TutorialSession for the specified lesson.
     */
    static TutorialSession finish(TutorialLesson lesson) {
        TutorialSession session = new TutorialSession(lesson, false);
        for (TutorialAction action : lesson.getActions()) {
            if (action.getType() == TutorialAction.Type.BID && action.getSeat().getPosition() == Position.SOUTH
                    || action.getType() == TutorialAction.Type.CARD && session.controlsHand(action.getSeat())) {
                assertTrue(action.getType() == TutorialAction.Type.BID
                        ? session.submitSouthBid(action.getBid())
                        : session.playLearnerCard(action.getSeat(), action.getCard()), session.getFeedback());
            } else {
                assertTrue(session.advanceOneComputerAction());
            }
        }
        assertTrue(session.isComplete());
        return session;
    }

    /**
     * Creates a scripted TutorialEntry for the given lesson with default parameters.
     * 
     * @param lesson The TutorialLesson to use in the entry.
     * @return A TutorialEntry object scripted from the lesson.
     */
    static TutorialEntry entry(TutorialLesson lesson) {
        return TutorialEntry.scripted(Path.of("lesson.txt"), 2, lesson);
    }

    /**
     * Sets the value of a private field named by 'name' on the given target object using reflection.
     * 
     * @param target The object whose private field will be set.
     * @param name The name of the private field to modify.
     * @param value The new value to assign to the field.
     * @throws Exception If reflection access or field modification fails.
     */
    static void set(Object target, String name, Object value) throws Exception {
        Field field = GameView.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * Gets the value of a private field named by 'name' from the given target object using reflection.
     * 
     * @param target The object from which to retrieve the private field value.
     * @param name The name of the private field to access.
     * @return The value of the specified private field.
     * @throws Exception If reflection access or field retrieval fails.
     */
    static Object get(Object target, String name) throws Exception {
        Field field = GameView.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    @SuppressWarnings("unchecked")
    /**
     * Retrieves the list of learning notes from the specified GameView's private field.
     * 
     * @param view The GameView instance from which to get learning notes.
     * @return A list of LearningNote objects associated with the view.
     * @throws Exception If reflection access or retrieval fails.
     */
    static List<LearningNote> notes(GameView view) throws Exception {
        return (List<LearningNote>) get(view, "learningNotes");
    }

    /**
     * Invokes a private no-argument method by name on the given GameView instance and returns its result.
     * 
     * @param view The GameView instance on which to invoke the method.
     * @param name The name of the private method to invoke.
     * @return The result returned by the invoked method.
     * @throws Exception If reflection access, method invocation fails, or the invoked method throws an exception.
     */
    static Object invoke(GameView view, String name) throws Exception {
        Method method = GameView.class.getDeclaredMethod(name);
        method.setAccessible(true);
        try {
            return method.invoke(view);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof Exception cause) throw cause;
            if (exception.getCause() instanceof Error cause) throw cause;
            throw exception;
        }
    }

    /**
     * Executes the specified Callable on the Swing Event Dispatch Thread and returns its result.
     * 
     * @param work The Callable task to execute on the EDT.
     * @return The result produced by the Callable task.
     * @throws Exception If execution on the EDT or task completion fails.
     */
    static <T> T onEdt(Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
