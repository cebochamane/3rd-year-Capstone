import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a learning note made by a user during a bridge game.
 * Notes can be made during bidding, at the beginning of play, or after play.
 */
public class LearningNote {

    private LocalDateTime timestamp;
    private NotePhase phase;
    private String noteText;
    private String handAnalysis;
    private String contract;
    private int trickNumber;
    private String biddingSequence;
    private String leadCard;

    // Keywords for bridge strategy
    public static final String[] KEYWORDS = {
            "establish", "finesse", "duck", "ruff", "cross-ruff",
            "trump", "promote", "discard", "entry", "dummy",
            "declarer", "defender", "lead", "signal", "count"
    };

    /**
     * Creates a new learning note.
     */
    public LearningNote() {
        this.timestamp = LocalDateTime.now();
        this.phase = NotePhase.GENERAL;
        this.trickNumber = 0;
    }

    /**
     * Creates a learning note with specified phase.
     */
    public LearningNote(NotePhase phase) {
        this();
        this.phase = phase;
    }

    // ===== Getters and Setters =====

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public NotePhase getPhase() {
        return phase;
    }

    public void setPhase(NotePhase phase) {
        this.phase = phase;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getHandAnalysis() {
        return handAnalysis;
    }

    public void setHandAnalysis(String handAnalysis) {
        this.handAnalysis = handAnalysis;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public int getTrickNumber() {
        return trickNumber;
    }

    public void setTrickNumber(int trickNumber) {
        this.trickNumber = trickNumber;
    }

    public String getBiddingSequence() {
        return biddingSequence;
    }

    public void setBiddingSequence(String biddingSequence) {
        this.biddingSequence = biddingSequence;
    }

    public String getLeadCard() {
        return leadCard;
    }

    public void setLeadCard(String leadCard) {
        this.leadCard = leadCard;
    }

    /**
     * Returns a formatted string representation of the note.
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(timestamp.format(formatter));
        sb.append("] ");
        sb.append(phase);
        if (trickNumber > 0) {
            sb.append(" Trick ").append(trickNumber);
        }
        if (contract != null && !contract.isEmpty()) {
            sb.append(" Contract: ").append(contract);
        }
        if (handAnalysis != null && !handAnalysis.isEmpty()) {
            sb.append("\n  Analysis: ").append(handAnalysis);
        }
        if (noteText != null && !noteText.isEmpty()) {
            sb.append("\n  Note: ").append(noteText);
        }
        return sb.toString();
    }

    /**
     * Checks if the note contains any bridge keywords.
     * Only matches whole words, not partial matches.
     */
    public String[] getKeywordsUsed() {
        if (noteText == null || noteText.isEmpty())
            return new String[0];

        java.util.ArrayList<String> found = new java.util.ArrayList<>();
        String lowerText = noteText.toLowerCase();

        String[] words = lowerText.split("[^a-zA-Z-]+");

        for (String keyword : KEYWORDS) {
            for (String word : words) {
                if (word.equals(keyword)) {
                    found.add(keyword);
                    break;
                }
            }
        }
        return found.toArray(new String[0]);
    }
}