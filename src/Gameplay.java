import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Controls the card-playing part of a Bridge game.
 */
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
     * Creates a new gameplay session.
     *
     * @param deal     the deal being played
     * @param contract the contract being played
     */
    public Gameplay(Deal deal, Contract contract) {

        this.contract = contract;

        /*
         * The player immediately clockwise from the declarer
         * makes the opening lead.
         */
        this.currentLeader = contract.getDeclarer().next();
        this.tricks = new ArrayList<>();
        this.trickNo = 1;
        this.declarerTricks = 0;
        this.defenderTricks = 0;
        this.finished = false;
        this.remainingHands = new HashMap<>();

        /*
         * Copy all four hands.
         */
        for (Position position : Position.values()) {
            Hand originalHand = deal.getHand(new Seat(position));
            Hand copiedHand = new Hand(new ArrayList<>(originalHand.getCards()));
            remainingHands.put(position, copiedHand);
        }

        /*
         * Create the first trick.
         */
        tricks.add(new Trick(trickNo, currentLeader));
    }

    /**
     * Returns the contract.
     *
     * @return contract
     */
    public Contract getContract() {
        return contract;
    }

    /**
     * Returns the current leader.
     *
     * @return current leader
     */
    public Seat getCurrentLeader() {
        return currentLeader;
    }

    /**
     * Returns all tricks.
     *
     * @return tricks
     */
    public ArrayList<Trick> getTricks() {
        return tricks;
    }

    /**
     * Returns the current trick.
     *
     * @return current trick
     */
    public Trick getCurrentTrick() {
        if (tricks.isEmpty()) {
            return null;
        }

        return tricks.get(tricks.size() - 1);
    }

    /**
     * Returns the current trick number.
     *
     * @return trick number
     */
    public int getTrickNo() {
        return trickNo;
    }

    /**
     * Plays a card.
     *
     * @param seat player playing the card
     * @param card card being played
     * @return true if successful
     */
    public boolean playCard(Seat seat, Card card) {
        if (finished || seat == null || card == null) {
            return false;
        }

        Hand hand = remainingHands.get(seat.getPosition());
        if (hand == null || !hand.contains(card)) {
            return false;
        }
        Trick currentTrick = getCurrentTrick();

        if (currentTrick == null) {
            return false;
        }

        /*
         * Work out whose turn it is.
         */
        Seat expectedSeat = currentTrick.getLeader();
        for (int i = 0; i < currentTrick.getPlays().size(); i++) {
            expectedSeat = expectedSeat.next();
        }

        /*
         * Make sure the correct player is playing.
         */
        if (!seat.equals(expectedSeat)) {
            return false;
        }

        /*
         * Find the legal cards.
         */
        ArrayList<Card> legalCards = getLegalCards(seat, hand);
        if (!legalCards.contains(card)) {
            return false;
        }

        /*
         * Add the card to the trick.
         */
        currentTrick.addPlay(seat, card);
        /*
         * Remove the card from the player's hand.
         */
        hand.removeCard(card);
        /*
         * If the trick is complete,
         * determine the winner.
         */
        if (currentTrick.isComplete()) {
            Seat winner = currentTrick.getWinner(contract.getDenomination());
            /*
             * Count the trick.
             */
            if (winner.isPartner(contract.getDeclarer())) {
                declarerTricks++;

            } else {
                defenderTricks++;
            }

            /*
             * Winner leads the next trick.
             */
            currentLeader = winner;

            /*
             * Check whether all thirteen
             * tricks have been completed.
             */
            if (trickNo == 13) {
                finished = true;
            } else {
                trickNo++;
                tricks.add(new Trick(trickNo, currentLeader));
            }
        }

        return true;
    }

    /**
     * Determines which cards can legally be played.
     *
     * @param seat the player
     * @param hand the player's hand
     * @return legal cards
     */
    public ArrayList<Card> getLegalCards(Seat seat, Hand hand) {
        ArrayList<Card> legalCards = new ArrayList<>();
        if (seat == null || hand == null) {
            return legalCards;
        }
        Trick currentTrick = getCurrentTrick();

        /*
         * If no card has been played,
         * any card may be played.
         */
        if (currentTrick == null || currentTrick.getPlays().isEmpty()) {
            legalCards.addAll(hand.getCards());
            return legalCards;
        }

        /*
         * The first card played is the lead card.
         */
        Card leadCard = null;

        for (Map.Entry<Seat, Card> entry : currentTrick.getPlays().entrySet()) {
            if (entry.getKey().equals(currentTrick.getLeader())) {
                leadCard = entry.getValue();
                break;
            }
        }

        if (leadCard == null) {
            legalCards.addAll(hand.getCards());
            return legalCards;
        }

        Suit leadSuit = leadCard.getSuit();

        /*
         * Check whether the player has
         * the lead suit.
         */
        boolean hasLeadSuit = false;

        for (Card card : hand.getCards()) {

            if (card.getSuit() == leadSuit) {
                hasLeadSuit = true;
                break;
            }
        }

        /*
         * If the player has the lead suit,
         * they must follow suit.
         */
        if (hasLeadSuit) {
            for (Card card : hand.getCards()) {
                if (card.getSuit() == leadSuit) {
                    legalCards.add(card);
                }
            }

        } else {
            /*
             * If they do not have the lead suit,
             * they may play anything.
             */
            legalCards.addAll(hand.getCards());
        }

        return legalCards;
    }

    /**
     * Checks whether the game is finished.
     *
     * @return true if finished
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Returns the winner of the current trick.
     *
     * @return winner, or null if incomplete
     */
    public Seat getWinner() {

        Trick currentTrick = getCurrentTrick();
        if (currentTrick == null || !currentTrick.isComplete()) {
            return null;
        }

        return currentTrick.getWinner(contract.getDenomination());
    }

    /**
     * Returns declarer tricks.
     *
     * @return declarer tricks
     */
    public int getDeclarerTricks() {
        return declarerTricks;
    }

    /**
     * Returns defender tricks.
     *
     * @return defender tricks
     */
    public int getDefenderTricks() {
        return defenderTricks;
    }

    /**
     * Returns the remaining hand for a seat.
     *
     * @param seat the seat
     * @return remaining hand
     */
    public Hand getRemainingHand(Seat seat) {
        if (seat == null) {
            return null;
        }
        return remainingHands.get(seat.getPosition());
    }
}