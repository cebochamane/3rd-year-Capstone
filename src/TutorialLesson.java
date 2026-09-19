import java.util.ArrayList;
import java.util.List;

/** An immutable, validated tutorial lesson. */
public final class TutorialLesson {

    private final TutorialLessonType type;
    private final Deal deal;
    private final Contract contract;
    private final List<TutorialAction> actions;
    private final String note;
    private final Integer finalDeclarerTricks;

    /**
     * Constructs an immutable TutorialLesson with the specified type, deal, contract, actions, and note, validating that required parameters are non-null.
     * 
     * @param type the type of the tutorial lesson
     * @param deal the associated deal for the lesson
     * @param contract the associated contract for the lesson
     * @param actions the list of tutorial actions included in the lesson
     * @param note an optional note attached to the lesson, trimmed of whitespace
     */
    public TutorialLesson(TutorialLessonType type, Deal deal, Contract contract,
            List<TutorialAction> actions, String note) {
        this(type, deal, contract, actions, note, null);
    }

    /**
     * Constructs an immutable TutorialLesson with the specified type, deal, contract, actions, note,
     * and optional final declarer trick count, validating parameters and their ranges.
     * 
     * @param type the type of the tutorial lesson
     * @param deal the associated deal for the lesson
     * @param contract the associated contract for the lesson
     * @param actions the list of tutorial actions included in the lesson
     * @param note an optional note attached to the lesson, trimmed of whitespace
     * @param finalDeclarerTricks the optional explicit final declarer-side trick total, must be between 0 and 13 if not null
     */
    public TutorialLesson(TutorialLessonType type, Deal deal, Contract contract,
            List<TutorialAction> actions, String note, Integer finalDeclarerTricks) {

        if (type == null || deal == null
                || contract == null || actions == null) {
            throw new IllegalArgumentException(
                    "A tutorial lesson needs its type, deal, contract and actions.");
        }

        if (finalDeclarerTricks != null && (finalDeclarerTricks < 0 || finalDeclarerTricks > 13)) {
            throw new IllegalArgumentException("The final declarer trick total must be between 0 and 13.");
        }

        this.type = type;
        this.deal = deal;
        this.contract = contract;
        this.actions = List.copyOf(new ArrayList<>(actions));
        this.note = note == null ? "" : note.trim();
        this.finalDeclarerTricks = finalDeclarerTricks;
    }

    /**
     * Returns the type of this tutorial lesson.
     * 
     * @return the tutorial lesson type
     */
    public TutorialLessonType getType() {
        return type;
    }

    /**
     * Returns the deal associated with this tutorial lesson.
     * 
     * @return the deal instance
     */
    public Deal getDeal() {
        return deal;
    }

    /**
     * Returns the contract associated with this tutorial lesson.
     * 
     * @return the contract instance
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Returns an unmodifiable list of tutorial actions for this lesson.
     * 
     * @return the list of tutorial actions
     */
    public List<TutorialAction> getActions() {
        return actions;
    }

    /**
     * Returns the note associated with this tutorial lesson, trimmed of leading and trailing whitespace.
     * 
     * @return the note string, possibly empty if none was provided
     */
    public String getNote() {
        return note;
    }

    /**
     * Returns the explicit final declarer-side trick total, or null if the lesson does not supply one.
     * 
     * @return the final declarer trick count or null
     */
    public Integer getFinalDeclarerTricks() {
        return finalDeclarerTricks;
    }
}