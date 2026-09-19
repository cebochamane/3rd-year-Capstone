import java.util.ArrayList;
import java.util.List;

/**
 * Runs one scripted lesson. The learner bids as South and plays both North/South hands when their partnership declares. 
 * When defending, only South is controlled.
 * Incorrect choices unlock an optional hint for that action only.
 */
public final class TutorialSession {

    private final TutorialLesson lesson;
    private final Auction auction;
    private final List<TutorialAction> completedActions;
    private int nextActionIndex;
    private Gameplay gameplay;
    private TutorialPhase phase;
    private String feedback;
    private boolean claimed;
    private boolean conceded;
    private final boolean automaticallyAdvanceComputerActions;
    private final boolean waitForEndingButton;
    private int hintActionIndex = -1;

    /**
     * Initializes a new tutorial session using the specified tutorial lesson.
     * 
     * @param lesson The tutorial lesson to run in this session.
     */
    public TutorialSession(TutorialLesson lesson) {
        this(lesson, true);
    }

    /**
     * Creates a new tutorial session using the specified tutorial lesson with control over automatic advancement of computer actions.
     * 
     * @param lesson The tutorial lesson to run in this session.
     * @param automaticallyAdvanceComputerActions If true, computer actions proceed automatically without user intervention.
     */
    public TutorialSession(TutorialLesson lesson, boolean automaticallyAdvanceComputerActions) {
        this(lesson, automaticallyAdvanceComputerActions, false);
    }

    /**
     * Constructs a tutorial session with specified lesson & flags for automatic computer action advancement,
     * and waiting for ending button.
     * 
     * @param lesson The tutorial lesson to run in this session.
     * @param automaticallyAdvanceComputerActions If true, computer actions proceed automatically without user intervention.
     * @param waitForEndingButton If true, lesson waits for a user action to conclude the ending (claim or concede).
     */
    public TutorialSession(TutorialLesson lesson, boolean automaticallyAdvanceComputerActions, boolean waitForEndingButton) {
        if (lesson == null) {
            throw new IllegalArgumentException("A tutorial lesson is required.");
        }

        this.lesson = lesson;
        this.auction = new Auction();
        this.completedActions = new ArrayList<>();
        this.phase = lesson.getType() == TutorialLessonType.BID_AND_PLAY
                ? TutorialPhase.BIDDING : TutorialPhase.PLAYING;
        this.feedback = "";
        this.automaticallyAdvanceComputerActions = automaticallyAdvanceComputerActions;
        this.waitForEndingButton = waitForEndingButton;

        if (phase == TutorialPhase.PLAYING) {
            startGameplay();
        }

        if (automaticallyAdvanceComputerActions) {
            advanceComputerActions();
        }
    }

    /**
     * Returns the tutorial lesson associated with this session.
     * 
     * @return The current tutorial lesson.
     */
    public TutorialLesson getLesson() {
        return lesson;
    }

    /**
     * Gets the current phase of the tutorial session.
     * 
     * @return The current tutorial phase (bidding, playing, or complete).
     */
    public TutorialPhase getPhase() {
        return phase;
    }

    /**
     * Retrieves the auction object representing the bidding so far.
     * 
     * @return The auction state of the tutorial session.
     */
    public Auction getAuction() {
        return auction;
    }

    /**
     * Returns the gameplay instance managing card play during the session.
     * 
     * @return The current gameplay state or null if not started.
     */
    public Gameplay getGameplay() {
        return gameplay;
    }

    /**
     * Provides feedback on the last learner action or lesson state.
     * 
     * @return A string containing feedback messages or prompts for the player.
     */
    public String getFeedback() {
        return feedback;
    }

    /**
     * Gets the seat expected to perform the next tutorial action.
     * @return The seat (player position) expected for the next action, or null if none.
     */
    public Seat getExpectedSeat() {
        TutorialAction action = nextAction();
        return action == null ? null : action.getSeat();
    }

