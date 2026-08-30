import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GameView {

    private GameController controller;
    private GameWindow window;
    private boolean windowInitialized = false;
    private ArrayList<LearningNote> learningNotes = new ArrayList<>();

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
            window.addLearningNotesButton();
        }
    }

    public void showMenu() {
        ensureWindow();
        String[] options = new String[] { "Start New Game", "Load Game", "View Learning Notes", "Exit" };
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
        } else if (choice == 1) {
            String gameId = JOptionPane.showInputDialog("Enter Game ID to load:");
            if (gameId != null && !gameId.isEmpty()) {
                controller.loadGame(gameId);
            }
        } else if (choice == 2) {
            showLearningNotes();
        } else {
            System.exit(0);
        }
    }

    public void showLearningNotes() {
        ensureWindow();
        if (learningNotes.isEmpty()) {
            JOptionPane.showMessageDialog(window.getFrame(),
                    "No learning notes have been saved yet.\n\n" +
                            "Tips for taking notes:\n" +
                            "Use keywords like: establish, finesse, duck, ruff, cross-ruff\n" +
                            "Record your bidding decisions\n" +
                            "Note your plan at the start of play\n" +
                            "Reflect on what you learned after the hand",
                    "Learning Notes",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== LEARNING NOTES ===\n");
        sb.append("Total notes: ").append(learningNotes.size()).append("\n\n");

        for (int i = 0; i < learningNotes.size(); i++) {
            LearningNote note = learningNotes.get(i);
            sb.append("Note ").append(i + 1).append(":\n");
            sb.append(note.toString());
            sb.append("\n");

            String[] keywords = note.getKeywordsUsed();
            if (keywords.length > 0) {
                sb.append("  Keywords: ");
                for (String kw : keywords) {
                    sb.append("[").append(kw).append("] ");
                }
                sb.append("\n");
            }
            sb.append("---\n\n");
        }

        JTextArea textArea = new JTextArea(sb.toString(), 30, 50);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(window.getFrame(),
                scrollPane,
                "Learning Notes",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showBiddingNotePrompt(String biddingSequence) {
        ensureWindow();
        String[] options = {
                "Add Bidding Note",
                "Skip"
        };

        int choice = JOptionPane.showOptionDialog(window.getFrame(),
                "Would you like to add a note about your bidding decision?\n" +
                        "Current bidding: " + biddingSequence + "\n\n" +
                        "Tips:\n" +
                        "Why did you choose this bid?\n" +
                        "What were you considering?\n" +
                        "What did the opponents' bids suggest?",
                "Bidding Note",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            showBiddingNoteDialog(biddingSequence);
        }
    }

    private void showBiddingNoteDialog(String biddingSequence) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setPreferredSize(new Dimension(400, 150));

        JLabel label = new JLabel("Enter your bidding note:");
        JTextArea textArea = new JTextArea(4, 30);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);

        String keywords = String.join(", ", LearningNote.KEYWORDS);
        JLabel keywordLabel = new JLabel("<html><i>Suggested keywords: " + keywords + "</i></html>");
        keywordLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        panel.add(label);
        panel.add(scrollPane);

        int result = JOptionPane.showConfirmDialog(window.getFrame(),
                new Object[] { panel, keywordLabel },
                "Add Bidding Note",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String noteText = textArea.getText().trim();
            if (!noteText.isEmpty()) {
                LearningNote note = new LearningNote("BIDDING");
                note.setNoteText(noteText);
                note.setBiddingSequence(biddingSequence);
                learningNotes.add(note);
                String keywordsFound = String.join(", ", note.getKeywordsUsed());
                JOptionPane.showMessageDialog(window.getFrame(),
                        "Note saved! Keywords found: " + (keywordsFound.isEmpty() ? "none" : keywordsFound),
                        "Note Saved",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void showPrePlayPlanPrompt(String contract) {
        ensureWindow();
        String[] options = {
                "Add Play Plan",
                "Skip"
        };

        int choice = JOptionPane.showOptionDialog(window.getFrame(),
                "Enter your plan before playing this hand.\n" +
                        "Contract: " + contract + "\n\n" +
                        "Consider:\n" +
                        "Winners: How many sure tricks?\n" +
                        "Losers: How many potential losers?\n" +
                        "Opportunities: Where can you develop extra tricks?\n" +
                        "Threats: What could go wrong?\n" +
                        "Entries: Can you get back to the right hand?",
                "Play Plan",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            showPrePlayPlanDialog(contract);
        }
    }

    private void showPrePlayPlanDialog(String contract) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.setPreferredSize(new Dimension(500, 300));

        String[] prompts = { "Winners:", "Losers:", "Opportunities:", "Threats:", "Entries:" };
        JTextArea[] textAreas = new JTextArea[prompts.length];

        for (int i = 0; i < prompts.length; i++) {
            JLabel label = new JLabel("#" + prompts[i]);
            gbc.gridx = 0;
            gbc.gridy = i;
            panel.add(label, gbc);

            JTextArea textArea = new JTextArea(2, 20);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textAreas[i] = textArea;
            JScrollPane scrollPane = new JScrollPane(textArea);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            panel.add(scrollPane, gbc);
            gbc.weightx = 0;
        }

        int result = JOptionPane.showConfirmDialog(window.getFrame(),
                panel,
                "Plan for " + contract,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            StringBuilder analysis = new StringBuilder();
            boolean hasContent = false;
            for (int i = 0; i < prompts.length; i++) {
                String text = textAreas[i].getText().trim();
                if (!text.isEmpty()) {
                    if (hasContent)
                        analysis.append(" | ");
                    analysis.append("#").append(prompts[i]).append(" ").append(text);
                    hasContent = true;
                }
            }

            if (hasContent) {
                LearningNote note = new LearningNote("PRE_PLAY");
                note.setContract(contract);
                note.setHandAnalysis(analysis.toString());
                learningNotes.add(note);
                JOptionPane.showMessageDialog(window.getFrame(),
                        "Plan saved! You can review it in Learning Notes.",
                        "Plan Saved",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void showPostPlayNotePrompt(String contract, int tricksWon, int tricksNeeded) {
        ensureWindow();
        boolean madeContract = tricksWon >= tricksNeeded;
        String result = madeContract ? "Contract made!" : "Contract failed";

        String[] options = {
                "Add Reflection",
                "Skip"
        };

        int choice = JOptionPane.showOptionDialog(window.getFrame(),
                "Game finished!\n" +
                        "Contract: " + contract + "\n" +
                        "Tricks won: " + tricksWon + " (needed " + tricksNeeded + ")\n" +
                        "Result: " + result + "\n\n" +
                        "Take a moment to reflect on what you learned:",
                "Post-Play Reflection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            showPostPlayNoteDialog(contract, tricksWon, tricksNeeded);
        }
    }

    private void showPostPlayNoteDialog(String contract, int tricksWon, int tricksNeeded) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(400, 200));

        JLabel label = new JLabel("What did you learn from this hand?");
        JTextArea textArea = new JTextArea(6, 30);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);

        String keywords = String.join(", ", LearningNote.KEYWORDS);
        JLabel keywordLabel = new JLabel("<html><i>Suggested keywords: " + keywords + "</i></html>");
        keywordLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(keywordLabel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(window.getFrame(),
                panel,
                "Reflection",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String noteText = textArea.getText().trim();
            if (!noteText.isEmpty()) {
                LearningNote note = new LearningNote("POST_PLAY");
                note.setNoteText(noteText);
                note.setContract(contract);
                note.setTrickNumber(13);
                learningNotes.add(note);
                String keywordsFound = String.join(", ", note.getKeywordsUsed());
                JOptionPane.showMessageDialog(window.getFrame(),
                        "Reflection saved! Keywords found: " + (keywordsFound.isEmpty() ? "none" : keywordsFound),
                        "Reflection Saved",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void showQuickNotePrompt(String phase) {
        ensureWindow();

        String[] options = {
                "Add Note",
                "Cancel"
        };

        String title = "Quick Note";
        String message = "Add a note about the current situation:";

        if (phase.equals("BIDDING")) {
            title = "Bidding Note";
            message = "Add a note about the bidding:";
        } else if (phase.equals("PLAYING")) {
            title = "Play Note";
            message = "Add a note about the current play:";
        }

        int choice = JOptionPane.showOptionDialog(window.getFrame(),
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            showQuickNoteDialog(phase);
        }
    }

    private void showQuickNoteDialog(String phase) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(400, 150));

        JLabel label = new JLabel("Enter your note:");
        JTextArea textArea = new JTextArea(4, 30);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);

        String keywords = String.join(", ", LearningNote.KEYWORDS);
        JLabel keywordLabel = new JLabel("<html><i>Suggested keywords: " + keywords + "</i></html>");
        keywordLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(keywordLabel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(window.getFrame(),
                panel,
                "Quick Note",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String noteText = textArea.getText().trim();
            if (!noteText.isEmpty()) {
                LearningNote note = new LearningNote(phase);
                note.setNoteText(noteText);

                if (controller.getContract() != null) {
                    note.setContract(controller.getContract().getLevel() + " " +
                            controller.getContract().getDenomination());
                }
                if (controller.getGameplay() != null) {
                    note.setTrickNumber(controller.getGameplay().getTrickNo());
                }

                learningNotes.add(note);
                String keywordsFound = String.join(", ", note.getKeywordsUsed());
                JOptionPane.showMessageDialog(window.getFrame(),
                        "Note saved! Keywords found: " + (keywordsFound.isEmpty() ? "none" : keywordsFound),
                        "Note Saved",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public ArrayList<LearningNote> getLearningNotes() {
        return learningNotes;
    }

    public void onQuickNote(String phase) {
        showQuickNotePrompt(phase);
    }

    public void onShowLearningNotes() {
        showLearningNotes();
    }

    public void onBidNote() {
        if (controller.getAuction() != null) {
            String sequence = getBiddingSequence();
            showBiddingNotePrompt(sequence);
        } else {
            showQuickNotePrompt("BIDDING");
        }
    }

    private String getBiddingSequence() {
        if (controller.getAuction() == null || controller.getAuction().getBids().isEmpty()) {
            return "No bids yet";
        }
        StringBuilder sb = new StringBuilder();
        for (Bid bid : controller.getAuction().getBids()) {
            if (bid.isPass()) {
                sb.append(bid.getBidder()).append(" Pass ");
            } else if (bid.isDouble()) {
                sb.append(bid.getBidder()).append(" X ");
            } else if (bid.isRedouble()) {
                sb.append(bid.getBidder()).append(" XX ");
            } else {
                sb.append(bid.getBidder()).append(" ").append(bid.getLevel()).append(bid.getDenomination()).append(" ");
            }
        }
        return sb.toString();
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

        StringBuilder text = new StringBuilder();
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

    public void showGameplayState(Gameplay gameplay) {
        ensureWindow();
        if (gameplay == null)
            return;

        List<Trick> tricks = gameplay.getTricks();
        if (!tricks.isEmpty()) {
            Trick currentTrick = tricks.get(tricks.size() - 1);
            if (currentTrick != null) {
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

    public void showBidPrompt(Seat currentBidder) {
        ensureWindow();
        window.setStatusText("Bidding - " + currentBidder + "'s turn");
        window.showBiddingButtons(currentBidder);
    }

    public void showPlayPrompt(Seat currentPlayer) {
        ensureWindow();
        window.setStatusText("Playing - " + currentPlayer + "'s turn");

        if (controller.getPhase() == GamePhase.PLAYING) {
            Hand hand = controller.getCurrentDeal().getHand(currentPlayer);
            Gameplay gameplay = controller.getGameplay();
            if (hand != null && gameplay != null) {
                ArrayList<Card> legalCards = gameplay.getLegalCards(currentPlayer, hand);
                window.setCurrentPlayer(currentPlayer);
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
            Contract contract = controller.getContract();
            controller.startPlay(contract);
            if (contract != null) {
                showPrePlayPlanPrompt(contract.getLevel() + " " + contract.getDenomination());
            }
        }
    }

    public GameController getController() {
        return controller;
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