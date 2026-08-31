import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
// ============================================================
// Portions of this class are AI-generated and not yet fully
// reviewed or understood by Cebolenkosi. See flagged methods
// below. Do not treat as final - needs team review before merge.
// ============================================================
public class GameWindow {

    private JFrame mainFrame;
    private JPanel tablePanel, trickPanel;
    private JPanel northPanel, eastPanel, southPanel, westPanel;
    private JLabel infoLabel, declarerLabel, contractLabel, statusLabel;
    private JLabel trickLabel, scoreLabel;
    private JTextArea auctionArea;
    private JPanel biddingPanel;
    private JPanel headerPanel;
    private GameView view;
    private Seat currentDeclarer;
    private Seat currentPlayer;

    private static final Color FELT_GREEN = new Color(27, 77, 62);
    private static final Color GOLD_ACCENT = new Color(201, 162, 39);
    private static final Color DECLARER_COLOR = new Color(255, 215, 0);
    private static final Color DUMMY_COLOR = new Color(100, 200, 255);

    private static final int CARD_WIDTH = 90;
    private static final int CARD_HEIGHT = 130;

    private boolean useImageCards = true;

    public GameWindow(GameView view) {
        this.view = view;
        this.currentDeclarer = null;
        this.currentPlayer = null;
        if (useImageCards) {
            CardImageLoader.loadCardImages();
        }
        initialize();
    }

    // AI-generated - GridBagLayout table structure below (player panel
    // positioning, right-side auction panel, control buttons) not yet
    // verified by me line-by-line.

    private void initialize() {
        mainFrame = new JFrame("BridgeTut");
        mainFrame.setSize(1600, 1000);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout(10, 10));
        mainFrame.getContentPane().setBackground(new Color(0, 60, 40));

        // ===== Header =====
        headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        infoLabel = new JLabel("Dealer: -  Vulnerability: -");
        infoLabel.setForeground(GOLD_ACCENT);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topHeader.add(infoLabel, BorderLayout.WEST);

        declarerLabel = new JLabel("Declarer: -");
        declarerLabel.setForeground(GOLD_ACCENT);
        declarerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topHeader.add(declarerLabel, BorderLayout.CENTER);

        contractLabel = new JLabel("Contract: -");
        contractLabel.setForeground(GOLD_ACCENT);
        contractLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topHeader.add(contractLabel, BorderLayout.EAST);

        JPanel bottomHeader = new JPanel(new BorderLayout());
        bottomHeader.setOpaque(false);

        statusLabel = new JLabel("Welcome to BridgeTut!");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        bottomHeader.add(statusLabel, BorderLayout.WEST);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scorePanel.setOpaque(false);
        scoreLabel = new JLabel("Tricks: NS: 0  EW: 0");
        scoreLabel.setForeground(GOLD_ACCENT);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scorePanel.add(scoreLabel);
        trickLabel = new JLabel("Trick: 0/13");
        trickLabel.setForeground(GOLD_ACCENT);
        trickLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scorePanel.add(trickLabel);
        bottomHeader.add(scorePanel, BorderLayout.EAST);

        headerPanel.add(topHeader);
        headerPanel.add(bottomHeader);
        mainFrame.add(headerPanel, BorderLayout.NORTH);

        // ===== Table Panel with GridBagLayout =====
        tablePanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(FELT_GREEN);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // ===== Create player panels =====
        northPanel = createPlayerPanel("North", Position.NORTH);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        gbc.anchor = GridBagConstraints.CENTER;
        tablePanel.add(northPanel, gbc);

        westPanel = createPlayerPanel("West", Position.WEST);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 0.15;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        tablePanel.add(westPanel, gbc);

        trickPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        trickPanel.setOpaque(false);
        trickPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        tablePanel.add(trickPanel, gbc);

        eastPanel = createPlayerPanel("East", Position.EAST);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 0.15;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        tablePanel.add(eastPanel, gbc);

        southPanel = createPlayerPanel("South", Position.SOUTH);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        gbc.anchor = GridBagConstraints.CENTER;
        tablePanel.add(southPanel, gbc);

        mainFrame.add(tablePanel, BorderLayout.CENTER);

