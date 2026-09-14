import java.util.List;
import java.util.Map;

/**
 * Represents one Bridge lesson: a predefined hand plus the correct
 * sequence of cards to play.
 */
public class Lesson {

    private Map<Position, List<Card>> hands;
    private Seat dealer;
    private Vulnerability vulnerability;
    private String contract;
    private List<Card> playSequence;
    private List<Seat> playOrder;
    private String endAction;
    private String learningNote;

    public Lesson() {
    }

    public Map<Position, List<Card>> getHands() {
        return hands;
    }

    public void setHands(Map<Position, List<Card>> hands) {
        this.hands = hands;
    }

    public Seat getDealer() {
        return dealer;
    }

    public void setDealer(Seat dealer) {
        this.dealer = dealer;
    }

    public Vulnerability getVulnerability() {
        return vulnerability;
    }

    public void setVulnerability(Vulnerability vulnerability) {
        this.vulnerability = vulnerability;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public List<Card> getPlaySequence() {
        return playSequence;
    }

    public void setPlaySequence(List<Card> playSequence) {
        this.playSequence = playSequence;
    }

    public List<Seat> getPlayOrder() {
        return playOrder;
    }

    public void setPlayOrder(List<Seat> playOrder) {
        this.playOrder = playOrder;
    }

    public String getEndAction() {
        return endAction;
    }

    public void setEndAction(String endAction) {
        this.endAction = endAction;
    }

    public String getLearningNote() {
        return learningNote;
    }

    public void setLearningNote(String learningNote) {
        this.learningNote = learningNote;
    }
}