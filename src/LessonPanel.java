import javax.swing.*;

/**
 * Displays the current state of a lesson and the learning note at the end.
 */
public class LessonPanel extends JPanel {

    private JLabel turnLabel;
    private JLabel stepLabel;
    private JTextArea noteArea;

    public LessonPanel() {
        // TODO: build UI
    }

    /** Refreshes the panel from the engine's current state. */
    public void updateState(LessonEngine engine) {
        // TODO
    }

    /** Shows the learning note after the lesson ends. */
    public void showLearningNote(String note) {
        // TODO
    }
}