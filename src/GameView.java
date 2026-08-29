import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GameView {

    private GameController controller;
    private GameWindow window;
    private boolean windowInitialized = false;

    public GameView(GameController controller) {
        this.controller = controller;
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    private void ensureWindow() {
        if (!windowInitialized) {
            window = new GameWindow(this);
            windowInitialized = true;
        }
    }

    public void showMenu() {
        ensureWindow();
        String[] options = new String[] { "Start New Game", "Exit" };
        int choice = JOptionPane.showOptionDialog(window.getFrame(),
                "Welcome to BridgeTut!\nChoose an option:",
                "Main Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            controller.startNewGame();
        } else {
            System.exit(0);
        }
    }

    public void showDeal(Deal deal) {
        ensureWindow();
        if (deal == null)
            return;

        window.updateInfo(deal.getDealer(), deal.getVulnerability());
        for (Position position : Position.values()) {
            Seat seat = new Seat(position);
            Hand hand = deal.getHand(seat);
            if (hand != null) {
                window.updateHand(position, hand);
            }
        }
    }

    public void showAuction(Auction auction) {
        ensureWindow();
        if (auction == null)
            return;

        StringBuilder text = new StringBuilder("Auction:\n");
        for (Bid bid : auction.getBids()) {
            if (bid.isPass()) {
                text.append(bid.getBidder()).append(" Pass\n");
            } else if (bid.isDouble()) {
                text.append(bid.getBidder()).append(" Double\n");
            } else if (bid.isRedouble()) {
                text.append(bid.getBidder()).append(" Redouble\n");
            } else {
                text.append(bid.getBidder()).append(" bids ").append(bid.getLevel())
                        .append(" ").append(bid.getDenomination()).append("\n");
            }
        }
        window.setAuctionText(text.toString());
    }

    public void showContract(Contract contract) {
        ensureWindow();
        if (contract == null)
            return;

        window.updateDeclarer(contract.getDeclarer());
        window.updateContract(contract);
    }

    // NEW METHOD - Add this!
    public void showGameplayState(Gameplay gameplay) {
        ensureWindow();
        if (gameplay == null)
            return;

        // Update trick display
        List<Trick> tricks = gameplay.getTricks();
        if (!tricks.isEmpty()) {
            Trick currentTrick = tricks.get(tricks.size() - 1);
            if (currentTrick != null) {
                // Get the plays as a list of cards in order
                List<Card> trickCards = new ArrayList<>();
                Seat leader = gameplay.getCurrentLeader();
                Seat current = leader;
                for (int i = 0; i < 4; i++) {
                    if (currentTrick.getPlays().containsKey(current)) {
                        trickCards.add(currentTrick.getPlays().get(current));
                    }
                    current = current.next();
                }
                if (!trickCards.isEmpty()) {
                    window.updateTrick(trickCards);
                }
            }
        }

        window.updateTrickNumber(gameplay.getTrickNo());
        window.updateScore(gameplay.getDeclarerTricks(), gameplay.getDefenderTricks());
    }

    // NEW METHOD - Add this!
    public void showBidPrompt(Seat currentBidder) {
        ensureWindow();
        window.setStatusText("Bidding - " + currentBidder + "'s turn");
        window.showBiddingButtons(currentBidder);
    }

    // NEW METHOD - Add this!
    public void showPlayPrompt(Seat currentPlayer) {
        ensureWindow();
        window.setStatusText("Playing - " + currentPlayer + "'s turn");

        if (controller.getPhase() == GamePhase.PLAYING) {
            Hand hand = controller.getCurrentDeal().getHand(currentPlayer);
            Gameplay gameplay = controller.getGameplay();
            if (hand != null && gameplay != null) {
                ArrayList<Card> legalCards = gameplay.getLegalCards(currentPlayer, hand);
                window.showCardButtons(currentPlayer, legalCards);
            }
        }
    }

    public void showTrick(int trickNo, List<Card> trick) {
        ensureWindow();
        if (trick != null) {
            window.updateTrick(trick);
        }
        window.updateTrickNumber(trickNo);
    }

    public void showScores(int declarerTricks, int defenderTricks) {
        ensureWindow();
        window.updateScore(declarerTricks, defenderTricks);
    }

    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message);
        });
    }

    // Callback methods for GUI actions
    public void onBidSelected(Bid bid) {
        try {
            controller.submitBid(bid);
        } catch (Exception e) {
            showMessage("Error: " + e.getMessage());
        }
    }

    public void onCardSelected(Seat seat, Card card) {
        try {
            controller.playCard(seat, card);
        } catch (Exception e) {
            showMessage("Error: " + e.getMessage());
        }
    }

    public void onStartPlay() {
        if (controller.getPhase() == GamePhase.AUCTION_COMPLETE) {
            controller.startPlay(controller.getContract());
        }
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