        // ===== Right panel =====
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.setPreferredSize(new Dimension(320, 0));

        auctionArea = new JTextArea(25, 25);
        auctionArea.setEditable(false);
        auctionArea.setBackground(new Color(0, 40, 30));
        auctionArea.setForeground(Color.WHITE);
        auctionArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane auctionScroll = new JScrollPane(auctionArea);
        auctionScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD_ACCENT),
                "Auction",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                GOLD_ACCENT));
        auctionScroll.getViewport().setBackground(new Color(0, 40, 30));
        rightPanel.add(auctionScroll, BorderLayout.CENTER);

        // Control buttons
        JPanel controlPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        controlPanel.setOpaque(false);

        JButton startPlayBtn = new JButton("Start Playing");
        startPlayBtn.setBackground(GOLD_ACCENT);
        startPlayBtn.setForeground(Color.BLACK);
        startPlayBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startPlayBtn.setPreferredSize(new Dimension(250, 45));
        startPlayBtn.addActionListener(e -> {
            if (view != null) {
                view.onStartPlay();
            }
        });
        controlPanel.add(startPlayBtn);

        JButton newGameBtn = new JButton("New Game");
        newGameBtn.setBackground(GOLD_ACCENT);
        newGameBtn.setForeground(Color.BLACK);
        newGameBtn.setFont(new Font("Arial", Font.BOLD, 14));
        newGameBtn.setPreferredSize(new Dimension(250, 45));
        newGameBtn.addActionListener(e -> {
            if (view != null) {
                view.showMenu();
            }
        });
        controlPanel.add(newGameBtn);

        biddingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        biddingPanel.setOpaque(false);
        biddingPanel.setPreferredSize(new Dimension(250, 120));
        controlPanel.add(biddingPanel);

        rightPanel.add(controlPanel, BorderLayout.SOUTH);

        mainFrame.add(rightPanel, BorderLayout.EAST);

        mainFrame.setVisible(true);
    }

    // ===== NEW: Add learning notes button to header =====
    public void addLearningNotesButton() {
        if (headerPanel == null)
            return;

        // Find the bottom header panel
        JPanel bottomHeader = null;
        for (Component comp : headerPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getLayout() instanceof BorderLayout) {
                    bottomHeader = panel;
                    break;
                }
            }
        }

        if (bottomHeader == null)
            return;

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setOpaque(false);

        // Learning Notes button
        JButton notesBtn = new JButton("Learning Notes");
        notesBtn.setBackground(new Color(200, 180, 50));
        notesBtn.setForeground(Color.BLACK);
        notesBtn.setFont(new Font("Arial", Font.BOLD, 12));
        notesBtn.setPreferredSize(new Dimension(130, 30));
        notesBtn.addActionListener(e -> {
            if (view != null) {
                view.onShowLearningNotes();
            }
        });
        buttonPanel.add(notesBtn);

        // Quick Note button - available anytime
        JButton quickNoteBtn = new JButton("Quick Note");
        quickNoteBtn.setBackground(new Color(100, 150, 200));
        quickNoteBtn.setForeground(Color.WHITE);
        quickNoteBtn.setFont(new Font("Arial", Font.BOLD, 12));
        quickNoteBtn.setPreferredSize(new Dimension(110, 30));
        quickNoteBtn.addActionListener(e -> {
            if (view != null) {
                String phase = "GENERAL";
                if (view.getController() != null) {
                    GamePhase gamePhase = view.getController().getPhase();
                    if (gamePhase == GamePhase.BIDDING) {
                        phase = "BIDDING";
                    } else if (gamePhase == GamePhase.PLAYING || gamePhase == GamePhase.AUCTION_COMPLETE) {
                        phase = "PLAYING";
                    }
                }
                view.onQuickNote(phase);
            }
        });
        buttonPanel.add(quickNoteBtn);

        // Add to the EAST side of bottom header
        bottomHeader.add(buttonPanel, BorderLayout.EAST);
        bottomHeader.revalidate();
        bottomHeader.repaint();
    }

    // ===== Creates a player panel =====
    private JPanel createPlayerPanel(String name, Position position) {
        JPanel wrapper = new JPanel(new BorderLayout(5, 5));
        wrapper.setOpaque(false);

        JLabel nameLabel = new JLabel(name + ":");
        nameLabel.setForeground(GOLD_ACCENT);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        wrapper.add(nameLabel, BorderLayout.WEST);

        JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        cardPanel.setOpaque(false);
        cardPanel.setName(position.toString());

        JScrollPane scrollPane = new JScrollPane(cardPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        if (position == Position.NORTH || position == Position.SOUTH) {
            scrollPane.setPreferredSize(new Dimension(900, 130));
        } else {
            scrollPane.setPreferredSize(new Dimension(150, 300));
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        }

        wrapper.add(scrollPane, BorderLayout.CENTER);
        wrapper.putClientProperty("cardPanel", cardPanel);
        wrapper.putClientProperty("position", position);

        return wrapper;
    }

    private JPanel getCardPanel(JPanel wrapper) {
        return (JPanel) wrapper.getClientProperty("cardPanel");
    }

    // AI-generated - real click-to-play card buttons. Replaces the
    // JOptionPane-based getUserCardToPlay() dialog approach built
    // earlier. Not yet reviewed.

    // ===== Creates card button =====
    private JButton createCardButton(Card card, Seat player) {
        JButton button;

        if (useImageCards) {
            Image image = CardImageLoader.getScaledCardImage(
                    card.getSuit(),
                    card.getRank(),
                    CARD_WIDTH,
                    CARD_HEIGHT);
            button = new JButton(new ImageIcon(image));
        } else {
            button = new JButton(card.toString());
            Suit suit = card.getSuit();
            if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) {
                button.setBackground(Color.RED);
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(Color.BLACK);
                button.setForeground(Color.WHITE);
            }
            button.setFont(new Font("Arial", Font.BOLD, 16));
        }

        button.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        button.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        button.setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        button.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.YELLOW, 3),
                        BorderFactory.createEmptyBorder(3, 3, 3, 3)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
            }
        });

        button.addActionListener(e -> {
            if (view != null) {
                view.onCardSelected(player, card);
            }
        });

        return button;
    }

    // ===== Creates card label =====
    private JLabel createCardLabel(Card card, boolean isDummy) {
        JLabel label;

        if (useImageCards) {
            Image image = CardImageLoader.getScaledCardImage(
                    card.getSuit(),
                    card.getRank(),
                    CARD_WIDTH,
                    CARD_HEIGHT);
            label = new JLabel(new ImageIcon(image));
        } else {
            label = new JLabel(card.toString());
            label.setOpaque(true);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

            Suit suit = card.getSuit();
            if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) {
                label.setBackground(Color.RED);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.BLACK);
                label.setForeground(Color.WHITE);
            }
        }

        label.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));

        if (isDummy) {
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DUMMY_COLOR, 3),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        } else {
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        }

        return label;
    }

    public void updateHand(Position position, Hand hand) {
        JPanel wrapper;

        switch (position) {
            case NORTH:
                wrapper = northPanel;
                break;
            case EAST:
                wrapper = eastPanel;
                break;
            case SOUTH:
                wrapper = southPanel;
                break;
            case WEST:
                wrapper = westPanel;
                break;
            default:
                return;
        }

        JPanel cardPanel = getCardPanel(wrapper);
        if (cardPanel == null)
            return;

        cardPanel.removeAll();

        boolean isDummy = false;
        if (currentDeclarer != null) {
            Seat partner = currentDeclarer.opposite();
            if (partner.getPosition() == position) {
                isDummy = true;
            }
        }

        boolean isUser = (position == Position.SOUTH);
        boolean isEastWest = (position == Position.EAST || position == Position.WEST);

        if (hand != null) {
            for (Card card : hand.getCards()) {
                if (isUser) {
                    JButton cardBtn = createCardButton(card, new Seat(position));
                    if (isEastWest) {
                        cardBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                    }
                    cardPanel.add(cardBtn);
                } else {
                    JLabel cardLabel = createCardLabel(card, isDummy);
                    if (isEastWest) {
                        cardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    }
                    cardPanel.add(cardLabel);
                }
            }
        }

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    public void updateInfo(Seat dealer, Vulnerability vulnerability) {
        infoLabel.setText("Dealer: " + dealer + "  Vulnerability: " + vulnerability);
    }

    public void updateDeclarer(Seat declarer) {
        this.currentDeclarer = declarer;
        declarerLabel.setText("Declarer: " + (declarer != null ? declarer + " (D)" : "-"));
    }

    public void updateContract(Contract contract) {
        if (contract == null) {
            contractLabel.setText("Contract: -");
            return;
        }
        String text = "Contract: " + contract.getLevel() + " " + contract.getDenomination();
        if (contract.isDoubled())
            text += " X";
        if (contract.isRedoubled())
            text += " XX";
        contractLabel.setText(text);
        updateDeclarer(contract.getDeclarer());
    }

    public void updateTrick(List<Card> trick) {
        trickPanel.removeAll();
        if (trick != null) {
            for (Card card : trick) {
                JLabel cardLabel = createCardLabel(card, false);
                trickPanel.add(cardLabel);
            }
        }
        trickPanel.revalidate();
        trickPanel.repaint();
    }

    public void updateTrickNumber(int trickNo) {
        trickLabel.setText("Trick: " + trickNo + "/13");
    }

    public void updateScore(int declarerTricks, int defenderTricks) {
        scoreLabel.setText("Tricks: NS: " + declarerTricks + "  EW: " + defenderTricks);
    }

    public void setStatusText(String text) {
        statusLabel.setText(text);
    }

    public void setAuctionText(String text) {
        auctionArea.setText(text);
        auctionArea.setCaretPosition(auctionArea.getDocument().getLength());
    }
    // AI-generated - event-driven bidding UI (calls view.onBidSelected()
// via button listeners). Different architecture from getUserBid(),
// which used a blocking JOptionPane dialog. Not yet reviewed - these
// two approaches currently conflict and can't both be wired in.
    public void showBiddingButtons(Seat currentBidder) {
        biddingPanel.removeAll();
        biddingPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JButton passBtn = new JButton("PASS");
        passBtn.setBackground(Color.GRAY);
        passBtn.setForeground(Color.WHITE);
        passBtn.setFont(new Font("Arial", Font.BOLD, 14));
        passBtn.setPreferredSize(new Dimension(70, 35));
        passBtn.addActionListener(e -> {
            if (view != null) {
                view.onBidSelected(Bid.BidPass(currentBidder));
            }
        });
        biddingPanel.add(passBtn);

        String[] bidLabels = { "1C", "1D", "1H", "1S", "1NT", "2C", "2D", "2H", "2S", "2NT" };
        for (String label : bidLabels) {
            JButton bidBtn = new JButton(label);
            bidBtn.setBackground(GOLD_ACCENT);
            bidBtn.setForeground(Color.BLACK);
            bidBtn.setFont(new Font("Arial", Font.BOLD, 14));
            bidBtn.setPreferredSize(new Dimension(55, 35));
            bidBtn.addActionListener(e -> {
                if (view != null) {
                    int level = Integer.parseInt(label.substring(0, 1));
                    String suitStr = label.substring(1);
                    Suit suit;
                    if (suitStr.equals("NT"))
                        suit = Suit.NT;
                    else if (suitStr.equals("C"))
                        suit = Suit.CLUBS;
                    else if (suitStr.equals("D"))
                        suit = Suit.DIAMONDS;
                    else if (suitStr.equals("H"))
                        suit = Suit.HEARTS;
                    else
                        suit = Suit.SPADES;
                    view.onBidSelected(new Bid(currentBidder, level, suit));
                }
            });
            biddingPanel.add(bidBtn);
        }

        biddingPanel.revalidate();
        biddingPanel.repaint();
    }

    public void showCardButtons(Seat currentPlayer, ArrayList<Card> legalCards) {
        this.currentPlayer = currentPlayer;
        setStatusText("Playing - " + currentPlayer + "'s turn");
    }

    public void setCurrentPlayer(Seat player) {
        this.currentPlayer = player;
        if (player != null) {
            setStatusText(player + "'s turn");
        }
    }

    public JFrame getFrame() {
        return mainFrame;
    }
}