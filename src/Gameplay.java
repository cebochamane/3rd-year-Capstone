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
    private Seat acceptedConceder;
    private int acceptedTricks = -1;

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
     * Calculates the number of tricks remaining to be played in the game.
     * 
     * @return the count of remaining tricks
     */
    public int getRemainingTricks() {
        return 13 - declarerTricks - defenderTricks;
    }

    /**
     * Performs analysis on the remaining tricks using a double-dummy solver.
     * 
     * @return analysis result of remaining tricks
     */
    public DdsSolver.Analysis analyzeRemainingTricks() {
        return DdsSolver.analyze(this);
    }

    /**
     * Checks if the given player is the one currently expected to act.
     * 
     * @param actor the player to check
     * @return true if the player is the current actor, false otherwise
     */
    private boolean isCurrentActor(Seat actor) {
        return actor != null && actor.getPosition() != null && actor.equals(getCurrentPlayer());
    }

    /**
     * Determines if the given player can validly claim all remaining tricks.
     * 
     * @param claimant the player attempting the claim
     * @return true if the claim is valid, false otherwise
     */
    public boolean isClaimValid(Seat claimant) {
        return isClaimValid(claimant, getRemainingTricks());
    }

    /**
     * Determines if the given player can validly claim a specified number of tricks.
     * 
     * @param claimant the player attempting the claim
     * @param tricks the number of tricks being claimed
     * @return true if claiming the specified number of tricks is valid, false otherwise
     */
    public boolean isClaimValid(Seat claimant, int tricks) {
        if (finished || !isCurrentActor(claimant) || tricks < 0 || tricks > getRemainingTricks()) {
            return false;
        }

        return tricks <= analyzeRemainingTricks().tricksFor(claimant, contract);
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

    /**
     * Returns the player whose claim has been accepted if any.
     * 
     * @return the claimant seat whose claim has been accepted, or null if none
     */
    public Seat getAcceptedClaimant() {
        return acceptedClaimant;
    }

    /**
     * Restores a previously accepted claim with specified final declarer and defender tricks.
     * 
     * @param claimant the player who claimed
     * @param finalDeclarerTricks final number of tricks credited to declarer side
     * @param finalDefenderTricks final number of tricks credited to defender side
     */
    void restoreAcceptedClaim(Seat claimant, int finalDeclarerTricks, int finalDefenderTricks) {
        restoreSettlement(claimant, false, getRemainingTricks(), finalDeclarerTricks, finalDefenderTricks);
    }

    /**
     * Restores a saved settlement state including conceding and awarded tricks, validating consistency.
     * 
     * @param actor the player settling
     * @param conceded true if the settlement is a concession
     * @param awardedTricks number of tricks awarded in the settlement
     * @param finalDeclarerTricks final declarer trick count
     * @param finalDefenderTricks final defender trick count
     */
    void restoreSettlement(Seat actor, boolean conceded, int awardedTricks,
            int finalDeclarerTricks, int finalDefenderTricks) {
        if (finished || !isCurrentActor(actor)) {
            throw new IllegalStateException("The saved settlement has an invalid actor or phase.");
        }

        if (awardedTricks < 0 || awardedTricks > getRemainingTricks() || (conceded && awardedTricks != 0)) {
            throw new IllegalStateException("The saved settlement has an invalid trick allocation.");
        }

        boolean declaringSide = actor.equals(contract.getDeclarer()) || actor.isPartner(contract.getDeclarer());
        int expectedDeclarer = declarerTricks + (declaringSide ? awardedTricks : getRemainingTricks() - awardedTricks);

        if (finalDeclarerTricks != expectedDeclarer || finalDefenderTricks != 13 - expectedDeclarer) {
            throw new IllegalStateException("Saved final totals contradict the tricks already won or awarded.");
        }
        settle(actor, awardedTricks, conceded);
    }

    /**
     * Returns the player associated with the current settlement or concession phase.
     * 
     * @return the player who performed the accepted claim or concession
     */
    public Seat getSettlementActor() {
        return acceptedConceder == null ? acceptedClaimant : acceptedConceder;
    }

    /**
     * Checks if the current game has been conceded by a player.
     * 
     * @return true if the game is conceded, false otherwise
     */
    public boolean isConceded() { 
        return acceptedConceder != null; 
    }
    
    /**
     * Returns the number of tricks accepted as won in a claim or concession.
     * 
     * @return the count of accepted tricks
     */
    public int getAcceptedTricks() { 
        return acceptedTricks; 
    }

    /**
     * Finalizes the game by settling with a player awarding specified tricks and concession status.
     * 
     * @param actor the player settling the result
     * @param tricks the number of tricks awarded
     * @param conceded true if the game was conceded
     */
    private void settle(Seat actor, int tricks, boolean conceded) {
        int remaining = getRemainingTricks();
        boolean declaringSide = actor.equals(contract.getDeclarer()) || actor.isPartner(contract.getDeclarer());
        declarerTricks += declaringSide ? tricks : remaining - tricks;
        defenderTricks = 13 - declarerTricks;
        acceptedTricks = tricks;
        acceptedClaimant = conceded ? null : actor;
        acceptedConceder = conceded ? actor : null;
        finished = true;
    }

    /**
     * Attempts to settle the game in a tutorial lession validating the declared tricks and analysis.
     * 
     * @param declarerRemainingTricks number of tricks declarer claims to remain
     * @param concede true if conceding
     * @param analysis analysis result to validate settlement
     * @return true if settlement was successful, false otherwise
     */
    boolean settleTutorial(int declarerRemainingTricks, boolean concede, DdsSolver.Analysis analysis) {
        if (finished) {
            return false;
        }

        if (declarerRemainingTricks < 0 || declarerRemainingTricks > getRemainingTricks()) {
            return false;
        }

        if (analysis == null || !analysis.matches(this) || declarerRemainingTricks > analysis.declarerTricks()) {
            return false;
        }

        settle(contract.getDeclarer(), declarerRemainingTricks, concede);
        return true;
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
        return claim(claimant, getRemainingTricks());
    }

    /**
     * Attempts to claim a specified number of remaining tricks for the specified player.
     * 
     * @param claimant the player making the claim
     * @param tricks number of tricks claimed
     * @return true if the claim was valid and accepted, false otherwise
     */
    public boolean claim(Seat claimant, int tricks) {
        if (finished || !isCurrentActor(claimant) || tricks < 0 || tricks > getRemainingTricks()) return false;
        return claim(claimant, tricks, analyzeRemainingTricks());
    }

    /**
     * Attempts to claim a specified number of tricks validated against a solver analysis.
     * 
     * @param claimant the player making the claim
     * @param tricks number of tricks claimed
     * @param analysis solver analysis to validate claim
     * @return true if the claim was valid and accepted, false otherwise
     */
    boolean claim(Seat claimant, int tricks, DdsSolver.Analysis analysis) {
        if (finished || !isCurrentActor(claimant) || analysis == null || !analysis.matches(this)
                || tricks < 0 || tricks > analysis.tricksFor(claimant, contract)) return false;
        settle(claimant, tricks, false);
        return true;
    }

    /**
     * Attempts to concede the game for the given player, ending play.
     * 
     * @param conceder the player conceding
     * @return true if the concession was accepted, false otherwise
     */
    public boolean concede(Seat conceder) {
        if (finished || !isCurrentActor(conceder)) return false;
        settle(conceder, 0, true);
        return true;
    }
}
