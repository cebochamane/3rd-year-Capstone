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
        if (deal == null) {
            throw new IllegalArgumentException("Deal cannot be null");
        }
        if (contract == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        this.contract = contract;

        this.tricks = new ArrayList<>();
        this.trickNo = 1;
        this.declarerTricks = 0;
        this.defenderTricks = 0;
        this.defenderTricks = 0;
        this.finished = false;
        this.currentLeader = contract.getDeclarer();
        this.remainingHands = new HashMap<>();

        for (Position position : Position.values()) {
            Hand originalHand = deal.getHand(new Seat(position));
            if (originalHand == null) {
                throw new IllegalArgumentException("Hand for position " + position + " is missing in the deal");
            } else {
                Hand copiedHand = new Hand(new ArrayList<>(originalHand.getCards()));
                remainingHands.put(position, copiedHand);
            }
        }

        this.tricks.add(new Trick(trickNo, currentLeader));

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
        if (finished) {
            return false;
        }
        if (seat == null || card == null) {
            return false;
        }
        Trick currentTrick = tricks.get(tricks.size() - 1);
        Seat expectedPlayer = currentTrick.getLeader();// ************** */

        for (int i = 0; i < currentTrick.getPlays().size(); i++) {
            expectedPlayer = expectedPlayer.next();
        }

        if (!expectedPlayer.equals(seat)) {
            return false;
        }
        Hand hand = remainingHands.get(seat.getPosition());
        if (hand == null) {
            return false;
        }
        if (!hand.contains(card)) {
            return false;
        }

        ArrayList<Card> legalCards = currentTrick.getLegalCards(seat, hand);// ************** */
        if (!legalCards.contains(card)) {
            return false;
        }

        currentTrick.addPlay(seat, card);
        removeCardFromHand(seat, card);

        if (trickNo == 13) {
            finished = true;
        } else {
            trickNo++;
            currentLeader = winner;
            tricks.add(new Trick(trickNo, currentLeader));
        }

        return true;
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
        ArrayList<Card> legalCards = new ArrayList<>();
        if (seat == null || hand == null) {
            return legalCards;
        }

        Trick currentTrick = getCurrentTrick();

        if (currentTrick.getPlays().isEmpty()) {
            legalCards.addAll(hand.getCards());
            return legalCards;
        }

        Card leadCard = currentTrick.getPlays().get(currentTrick.getLeader());
        if (leadCard == null) {
            legalCards.addAll(hand.getCards());
            return legalCards;
        }

        Suit leadSuit = leadCard.getSuit();
        for (Card card : hand.getCards()) {
            if (card.getSuit() == leadSuit) {
                legalCards.add(card);
            }
        }

        if (legalCards.isEmpty()) {
            legalCards.addAll(hand.getCards());
        }
        return legalCards;
    }

    /**
     * Checks whether all 13 tricks have been completed.
     *
     * @return true if the game is finished, otherwise false
     */
    public boolean isFinished() {
        return finished;
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
        if (tricks.isEmpty()) {
            return null;
        }
        Trick currentTrick = tricks.get(tricks.size() - 1);
        if (!currentTrick.isComplete()) {
            return null;
        }
        Suit trump;
        if (contract.isNoTrump()) {
            trump = Suit.NT;
        } else {
            trump = contract.getDenomination();
        }

        return currentTrick.getWinner(trump);
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

    public Trick getCurrentTrick() {
        return tricks.get(tricks.size() - 1);

    }

    private Seat resolveCurrentTrick() {
        Trick currentTrick = getCurrentTrick();
        Suit trump;
        if (contract.isNoTrump()) {
            trump = Suit.NT;
        } else {
        }
        {
            trump = contract.getDenomination();
        }
        Seat winner = currentTrick.getWinner(trump);
        if (!currentTrick.isComplete()) {
            return null;
        }
        if (winner.equals(contract.getDeclarer()) || winner.isPartner(contract.getDeclarer())) {
            declarerTricks++;
        } else {
            defenderTricks++;
        }
        return currentTrick.getWinner(trump);
    }

    private void removeCardFromHand(Hand hand, Card card) {
        for (int i = 0; i < hand.getCards().size(); i++) {
            Card existingCard = hand.getCards().get(i);
            if (existingCard.getSuit() == card.getSuit() && existingCard.getRank() == card.getRank()) {
                hand.getCards().remove(i);
                return;
            }
        }
    }
}