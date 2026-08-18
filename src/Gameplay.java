import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Gameplay {

    private Contract contract;
    private Seat currentLeader;
    private ArrayList<Trick> tricks;
    private int trickNo;
    private int declarerTricks;
    private int defenderTricks;
    private boolean finished;
    private Map<Position, Hand> remainingHands;

    /**
     * Creates a new gameplay session for the given contract.
     *
     * @param contract the contract being played
     */
    public Gameplay(Deal deal, Contract contract) {
        this.contract = contract;
        this.remainingHands = new HashMap<>();

        for (Position position : Position.values()) {
            Hand originalHand = deal.getHand(new Seat(position));
            Hand copiedHand = new Hand(new ArrayList<>(originalHand.getCards()));
            remainingHands.put(position, copiedHand);
        }
    }

    /**
     * Returns the contract being played.
     *
     * @return the current contract
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Returns the current leader of the trick.
     *
     * @return the seat that leads the current trick
     */
    public Seat getCurrentLeader() {
        return currentLeader;
    }

    /**
     * Returns all tricks played so far.
     *
     * @return the list of completed and current tricks
     */
    public ArrayList<Trick> getTricks() {
        return tricks;
    }

    /**
     * Returns the number of the current trick.
     *
     * @return the current trick number
     */
    public int getTrickNo() {
        return trickNo;
    }

    /**
     * Attempts to play a card for a player.
     *
     * The card must belong to the player's hand and must follow
     * the rules of the current trick.
     *
     * @param seat the player playing the card
     * @param card the card being played
     * @return true if the card was legally played, otherwise false
     */
    public boolean playCard(Seat seat, Card card) {
        // TODO: implement
        return false;
    }

    /**
     * Determines which cards a player is legally allowed to play.
     *
     * This is where the follow-suit rule should be enforced.
     *
     * @param seat the player whose legal cards are required
     * @param hand the player's hand
     * @return a list of cards that can legally be played
     */
    public ArrayList<Card> getLegalCards(Seat seat, Hand hand) {
        // TODO: implement
        return null;
    }

    /**
     * Checks whether all 13 tricks have been completed.
     *
     * @return true if the game is finished, otherwise false
     */
    public boolean isFinished() {
        // TODO: implement
        return false;
    }

    /**
     * Determines which seat won the current trick.
     *
     * The winner is determined using the lead suit and the contract's
     * trump suit, with NT having no trump suit.
     *
     * @return the seat that won the trick
     */
    public Seat getWinner() {
        // TODO: implement
        return null;
    }

    /**
     * Returns the number of tricks won by the declarer side.
     *
     * @return the number of declarer tricks
     */
    public int getDeclarerTricks() {
        return declarerTricks;
    }

    /**
     * Returns the number of tricks won by the defenders.
     *
     * @return the number of defender tricks
     */
    public int getDefenderTricks() {
        return defenderTricks;
    }
}