import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.TitledBorder;

public class GameWindow {

    private JFrame mainFrame;
    private JPanel tablePanel, trickPanel;
    private JPanel northPanel, eastPanel, southPanel, westPanel;
    private JLabel infoLabel, declarerLabel, contractLabel, statusLabel;
    private JLabel trickLabel, scoreLabel;
    private JTextArea auctionArea;
    private JPanel biddingPanel, cardPanel;
    private GameView view;

    private static final Color FELT_GREEN = new Color(27, 77, 62);
    private static final Color GOLD_ACCENT = new Color(201, 162, 39);

    public GameWindow(GameView view) {
        this.view = view;
        initialize();
    }

    private void initialize() {
        mainFrame = new JFrame("BridgeTut");
        mainFrame.setSize(1000, 750);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout(10, 10));
        mainFrame.getContentPane().setBackground(new Color(0, 60, 40));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        infoLabel = new JLabel("Dealer: -  Vulnerability: -");
        infoLabel.setForeground(GOLD_ACCENT);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topHeader.add(infoLabel, BorderLayout.WEST);

        declarerLabel = new JLabel("Declarer: -");
        declarerLabel.setForeground(GOLD_ACCENT);
        declarerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topHeader.add(declarerLabel, BorderLayout.CENTER);

        contractLabel = new JLabel("Contract: -");
        contractLabel.setForeground(GOLD_ACCENT);
        contractLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topHeader.add(contractLabel, BorderLayout.EAST);

        JPanel bottomHeader = new JPanel(new BorderLayout());
        bottomHeader.setOpaque(false);