    /**
     * Computes and returns the final score of the tutorial session if gameplay has finished.
     * 
     * @return The final score of the tutorial or null if incomplete or gameplay not available.
     */
    public Score getScore() {
        if (!isComplete() || gameplay == null) {
            return null;
        }

        if (waitForEndingButton && gameplay.isFinished()) {
            return Score.fromCompletedGame(lesson.getDeal(), gameplay);
        }
        Integer finalTricks = lesson.getFinalDeclarerTricks();

        if (finalTricks == null) {
            if (!gameplay.isFinished()) {
                return null;
            }

            return Score.fromCompletedGame(lesson.getDeal(), gameplay);
        }

        int minimum = gameplay.getDeclarerTricks();
        int maximum = 13 - gameplay.getDefenderTricks();

        if (finalTricks < minimum || finalTricks > maximum) {
            throw new IllegalStateException("The tutorial result contradicts the completed tricks.");
        }

        return new Score(lesson.getContract(), finalTricks, lesson.getDeal().getVulnerability());
    }
    /**
     * Determines if the tutorial session has been completed.
     * 
     * @return True if the tutorial phase is complete; false otherwise.
     */
    public boolean isComplete() {
        return phase == TutorialPhase.COMPLETE;
    }

    /**
     * Indicates whether the tutorial has ended with a claim.
     * 
     * @return True if the session ended by claiming; false otherwise.
     */
    public boolean isClaimed() {
        return claimed;
    }

    /**
     * Indicates whether the tutorial has ended with a concession.
     * 
     * @return True if the session ended by conceding; false otherwise.
     */
    public boolean isConceded() {
        return conceded;
    }

    /**
     * Checks whether the session is waiting for the player to claim to finish the lesson.
     * 
     * @return True if waiting for claim action; false otherwise.
     */
    public boolean isAwaitingClaim() {
        return waitForEndingButton && !isComplete() && nextAction() != null && nextAction().getType() == TutorialAction.Type.CLAIM;
    }

    /**
     * Checks whether the session is waiting for the player to concede to finish the lesson.
     * 
     * @return True if waiting for concede action; false otherwise.
     */
    public boolean isAwaitingConcede() {
        return waitForEndingButton && !isComplete() && nextAction() != null && nextAction().getType() == TutorialAction.Type.CONCEDE;
    }

    /**
     * Executes isAwaitingEnding.
     * 
     * @param isAwaitingConcede( the input value
     * @return the result
     */
    public boolean isAwaitingEnding() { return isAwaitingClaim() || isAwaitingConcede(); }

    /** Number of future tricks allocated to declarer's side by the lesson ending. */
    public int proposedEndingTricks(DdsSolver.Analysis analysis) {
        if (!isAwaitingEnding()) throw new IllegalStateException("No tutorial ending is waiting.");
        if (gameplay.isFinished()) return 0;
        if (analysis == null || !analysis.matches(gameplay)) {
            throw new IllegalStateException("The double-dummy analysis no longer matches this lesson.");
        }
        Integer result = lesson.getFinalDeclarerTricks();
        int tricks = result != null ? result - gameplay.getDeclarerTricks()
                : isAwaitingClaim() ? analysis.declarerTricks() : 0;
        if (tricks < 0 || tricks > analysis.declarerTricks()) {
            throw new IllegalStateException("The lesson RESULT cannot be achieved against optimal defence "
                    + "from this position. Ask the lecturer to check it; the lesson has not changed.");
        }
        return tricks;
    }

    /**
     * Confirms the tutorial session ending by claim or concede, settling the lesson result with provided analysis.
     * 
     * @param concede True to concede, false to claim.
     * @param analysis The double-dummy solver analysis for confirming the ending.
     * @return True if the ending confirmation was successful; false otherwise.
     */
    public boolean confirmEnding(boolean concede, DdsSolver.Analysis analysis) {
        if (concede ? !isAwaitingConcede() : !isAwaitingClaim()) return false;
        int tricks = proposedEndingTricks(analysis);
        if (!gameplay.isFinished() && !gameplay.settleTutorial(tricks, concede, analysis)) return false;
        hintActionIndex = -1;
        completedActions.add(nextAction());
        nextActionIndex++;
        claimed = !concede;
        conceded = concede;
        phase = TutorialPhase.COMPLETE;
        feedback = lesson.getNote();
        return true;
    }

    /**
     * Provides a prompt message for the South player based on the current expected action or tutorial state.
     * 
     * @return A string containing instructions or status messages for South.
     */
    public String getSouthPrompt() {
        return getLearnerPrompt();
    }

