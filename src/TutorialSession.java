import java.util.ArrayList;
import java.util.List;

/**
 * Runs one scripted lesson. West, North and East are played automatically;
 * South must supply exactly the next scripted bid or card.
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

    public TutorialSession(TutorialLesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("A tutorial lesson is required.");
        }
        this.lesson = lesson;
        this.auction = new Auction();
        this.completedActions = new ArrayList<>();
        this.phase = lesson.getType() == TutorialLessonType.BID_AND_PLAY
                ? TutorialPhase.BIDDING : TutorialPhase.PLAYING;
        this.feedback = "";

        if (phase == TutorialPhase.PLAYING) {
            startGameplay();
        }
        advanceComputerActions();
    }

    public TutorialLesson getLesson() {
        return lesson;
    }

    public TutorialPhase getPhase() {
        return phase;
    }

    public Auction getAuction() {
        return auction;
    }

    public Gameplay getGameplay() {
        return gameplay;
    }

    public String getFeedback() {
        return feedback;
    }

    /** Returns the player required by the next scripted action, if any. */
    public Seat getExpectedSeat() {
        TutorialAction action = nextAction();
        return action == null ? null : action.getSeat();
    }

    public boolean isComplete() {
        return phase == TutorialPhase.COMPLETE;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public boolean isConceded() {
        return conceded;
    }

    /** Returns a concise prompt for South, or the lesson note after completion. */
    public String getSouthPrompt() {
        TutorialAction expected = nextAction();
        if (expected == null) {
            return isComplete() ? lesson.getNote() : "The computer is making its move.";
        }
        if (expected.getType() == TutorialAction.Type.BID && isSouth(expected)) {
            return "Your turn to bid as South.";
        }
        if (expected.getType() == TutorialAction.Type.CARD && isSouth(expected)) {
            return "Your turn to play as South.";
        }
        return "The computer is making its move.";
    }

    /** Accepts South's bid only when it matches the scripted auction exactly. */
    public boolean submitSouthBid(Bid bid) {
        TutorialAction expected = nextAction();
        if (phase != TutorialPhase.BIDDING || !isSouthBid(expected)) {
            return reject("It is not South's turn to bid.");
        }
        if (!sameBid(expected.getBid(), bid)) {
            return reject("That is not the lesson bid. " + expectedBidMessage(expected.getBid()));
        }

        complete(expected);
        feedback = "Correct bid.";
        advanceComputerActions();
        return true;
    }

    /** Accepts South's card only when it matches the scripted play exactly. */
    public boolean playSouthCard(Card card) {
        TutorialAction expected = nextAction();
        if (phase != TutorialPhase.PLAYING || !isSouthCard(expected)) {
            return reject("It is not South's turn to play.");
        }
        if (!sameCard(expected.getCard(), card)) {
            return reject("That is not the lesson card. Play " + expected.getCard() + ".");
        }
        if (!gameplay.playCard(new Seat(Position.SOUTH), expected.getCard())) {
            throw new IllegalStateException("The lesson's scripted South card is not legal: "
                    + expected.getCard() + ".");
        }

        complete(expected);
        feedback = "Correct card.";
        advanceComputerActions();
        return true;
    }

    private void advanceComputerActions() {
        while (!isComplete()) {
            TutorialAction expected = nextAction();
            if (expected == null) {
                throw new IllegalStateException("The tutorial ended without Claim or Concede.");
            }

            if (expected.getType() == TutorialAction.Type.CLAIM) {
                claimed = true;
                phase = TutorialPhase.COMPLETE;
                feedback = lesson.getNote();
                return;
            }
            if (expected.getType() == TutorialAction.Type.CONCEDE) {
                conceded = true;
                phase = TutorialPhase.COMPLETE;
                feedback = lesson.getNote();
                return;
            }

            if (phase == TutorialPhase.BIDDING) {
                if (isSouth(expected)) {
                    return;
                }
                complete(expected);
                continue;
            }

            if (isSouth(expected)) {
                return;
            }
            if (!gameplay.playCard(expected.getSeat(), expected.getCard())) {
                throw new IllegalStateException("The lesson's scripted card is not legal for "
                        + expected.getSeat() + ": " + expected.getCard() + ".");
            }
            complete(expected);
        }
    }

    private void complete(TutorialAction action) {
        completedActions.add(action);
        nextActionIndex++;

        if (phase == TutorialPhase.BIDDING) {
            auction.getBids().add(action.getBid());
            if (!hasRemainingBid()) {
                startGameplay();
            }
        }
    }

    private void startGameplay() {
        this.gameplay = new Gameplay(lesson.getDeal(), lesson.getContract());
        this.phase = TutorialPhase.PLAYING;
    }

    private boolean hasRemainingBid() {
        TutorialAction next = nextAction();
        return next != null && next.getType() == TutorialAction.Type.BID;
    }

    private TutorialAction nextAction() {
        return nextActionIndex < lesson.getActions().size()
                ? lesson.getActions().get(nextActionIndex) : null;
    }

    private boolean isSouth(TutorialAction action) {
        return action != null && action.getSeat() != null
                && action.getSeat().getPosition() == Position.SOUTH;
    }

    private boolean isSouthBid(TutorialAction action) {
        return action != null && action.getType() == TutorialAction.Type.BID && isSouth(action);
    }

    private boolean isSouthCard(TutorialAction action) {
        return action != null && action.getType() == TutorialAction.Type.CARD && isSouth(action);
    }

    private boolean reject(String message) {
        feedback = message;
        return false;
    }

    private boolean sameCard(Card expected, Card actual) {
        return expected != null && actual != null
                && expected.getSuit() == actual.getSuit()
                && expected.getRank() == actual.getRank();
    }

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