        statusLabel = new JLabel("Welcome to BridgeTut!");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        bottomHeader.add(statusLabel, BorderLayout.WEST);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scorePanel.setOpaque(false);
        scoreLabel = new JLabel("Tricks: NS: 0  EW: 0");
        scoreLabel.setForeground(GOLD_ACCENT);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 12));
        scorePanel.add(scoreLabel);
        trickLabel = new JLabel("Trick: 0/13");
        trickLabel.setForeground(GOLD_ACCENT);
        trickLabel.setFont(new Font("Arial", Font.BOLD, 12));
        scorePanel.add(trickLabel);
        bottomHeader.add(scorePanel, BorderLayout.EAST);

        headerPanel.add(topHeader);
        headerPanel.add(bottomHeader);
        mainFrame.add(headerPanel, BorderLayout.NORTH);

        // Center - Table
        tablePanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(FELT_GREEN);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(GOLD_ACCENT);
                g2d.setStroke(new BasicStroke(3));
                int margin = 30;
                g2d.drawOval(margin, margin, getWidth() - 2 * margin, getHeight() - 2 * margin);
            }
        };
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        northPanel = createPlayerPanel("North");
        eastPanel = createPlayerPanel("East");
        southPanel = createPlayerPanel("South");
        westPanel = createPlayerPanel("West");

        tablePanel.add(northPanel, BorderLayout.NORTH);
        tablePanel.add(eastPanel, BorderLayout.EAST);
        tablePanel.add(southPanel, BorderLayout.SOUTH);
        tablePanel.add(westPanel, BorderLayout.WEST);

        trickPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        trickPanel.setOpaque(false);
        trickPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        tablePanel.add(trickPanel, BorderLayout.CENTER);

        mainFrame.add(tablePanel, BorderLayout.CENTER);

        // Right panel - Auction and Controls
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rightPanel.setPreferredSize(new Dimension(250, 0));

        auctionArea = new JTextArea(15, 20);
        auctionArea.setEditable(false);
        auctionArea.setBackground(new Color(0, 40, 30));
        auctionArea.setForeground(Color.WHITE);
        auctionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane auctionScroll = new JScrollPane(auctionArea);
        auctionScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GOLD_ACCENT),
                "Auction",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12),
                GOLD_ACCENT));
        auctionScroll.getViewport().setBackground(new Color(0, 40, 30));
        rightPanel.add(auctionScroll, BorderLayout.CENTER);

        // Control buttons
        JPanel controlPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        controlPanel.setOpaque(false);

        JButton startPlayBtn = new JButton("Start Playing");
        startPlayBtn.setBackground(GOLD_ACCENT);
        startPlayBtn.setForeground(Color.BLACK);
        startPlayBtn.setFont(new Font("Arial", Font.BOLD, 12));
        startPlayBtn.addActionListener(e -> {
            if (view != null) {
                view.onStartPlay();
            }
        });
        controlPanel.add(startPlayBtn);

        JButton newGameBtn = new JButton("New Game");
        newGameBtn.setBackground(GOLD_ACCENT);
        newGameBtn.setForeground(Color.BLACK);
        newGameBtn.setFont(new Font("Arial", Font.BOLD, 12));
        newGameBtn.addActionListener(e -> {
            if (view != null) {
                view.showMenu();
            }
        });
        controlPanel.add(newGameBtn);

        biddingPanel = new JPanel(new FlowLayout());
        biddingPanel.setOpaque(false);
        controlPanel.add(biddingPanel);

        rightPanel.add(controlPanel, BorderLayout.SOUTH);

        cardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cardPanel.setPreferredSize(new Dimension(0, 80));
        mainFrame.add(cardPanel, BorderLayout.SOUTH);

        mainFrame.setVisible(true);
    }

    private JPanel createPlayerPanel(String name) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        panel.setOpaque(false);

        JLabel nameLabel = new JLabel(name + ":");
        nameLabel.setForeground(GOLD_ACCENT);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(nameLabel);

        return panel;
    }

    private JLabel createCardLabel(Card card) {
        JLabel label = new JLabel(card.toString());
        label.setOpaque(true);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(50, 30));

        Suit suit = card.getSuit();
        if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) {
            label.setBackground(Color.RED);
            label.setForeground(Color.WHITE);
        } else {
            label.setBackground(Color.BLACK);
            label.setForeground(Color.WHITE);
        }
        return label;
    }

    public void updateHand(Position position, Hand hand) {
        JPanel target;
        String name;
        switch (position) {
            case NORTH:
                target = northPanel;
                name = "North:";
                break;
            case EAST:
                target = eastPanel;
                name = "East:";
                break;
            case SOUTH:
                target = southPanel;
                name = "South:";
                break;
            case WEST:
                target = westPanel;
                name = "West:";
                break;
            default:
                return;
        }

        target.removeAll();

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(GOLD_ACCENT);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        target.add(nameLabel);

        if (hand != null) {
            for (Card card : hand.getCards()) {
                JLabel cardLabel = createCardLabel(card);
                target.add(cardLabel);
            }
        }

        target.revalidate();
        target.repaint();
    }

    public void updateInfo(Seat dealer, Vulnerability vulnerability) {
        infoLabel.setText("Dealer: " + dealer + "  Vulnerability: " + vulnerability);
    }

    public void updateDeclarer(Seat declarer) {
        declarerLabel.setText("Declarer: " + (declarer != null ? declarer.toString() : "-"));
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
    }

    public void updateTrick(List<Card> trick) {
        trickPanel.removeAll();
        if (trick != null) {
            for (Card card : trick) {
                JLabel cardLabel = createCardLabel(card);
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

    public void showBiddingButtons(Seat currentBidder) {
        biddingPanel.removeAll();
        biddingPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JButton passBtn = new JButton("Pass");
        passBtn.setBackground(Color.GRAY);
        passBtn.setForeground(Color.WHITE);
        passBtn.setFont(new Font("Arial", Font.BOLD, 10));
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
            bidBtn.setFont(new Font("Arial", Font.BOLD, 10));
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

    public void showCardButtons(Seat currentPlayer, List<Card> legalCards) {
        cardPanel.removeAll();
        cardPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        if (legalCards != null) {
            for (Card card : legalCards) {
                JButton cardBtn = new JButton(card.toString());
                Suit suit = card.getSuit();
                if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) {
                    cardBtn.setBackground(Color.RED);
                    cardBtn.setForeground(Color.WHITE);
                } else {
                    cardBtn.setBackground(Color.BLACK);
                    cardBtn.setForeground(Color.WHITE);
                }
                cardBtn.setFont(new Font("Arial", Font.BOLD, 12));
                cardBtn.setPreferredSize(new Dimension(60, 35));
                cardBtn.addActionListener(e -> {
                    if (view != null) {
                        view.onCardSelected(currentPlayer, card);
                    }
                });
                cardPanel.add(cardBtn);
            }
        }

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    public JFrame getFrame() {
        return mainFrame;
    }
}