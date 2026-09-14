/**
 * Drives a Lesson: loads it, advances through it,
 * and calls back into the view to display state.
 */
public class LessonController {

    private LessonEngine engine;
    private GameView view;

    public LessonController(GameView view) {
        this.view = view;
    }

    /** Loads a lesson from a file and starts it. */
    public void startLesson(String filePath) throws Exception {
        // TODO: load lesson, create engine, show state, auto-play computer turns
    }

    /** Called when the user clicks a card. */
    public void onUserCard(Card card) {
        // TODO: check with engine, handle CORRECT/WRONG/FINISHED
    }

    /** Keeps auto-playing while it's a computer player's turn. */
    private void advanceComputerTurns() {
        // TODO
    }
}