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
    private TutorialTestSupport() {}

    static final String HANDS = """
            Cards held: spades;hearts;diamonds;clubs
            N: AKQJT98765432;-;-;-
            S: -;AKQJT98765432;-;-
            E: -;-;AKQJT98765432;-
            W: -;-;-;AKQJT98765432
            """;

    static TutorialLesson early(String ending, Integer result) {
        return TutorialLessonParser.parse(HANDS + "PLAY 3NT\nAC,AS,AD,AH\n"
                + ending + "\n" + (result == null ? "" : "RESULT: " + result + "\n")
                + "NOTE: Review the opening lead.\n").get(0);
    }

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

    static TutorialEntry entry(TutorialLesson lesson) {
        return TutorialEntry.scripted(Path.of("lesson.txt"), 2, lesson);
    }

    static void set(Object target, String name, Object value) throws Exception {
        Field field = GameView.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    static Object get(Object target, String name) throws Exception {
        Field field = GameView.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    @SuppressWarnings("unchecked")
    static List<LearningNote> notes(GameView view) throws Exception {
        return (List<LearningNote>) get(view, "learningNotes");
    }

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

    static <T> T onEdt(Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }
}
