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

    /**
     * Creates a new TutorialAction with the specified type, seat, bid, and card.
     * 
     * @param type the type of this action
     * @param seat the seat associated with this action
     * @param bid the bid associated with this action
     * @param card the card associated with this action
     */
    private TutorialAction(Type type, Seat seat, Bid bid, Card card) {
        this.type = type;
        this.seat = seat;
        this.bid = bid;
        this.card = card;
    }

    /**
     * Creates a TutorialAction representing a bid made by a player.
     * 
     * @param bid the bid to associate with the action
     * @return a TutorialAction of type BID for the given bid
     */
    public static TutorialAction bid(Bid bid) {
        return new TutorialAction(Type.BID, bid.getBidder(), bid, null);
    }

    /**
     * Creates a TutorialAction representing a card play by a player at a specific seat.
     * 
     * @param seat the seat of the player playing the card
     * @param card the card being played
     * @return a TutorialAction of type CARD for the given seat and card
     */
    public static TutorialAction card(Seat seat, Card card) {
        return new TutorialAction(Type.CARD, seat, null, card);
    }

    /**
     * Creates a TutorialAction representing a claim action in the tutorial.
     * 
     * @return a TutorialAction of type CLAIM
     */
    public static TutorialAction claim() {
        return new TutorialAction(Type.CLAIM, null, null, null);
    }

    /**
     * Creates a TutorialAction representing a concede action in the tutorial.
     * 
     * @return a TutorialAction of type CONCEDE
     */
    public static TutorialAction concede() {
        return new TutorialAction(Type.CONCEDE, null, null, null);
    }

    /**
     * Returns the type of this tutorial action.
     * 
     * @return the type of this action
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the seat associated with this action, if any.
     * 
     * @return the seat associated with this action, or null if none
     */
    public Seat getSeat() {
        return seat;
    }

    /**
     * Returns the bid associated with this action, if any.
     * 
     * @return the bid associated with this action, or null if none
     */
    public Bid getBid() {
        return bid;
    }

    /**
     * Returns the card associated with this action, if any.
     * 
     * @return the card associated with this action, or null if none
     */
    public Card getCard() {
        return card;
    }
}