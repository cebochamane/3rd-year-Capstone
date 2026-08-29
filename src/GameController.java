import javax.swing.SwingUtilities;

public class GameController {

    private GameView view;
    private Storage storage;
    private Deal currentDeal;
    private Auction auction;
    private Contract contract;
    private Gameplay gameplay;
    private GamePhase phase = GamePhase.MENU;
    private Seat currentBidder;
    private Seat currentPlayer;

    public void startNewGame() {
        Seat dealer = new Seat(Position.NORTH);
        Vulnerability vulnerability = Vulnerability.NONE;
        startRandomDeal(dealer, vulnerability);
    }

    public void loadGame(String gameId) {
        // TODO: implement
    }

    public void saveGame() {
        // TODO: implement
    }

    public void startRandomDeal(Seat dealer, Vulnerability vulnerability) {
        Deal deal = new Deal(dealer, vulnerability);
        deal.dealCards();
        startLoadedDeal(deal);
    }

    public void startLoadedDeal(Deal deal) {
        if (!isCompleteDeal(deal))
            throw new IllegalArgumentException("A loaded deal must contain four hands of 13 cards.");
        this.currentDeal = deal;
        startAuction();
    }

    public void startAuction() {
        if (currentDeal == null)
            throw new IllegalStateException("Cannot start auction without a deal.");
        this.auction = new Auction();
        this.currentBidder = currentDeal.getDealer();
        this.phase = GamePhase.BIDDING;

        if (view != null) {
            view.showDeal(currentDeal);
            view.showAuction(auction);
            view.showBidPrompt(currentBidder);
        }
    }

    public void submitBid(Bid bid) {
        if (phase != GamePhase.BIDDING)
            throw new IllegalStateException("Cannot submit bid when not in bidding phase.");
        if (bid == null)
            throw new IllegalArgumentException("Bid cannot be null.");
        if (!currentBidder.getPosition().equals(bid.getBidder().getPosition()))
            throw new IllegalArgumentException("It's not the turn of the seat making the bid.");

        boolean added = auction.addBid(bid);
        if (!added) {
            if (view != null) {
                view.showMessage("Invalid bid, please try again");
            }
            return;
        }
        if (view != null) {
            view.showAuction(auction);
        }

        if (auction.isComplete()) {
            this.contract = auction.createContract();
            phase = GamePhase.AUCTION_COMPLETE;
            if (view != null) {
                view.showContract(contract);
                view.showPlayPrompt(contract.getDeclarer());
            }
        } else {
            currentBidder = currentBidder.next();
            if (view != null) {
                view.showBidPrompt(currentBidder);
            }
        }
    }

    public boolean startPlay(Contract completedContract) {
        if (phase != GamePhase.AUCTION_COMPLETE || completedContract == null)
            return false;

        contract = completedContract;
        gameplay = new Gameplay(currentDeal, contract);
        phase = GamePhase.PLAYING;
        this.currentPlayer = gameplay.getCurrentLeader();

        if (view != null) {
            view.showContract(contract);
            view.showGameplayState(gameplay);
            view.showPlayPrompt(currentPlayer);
        }
        return true;
    }

    public boolean playCard(Seat seat, Card card) {
        if (phase != GamePhase.PLAYING) {
            if (view != null) {
                view.showMessage("Game is not in playing phase.");
            }
            return false;
        }
        if (gameplay == null || gameplay.isFinished()) {
            if (view != null) {
                view.showMessage("Game is already finished.");
            }
            return false;
        }

        boolean played = gameplay.playCard(seat, card);
        if (!played) {
            if (view != null) {
                view.showMessage("Invalid card played.");
            }
            return false;
        }

        if (view != null) {
            view.showGameplayState(gameplay);
        }

        if (gameplay.isFinished()) {
            phase = GamePhase.FINISHED;
            int declarerTricks = gameplay.getDeclarerTricks();
            int requiredTricks = contract.getRequiredTricks();
            boolean madeContract = declarerTricks >= requiredTricks;

            if (view != null) {
                view.showMessage(String.format(
                        "Game finished!\nDeclarer made %d tricks (needed %d)\n%s!",
                        declarerTricks, requiredTricks,
                        madeContract ? "CONTRACT MADE" : "CONTRACT FAILED"));
            }
        } else {
            currentPlayer = gameplay.getCurrentLeader();
            if (view != null) {
                view.showPlayPrompt(currentPlayer);
            }
        }
        return true;
    }

    public void undo() {
        // TODO: implement
    }

    public String getCurrentState() {
        return phase.toString();
    }

    public void setView(GameView view) {
        this.view = view;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public Deal getCurrentDeal() {
        return currentDeal;
    }

    public Auction getAuction() {
        return auction;
    }

    public Seat getCurrentBidder() {
        return currentBidder;
    }

    public Seat getCurrentPlayer() {
        return currentPlayer;
    }

    public Contract getContract() {
        return contract;
    }

    public Gameplay getGameplay() {
        return gameplay;
    }

    private boolean isCompleteDeal(Deal deal) {
        if (deal == null || deal.getAllHands().size() != Position.values().length)
            return false;

        for (Position position : Position.values()) {
            Hand hand = deal.getHand(new Seat(position));
            if (hand == null || hand.getCards().size() != 13)
                return false;
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            GameView view = new GameView(controller);
            controller.setView(view);
            view.showMenu();
        });
    }
}