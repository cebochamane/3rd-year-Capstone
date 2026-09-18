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
    private Seat acceptedClaimant;

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
            Seat declarer = contract.getDeclarer();
            if (winner.isPartner(contract.getDeclarer()) || winner.equals(declarer)) {
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

    private ArrayList<Card> getLegalCards(Seat seat, Hand hand, Trick currentTrick) {

        ArrayList<Card> legalCards = new ArrayList<>();

        if (seat == null || hand == null) {
            return legalCards;
        }

        /*
         * If no card has been played, any card may be played.
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

        boolean hasLeadSuit = false;

        for (Card card : hand.getCards()) {
            if (card.getSuit() == leadSuit) {
                hasLeadSuit = true;
                break;
            }
        }

        if (hasLeadSuit) {

            for (Card card : hand.getCards()) {
                if (card.getSuit() == leadSuit) {
                    legalCards.add(card);
                }
            }

        } else {
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
     * Returns the strongest legal card for the current trick.
     * Used when checking wheather a claim is valid.
     * 
     * @param legalCards cards the player may legally play
     * @param trick      the current trick
     * @return stongest legal card
     */
    private Card getStrongestLegalCard(ArrayList<Card> legalCards, Trick trick, Contract contract) {

        if (legalCards == null || legalCards.isEmpty()) {
            return null;
        }
        Card strongest = legalCards.get(0);

        /*
         * If this player is leading there is no lead suit yet.
         * Choose the highest-ranked card.
         */
        if (trick.getPlays().isEmpty()) {

            for (Card card : legalCards) {
                if (card.getRank().compareTo(strongest.getRank()) > 0) {
                    strongest = card;
                }
            }
            return strongest;
        }

        /*
         * The trick has already started, so find the lead suit.
         */
        Card leadCard = trick.getPlays().get(trick.getLeader());
        Suit leadSuit = leadCard.getSuit();
        Suit trump = contract.getDenomination();

        /*
         * Find the strongest legal card in the context of this trick.
         */
        for (Card card : legalCards) {
            if (card.compareTo(strongest, trump, leadSuit) > 0) {
                strongest = card;
            }
        }
        return strongest;
    }

    /**
     * Create a copy of all the remaining hands.
     * The copies can be changed during claim validation without
     * changing the real game.
     */
    private Map<Position, Hand> copyRemainingHands() {
        Map<Position, Hand> copiedHands = new HashMap<>();

        for (Position position : Position.values()) {

            Hand originalHand = remainingHands.get(position);
            Hand copiedHand = new Hand(new ArrayList<>(originalHand.getCards()));

            copiedHands.put(position, copiedHand);
        }
        return copiedHands;
    }

    /**
     * Create a copy of the current trick.
     * This allows claim validation to continue from the current position without
     * changing the real trick.
     * 
     * @return copied current trick
     */
    private Trick copyCurrentTrick() {

        Trick currentTrick = this.getCurrentTrick();

        if (currentTrick == null) {
            return null;
        }
        Trick copiedTrick = new Trick(currentTrick.getTrickNo(), currentTrick.getLeader());

        for (Map.Entry<Seat, Card> play : currentTrick.getPlays().entrySet()) {
            copiedTrick.addPlay(play.getKey(), play.getValue());
        }
        return copiedTrick;
    }

    /**
     * Checks whether the current player can validily claim all remaining tricks.
     * 
     * @param claimant the player making the claim
     * @return true if the claiming side wins all remaining tricks in the claim
     *         simulation
     * 
     */
    public boolean isClaimValid(Seat claimant) {

        if (finished || claimant == null) {
            return false;
        }

        /*
         * Make copies so that checking the claim does not change the real game.
         */
        Map<Position, Hand> simulatedHands = copyRemainingHands();
        Trick simulatedTrick = this.copyCurrentTrick();

        if (simulatedTrick == null) {
            return false;
        }

        /*
         * Determine which partnership is making the claim.
         */
        Seat declarer = contract.getDeclarer();
        boolean claimantIsDeclarerSide = claimant.equals(declarer) || claimant.isPartner(declarer);

        /*
         * Start from the player whose turn it currently is.
         */
        Seat currentPlayer = simulatedTrick.getLeader();

        for (int i = 0; i < simulatedTrick.getPlays().size(); i++) {
            currentPlayer = currentPlayer.next();
        }

        /*
         * Continue simulating until there are no cards left.
         */

        while (true) {
            /*
             * Get the current player's simulated hand.
             */
            Hand simulatedHand = simulatedHands.get(currentPlayer.getPosition());

            if (simulatedHand == null || simulatedHand.getCards().isEmpty()) {
                /*
                 * No cards remain, so every remaining trick
                 * has been successfully simulated.
                 */
                return true;
            }

            /*
             * Determine which cards this player may legally play.
             */
            ArrayList<Card> legalCards = getLegalCards(currentPlayer, simulatedHand, simulatedTrick);

            /*
             * Choose the strongest legal card.
             */
            Card cardToPlay = this.getStrongestLegalCard(legalCards, simulatedTrick, contract);

            if (cardToPlay == null) {
                return false;
            }

            /*
             * Play the card only in the simulation.
             */
            simulatedTrick.addPlay(currentPlayer, cardToPlay);
            simulatedHand.removeCard(cardToPlay);

            /*
             * If the trick is not complete, move clockwise to the next player.
             */
            if (!simulatedTrick.isComplete()) {
                currentPlayer = currentPlayer.next();
                continue; // jump back to "while true"
            }

            /*
             * The trick is complete, so determine its winner.
             */
            Seat winner = simulatedTrick.getWinner(contract.getDenomination());

            /*
             * Determine whether the winner belongs to the declarer's partenership.
             */
            boolean winnerIsDeclarerSide = winner.equals(declarer) || winner.isPartner(declarer);

            /*
             * The claim fails immediately if the opponents win even one of the remaining
             * tricks.
             */
            if (winnerIsDeclarerSide != claimantIsDeclarerSide) {
                return false;
            }

            /*
             * The claiming side won this trick otherwise the method would have terminated.
             * 
             * If all four simulated hands are now empty, there are no more tricks to play.
             */
            boolean cardsRemaining = false;
            for (Hand hand : simulatedHands.values()) {

                if (!hand.getCards().isEmpty()) {
                    cardsRemaining = true;
                    break;
                }
            }
            if (!cardsRemaining) {
                return true;
            }

            /*
             * The winner of this trick leads the next trick.
             */
            currentPlayer = winner;

            simulatedTrick = new Trick(simulatedTrick.getTrickNo() + 1, winner);

        }
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

    /**
     * Returns the player whose turn it currently is.
     *
     * @return the current player
     */
    public Seat getCurrentPlayer() {
        Trick currentTrick = getCurrentTrick();

        if (currentTrick == null) {
            return currentLeader;
        }

        Seat currentPlayer = currentTrick.getLeader();
        int numberOfPlays = currentTrick.getPlays().size();

        for (int i = 0; i < numberOfPlays; i++) {
            currentPlayer = currentPlayer.next();
        }

        return currentPlayer;
    }

    /**
     * Returns the declarer's partner, who is the dummy.
     *
     * @return the dummy's seat
     */
    public Seat getDummy() {
        if (contract == null || contract.getDeclarer() == null) {
            return null;
        }

        Seat declarer = contract.getDeclarer();
        return declarer.next().next();
    }

    public Seat getAcceptedClaimant() {
        return acceptedClaimant;
    }

    /**
     * Restores an already accepted claim after saved cards have been replayed.
     *
     * This validates the recorded outcome without re-running claim simulation.
     * Unplayed cards and any partial trick remain as they were at the claim.
     */
    void restoreAcceptedClaim(
            Seat claimant,
            int finalDeclarerTricks,
            int finalDefenderTricks) {

        if (finished || acceptedClaimant != null) {
            throw new IllegalStateException(
                    "Cannot restore a claim into an already finished game.");
        }

        if (claimant == null || claimant.getPosition() == null) {
            throw new IllegalStateException(
                    "The saved claim has no valid claimant.");
        }

        if (!claimant.equals(getCurrentPlayer())) {
            throw new IllegalStateException(
                    "The saved claimant was not the player whose turn it was.");
        }

        if (finalDeclarerTricks < 0 || finalDeclarerTricks > 13
                || finalDefenderTricks < 0 || finalDefenderTricks > 13
                || finalDeclarerTricks + finalDefenderTricks != 13) {
            throw new IllegalStateException(
                    "Saved final trick totals must account for all 13 tricks.");
        }

        int completedTricks = declarerTricks + defenderTricks;

        if (completedTricks < 0 || completedTricks >= 13) {
            throw new IllegalStateException(
                    "There are no remaining tricks to restore as claimed.");
        }

        int remainingTricks = 13 - completedTricks;

        Seat declarer = contract.getDeclarer();
        boolean claimantIsDeclarerSide =
                claimant.equals(declarer) || claimant.isPartner(declarer);

        int expectedDeclarerTricks = declarerTricks
                + (claimantIsDeclarerSide ? remainingTricks : 0);

        int expectedDefenderTricks = defenderTricks
                + (claimantIsDeclarerSide ? 0 : remainingTricks);

        if (finalDeclarerTricks != expectedDeclarerTricks
                || finalDefenderTricks != expectedDefenderTricks) {
            throw new IllegalStateException(
                    "Saved claim totals contradict the tricks already won "
                    + "or the claiming partnership.");
        }

        // Mutate only after every check succeeds.
        declarerTricks = finalDeclarerTricks;
        defenderTricks = finalDefenderTricks;
        acceptedClaimant = claimant;
        finished = true;
    }

    /**
     * Checks whether the dummy's hand should be visible.
     * The dummy becomes visible after the opening lead.
     *
     * @return true if the dummy should be visible
     */
    public boolean isDummyVisible() {
        if (contract == null) {
            return false;
        }

        if (trickNo > 1) {
            return true;
        }

        Trick currentTrick = getCurrentTrick();

        return currentTrick != null && currentTrick.getPlays().size() > 0;
    }

    /**
     * Checks whether a player's hand should currently be visible.
     *
     * @param seat the player whose hand is being checked
     * @return true if the hand should be visible
     */
    public boolean isHandVisible(Seat seat) {
        if (seat == null) {
            return false;
        }
        Seat currentPlayer = getCurrentPlayer();

        if (currentPlayer == null) {
            return false;
        }

        /*
         * The current player's hand is visible.
         */
        if (seat.equals(currentPlayer)) {
            return true;
        }

        /*
         * Before the opening lead, only the current
         * player's hand is visible.
         */
        if (!isDummyVisible()) {
            return false;
        }

        Seat dummy = getDummy();
        if (dummy == null) {
            return false;
        }

        /*
         * After the opening lead, dummy stays visible.
         */
        if (seat.equals(dummy)) {
            return true;
        }

        /*
         * When dummy is playing, declarer's hand
         * also remains visible.
         */
        Seat declarer = contract.getDeclarer();

        return currentPlayer.equals(dummy) && seat.equals(declarer);
    }

    /**
     * Attempts to claim all remaining tricks.
     *
     * @param claimant the player making the claim
     * @return true if the claim is valid and accepted, otherwise false
     */
    public boolean claim(Seat claimant) {

        /*
         * First validate the claim.
         */
        if (!isClaimValid(claimant)) {
            return false;
        }

        /*
         * Count how many tricks have already been completed.
         */
        int completedTricks = declarerTricks + defenderTricks;

        int remainingTricks = 13 - completedTricks;

        /*
         * Determine which partnership made the claim.
         */
        Seat declarer = contract.getDeclarer();

        boolean claimantIsDeclarerSide = claimant.equals(declarer)
                || claimant.isPartner(declarer);

        /*
         * Award all remaining tricks to the claiming side.
         */
        if (claimantIsDeclarerSide) {
            declarerTricks += remainingTricks;
        } else {
            defenderTricks += remainingTricks;
        }

        /*
         * A successful claim ends the game.
         */
        acceptedClaimant = claimant;
        finished = true;
        return true;
    }

}