    /**
     * Determines if the specified seat is controlled by the learner or their partnership during the session.
     * 
     * @param seat The player seat to check control for.
     * @return True if the seat is controlled by the learner or their partnership; false otherwise.
     */
    public boolean controlsHand(Seat seat) {
        if (seat == null || seat.getPosition() == null) return false;
        Position declarer = lesson.getContract().getDeclarer().getPosition();
        return seat.getPosition() == Position.SOUTH
                || (seat.getPosition() == Position.NORTH
                    && (declarer == Position.NORTH || declarer == Position.SOUTH));
    }

    /** Bidding remains South-only; card play follows ownership of declarer and dummy. */
    //

    /**
     * Checks if it is currently the learner's turn to act in the tutorial session.
     * Bidding remains South-only; card play follows ownership of declarer and dummy.
     * 
     * @return True if it is the learner's turn; false otherwise.
     */
    public boolean isLearnerTurn() {
        TutorialAction expected = nextAction();
        if (isComplete() || expected == null) return false;
        return phase == TutorialPhase.BIDDING && isSouthBid(expected)
                || phase == TutorialPhase.PLAYING && expected.getType() == TutorialAction.Type.CARD
                    && controlsHand(expected.getSeat());
    }

    /**
     * Provides a prompt message instructing or informing the learner about their current tutorial action.
     * 
     * @return A string prompt guiding the learner's expected action or state.
     */
    public String getLearnerPrompt() {
        if (isAwaitingClaim()) return "Click Claim to finish the lesson.";
        if (isAwaitingConcede()) return "Click Concede to finish the lesson.";
        if (isComplete()) return "Lesson complete.";
        if (!isLearnerTurn()) return "The computer is making its move.";
        if (phase == TutorialPhase.BIDDING) return "Your turn to bid as South.";
        Seat expected = getExpectedSeat();
        String hand = expected.getPosition() == Position.NORTH ? "North" : "South";
        String role = expected.equals(gameplay.getDummy()) ? "dummy"
                : expected.equals(gameplay.getContract().getDeclarer()) ? "declarer" : "defender";
        return "Play " + hand + "'s hand (" + role + ").";
    }

    /**
     * Determines if the learner can request a hint for the current tutorial action due to an earlier incorrect choice.
     * 
     * @return True if a hint is available for the current action; false otherwise.
     */
    public boolean canRequestHint() {
        return isLearnerTurn() && hintActionIndex == nextActionIndex;
    }

    /**
     * Retrieves a hint message for the learner related to the expected correct action.
     * 
     * @return A string containing a hint describing the expected bid or card to play.
     */
    public String getHint() {
        if (!canRequestHint()) return "A hint is available after an incorrect bid or card choice.";
        TutorialAction expected = nextAction();
        if (expected.getType() == TutorialAction.Type.BID) return expectedBidMessage(expected.getBid());
        return "Play " + expected.getCard() + " from " + expected.getSeat() + "'s hand.";
    }

    /**
     * Registers a rejected learner choice and provides feedback indicating to try again or request a hint.
     * @param message A message explaining why the choice was rejected.
     * @return False indicating the choice was rejected.
     */
    private boolean rejectChoice(String message) {
        hintActionIndex = nextActionIndex;
        return reject(message + " Try again, or use Hint.");
    }

    /**
     * Submits the South player's bid if it matches the expected tutorial bid and advances the lesson.
     * 
     * @param bid The bid submitted by South.
     * @return True if the bid is correct and accepted; false if rejected.
     */
    public boolean submitSouthBid(Bid bid) {
        TutorialAction expected = nextAction();
        if (phase != TutorialPhase.BIDDING || !isSouthBid(expected)) {
            return reject("It is not South's turn to bid.");
        }

        if (bid == null || bid.getBidder() == null || bid.getBidder().getPosition() != Position.SOUTH) {
            return reject("Submit a bid for South.");
        }

        if (!sameBid(expected.getBid(), bid)) {
            return rejectChoice("That is not the lesson bid.");
        }

        complete(expected);
        feedback = "Correct bid.";

        if (automaticallyAdvanceComputerActions) {
            advanceComputerActions();
        }

        return true;
    }

    /**
     * Plays the South player's card if it matches the expected tutorial card and advances the lesson.
     * 
     * @param card The card played by South.
     * @return True if the card is correct and played; false if rejected.
     */
    public boolean playSouthCard(Card card) {
        return playLearnerCard(new Seat(Position.SOUTH), card);
    }

