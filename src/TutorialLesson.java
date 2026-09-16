import java.util.ArrayList;
import java.util.List;

/** An immutable, validated tutorial lesson. */
public final class TutorialLesson {

    private final TutorialLessonType type;
    private final Deal deal;
    private final Contract contract;
    private final List<TutorialAction> actions;
    private final String note;

    public TutorialLesson(TutorialLessonType type, Deal deal, Contract contract,
            List<TutorialAction> actions, String note) {
        if (type == null || deal == null || contract == null || actions == null) {
            throw new IllegalArgumentException("A tutorial lesson needs its type, deal, contract and actions.");
        }
        
        this.type = type;
        this.deal = deal;
        this.contract = contract;
        this.actions = List.copyOf(new ArrayList<>(actions));
        this.note = note == null ? "" : note.trim();
    }

    public TutorialLessonType getType() {
        return type;
    }

    public Deal getDeal() {
        return deal;
    }

    public Contract getContract() {
        return contract;
    }

    public List<TutorialAction> getActions() {
        return actions;
    }

    public String getNote() {
        return note;
    }
}