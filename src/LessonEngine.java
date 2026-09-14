import java.util.List;

/**
 * Runs a Lesson: tracks the step, auto-plays computer cards,
 * and enforces that the user plays the correct card.
 */
public class LessonEngine {

    public enum Result {
        CORRECT,
        WRONG,
        FINISHED
    }

    private final Lesson lesson;
    private int step;
    private boolean finished;
    private final List<Card> playedCards;

    public LessonEngine(Lesson lesson) {
        this.lesson = lesson;
        this.step = 0;
        this.finished = false;
        this.playedCards = null; // TODO
    }

    public Lesson getLesson() {
        return lesson;
    }

    public int getStep() {
        return step;
    }

    public boolean isFinished() {
        return finished;
    }

    /** Whose turn is it right now? */
    public Seat currentPlayer() {
        // TODO
        return null;
    }

    /** Is it the human user's turn? */
    public boolean isUserTurn() {
        // TODO
        return false;
    }

    /** The card the engine expects next. */
    public Card expectedCard() {
        // TODO
        return null;
    }

    /** Auto-plays the current step if it belongs to a computer player. */
    public Card autoPlay() {
        // TODO
        return null;
    }

    /** Called when the user clicks a card. */
    public Result checkUserCard(Card card) {
        // TODO: if correct, advance; if wrong, do NOT advance
        return null;
    }

    /** The learning note to display when the lesson ends. */
    public String getLearningNote() {
        // TODO
        return null;
    }

    private boolean checkFinished() {
        // TODO
        return false;
    }

    private static boolean sameCard(Card a, Card b) {
        // TODO
        return false;
    }
}