    /**
     * Processes the learner playing a card for the specified seat if it matches the expected tutorial card and advances the lesson.
     * 
     * @param seat The seat playing the card.
     * @param card The card played by the learner.
     * @return True if the card play is correct and accepted; false if rejected.
     */
    public boolean playLearnerCard(Seat seat, Card card) {
        TutorialAction expected = nextAction();

        if (phase != TutorialPhase.PLAYING || !isLearnerTurn()
                || seat == null || !seat.equals(expected.getSeat())) {
            return reject("It is not that hand's turn to play.");
        }

        if (card == null) {
            return reject("Select a card from the current hand.");
        }

        if (!sameCard(expected.getCard(), card)) {
            return rejectChoice("That is not the lesson card.");
        }

        if (!playScriptedCard(seat, expected.getCard())) {
            throw new IllegalStateException("The lesson's scripted card is not legal for " + seat + ".");
        }

        feedback = "Correct card.";
        complete(expected);

        if (automaticallyAdvanceComputerActions) {
            advanceComputerActions();
        }

        return true;
    }

    /**
     * Attempts to play the expected scripted card for the given seat if legal.
     * 
     * @param seat The seat that should play the card.
     * @param expectedCard The scripted card expected to be played.
     * @return True if the card was found in the player's hand and played successfully; false otherwise.
     */
    private boolean playScriptedCard(Seat seat, Card expectedCard) {
        if (gameplay == null || seat == null || expectedCard == null) {
            return false;
        }

        Hand hand = gameplay.getRemainingHand(seat);
        if (hand == null) {
            return false;
        }

        for (Card heldCard : hand.getCards()) {
            if (sameCard(heldCard, expectedCard)) {
                return gameplay.playCard(seat, heldCard);
            }
        }

        return false;
    }

    /**
     * Progresses the lesson by executing automated actions played by computer opponents until a learner turn or lesson end.
     */
    private void advanceComputerActions() {
        while (advanceOneComputerAction()) {
            // Keep advancing only in the original automatic-session mode.
        }
    }

    /**
     * Advances the lesson by processing one automated computer action if possible.
     * 
     * @return True if a computer action was processed; false if a learner turn or lesson complete.
     */
    public boolean advanceOneComputerAction() {
        if (isComplete()) {
            return false;
        }

        TutorialAction expected = nextAction();

        if (expected == null) {
            throw new IllegalStateException("The tutorial ended before 13 tricks were completed " + "and without Claim or Concede.");
        }

        if (isAwaitingEnding()) {
            feedback = getSouthPrompt();
            return false;
        }

        if (expected.getType() == TutorialAction.Type.CLAIM) {
            hintActionIndex = -1;
            claimed = true;
            phase = TutorialPhase.COMPLETE;
            feedback = lesson.getNote();
            return true;
        }

        if (expected.getType() == TutorialAction.Type.CONCEDE) {
            hintActionIndex = -1;
            conceded = true;
            phase = TutorialPhase.COMPLETE;
            feedback = lesson.getNote();
            return true;
        }

        if (isLearnerTurn()) {
            return false;
        }

        if (phase == TutorialPhase.BIDDING) {
            complete(expected);
            return true;
        }

        if (!playScriptedCard(expected.getSeat(), expected.getCard())) {
            throw new IllegalStateException(
                    "The lesson's scripted card is not legal for "
                            + expected.getSeat() + ": " + expected.getCard() + ".");
        }

        complete(expected);
        return true;
    }

    /**
     * Marks the given tutorial action as completed and updates session state accordingly.
     * 
     * @param action The tutorial action to mark as complete.
     */
    private void complete(TutorialAction action) {
        hintActionIndex = -1;
        completedActions.add(action);
        nextActionIndex++;

        if (phase == TutorialPhase.BIDDING) {
            if (!auction.addBid(action.getBid())) {
                throw new IllegalStateException("The tutorial contains an illegal auction call: "
                    + action.getBid() + ".");
            }

            if (!hasRemainingBid()) {
                addImplicitClosingPasses();
                startGameplay();
            }
        }

        // Finish naturally when all cards have been played and there are no remaining scripted actions.
        if (phase == TutorialPhase.PLAYING && gameplay != null 
                && gameplay.isFinished() && nextAction() == null) {
            phase = TutorialPhase.COMPLETE;
            feedback = lesson.getNote();
        }
    }

