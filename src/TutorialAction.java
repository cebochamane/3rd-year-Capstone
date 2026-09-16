/** One scripted action in a tutorial lesson. */
public final class TutorialAction {

    public enum Type {
        BID,
        CARD,
        CLAIM,
        CONCEDE
    }

    private final Type type;
    private final Seat seat;
    private final Bid bid;
    private final Card card;

    private TutorialAction(Type type, Seat seat, Bid bid, Card card) {
        this.type = type;
        this.seat = seat;
        this.bid = bid;
        this.card = card;
    }

    public static TutorialAction bid(Bid bid) {
        return new TutorialAction(Type.BID, bid.getBidder(), bid, null);
    }

    public static TutorialAction card(Seat seat, Card card) {
        return new TutorialAction(Type.CARD, seat, null, card);
    }

    public static TutorialAction claim() {
        return new TutorialAction(Type.CLAIM, null, null, null);
    }

    public static TutorialAction concede() {
        return new TutorialAction(Type.CONCEDE, null, null, null);
    }

    public Type getType() {
        return type;
    }

    public Seat getSeat() {
        return seat;
    }

    public Bid getBid() {
        return bid;
    }

    public Card getCard() {
        return card;
    }
}