    /**
     * Adds the omitted closing passes used by the tutorial-file shorthand.
     * These calls complete the visible auction without asking the learner to
     * enter passes that were not present in the tutorial script.
     */
    private void addImplicitClosingPasses() {
        if (auction.isComplete()) {
            return;
        }

        Bid finalBid = auction.getFinalBid();
        if (finalBid == null || auction.getBids().isEmpty()) {
            throw new IllegalStateException(
                    "A bid-and-play tutorial needs a final contract bid.");
        }

        Seat bidder = auction.getBids().get(auction.getBids().size() - 1)
                .getBidder().next();

        while (!auction.isComplete()) {
            if (!auction.addBid(Bid.BidPass(bidder))) {
                throw new IllegalStateException("Could not complete the tutorial auction with closing passes.");
            }
            bidder = bidder.next();
        }
    }

    /**
     * Initializes the gameplay phase using the lesson deal and contract.
     */
    private void startGameplay() {
        this.gameplay = new Gameplay(lesson.getDeal(), lesson.getContract());
        this.phase = TutorialPhase.PLAYING;
    }

    /**
     * Checks if there are remaining bid actions in the tutorial script.
     * 
     * @return True if there is a next bid action pending; false otherwise.
     */
    private boolean hasRemainingBid() {
        TutorialAction next = nextAction();
        return next != null && next.getType() == TutorialAction.Type.BID;
    }

    /**
     * Retrieves the next tutorial action to be performed based on progress index.
     * 
     * @return The next tutorial action or null if none remain.
     */
    private TutorialAction nextAction() {
        return nextActionIndex < lesson.getActions().size()
                ? lesson.getActions().get(nextActionIndex) : null;
    }

    /**
     * Checks if the given action is assigned to the South seat.
     * 
     * @param action The tutorial action to check.
     * @return True if the action is by South; false otherwise.
     */
    private boolean isSouth(TutorialAction action) {
        return action != null && action.getSeat() != null
            && action.getSeat().getPosition() == Position.SOUTH;
    }

    /**
     * Determines if the given tutorial action is a South bid.
     * 
     * @param action The tutorial action to evaluate.
     * @return True if the action is a bid by South; false otherwise.
     */
    private boolean isSouthBid(TutorialAction action) {
        return action != null && action.getType() == TutorialAction.Type.BID && isSouth(action);
    }

    /**
     * Sets feedback with the given message and returns false to indicate rejection.
     * 
     * @param message The rejection feedback message.
     * @return False to indicate the action was rejected.
     */
    private boolean reject(String message) {
        feedback = message;
        return false;
    }

    /**
     * Compares two cards for equivalence by suit and rank.
     * 
     * @param expected The expected card.
     * @param actual The actual card to compare.
     * @return True if both cards match in suit and rank; false otherwise.
     */
    private boolean sameCard(Card expected, Card actual) {
        return expected != null && actual != null
                && expected.getSuit() == actual.getSuit()
                && expected.getRank() == actual.getRank();
    }

    /**
     * Compares two bids for equivalence considering bidder, type and details.
     * 
     * @param expected The expected bid.
     * @param actual The actual bid to compare.
     * @return True if both bids are identical in bidder and bid details; false otherwise.
     */
    private boolean sameBid(Bid expected, Bid actual) {
        if (expected == null || actual == null || !expected.getBidder().equals(actual.getBidder())) {
            return false;
        }
        if (expected.isPass() || expected.isDouble() || expected.isRedouble()) {
            return expected.isPass() == actual.isPass()
                    && expected.isDouble() == actual.isDouble()
                    && expected.isRedouble() == actual.isRedouble();
        }
        return !actual.isPass() && !actual.isDouble() && !actual.isRedouble()
                && expected.getLevel() == actual.getLevel()
                && expected.getDenomination() == actual.getDenomination();
    }

    /**
     * Generates a descriptive message for the expected bid type or level and denomination.
     * 
     * @param bid The bid from which to create the message.
     * @return A string message describing the expected bid.
     */
    private String expectedBidMessage(Bid bid) {
        if (bid.isPass()) {
            return "Pass.";
        }
        if (bid.isDouble()) {
            return "Double.";
        }
        if (bid.isRedouble()) {
            return "Redouble.";
        }
        return "Bid " + bid.getLevel() + " " + bid.getDenomination() + ".";
    }
}
