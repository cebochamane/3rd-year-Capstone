import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GameWindow {

    private JFrame mainFrame;
    private JPanel headerBar, tablePanel, trickPanel, notesPanel;
    private JLabel northLabel, eastLabel, southLabel, westLabel;
    private JPanel northPanel, eastPanel, southPanel, westPanel;
    private JLabel infoLabel, scoreLabel, declarerLabel, notesPlaceholderLabel;
    private JLabel trickLabel;
    private JLabel contractLabel;
    private JLabel trumpLabel;
    private JLabel leadLabel;

    private static final Color FELT_GREEN = new Color(27, 77, 62);
    private static final Color GOLD_ACCENT = new Color(201, 162, 39);

    public GameWindow() {
        mainFrame = new JFrame("BridgeTut");
        mainFrame.setSize(900, 700);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);

        infoLabel = new JLabel("Dealer: -  Vulnerability: -");
        infoLabel.setForeground(GOLD_ACCENT);
        headerBar.add(infoLabel, BorderLayout.WEST);

        declarerLabel = new JLabel("Declarer: -");
        declarerLabel.setForeground(GOLD_ACCENT);
        headerBar.add(declarerLabel, BorderLayout.CENTER);

        JPanel contractPanel = new JPanel(new FlowLayout());
        contractPanel.setOpaque(false);
        contractLabel = new JLabel("Contract: -");
        contractLabel.setForeground(GOLD_ACCENT);
        contractPanel.add(contractLabel);

        trumpLabel = new JLabel("Trump: -");
        trumpLabel.setForeground(GOLD_ACCENT);
        contractPanel.add(trumpLabel);

        leadLabel = new JLabel("Lead: -");
        leadLabel.setForeground(GOLD_ACCENT);
        contractPanel.add(leadLabel);

        notesPanel = new JPanel(new BorderLayout());
        notesPanel.setOpaque(false);
        notesPlaceholderLabel = new JLabel("Notes: -");
        notesPlaceholderLabel.setForeground(Color.WHITE);
        notesPanel.add(notesPlaceholderLabel);
        headerBar.add(notesPanel, BorderLayout.EAST);

        mainFrame.add(headerBar, BorderLayout.PAGE_START);

        scoreLabel = new JLabel("NS: 0   EW: 0");
        scoreLabel.setForeground(GOLD_ACCENT);
        mainFrame.add(scoreLabel, BorderLayout.PAGE_END);

        tablePanel = new JPanel(new BorderLayout()) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(FELT_GREEN);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(GOLD_ACCENT);
                g2d.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
            }
        };
        tablePanel.setOpaque(false);

        northPanel = new JPanel();
        northPanel.setOpaque(false);
        northLabel = new JLabel("North: -");
        northLabel.setForeground(Color.WHITE);
        northPanel.add(northLabel);

        eastPanel = new JPanel();
        eastPanel.setOpaque(false);
        eastLabel = new JLabel("East: -");
        eastLabel.setForeground(Color.WHITE);
        eastPanel.add(eastLabel);

        southPanel = new JPanel();
        southPanel.setOpaque(false);
        southLabel = new JLabel("South: -");
        southLabel.setForeground(Color.WHITE);
        southPanel.add(southLabel);

        westPanel = new JPanel();
        westPanel.setOpaque(false);
        westLabel = new JLabel("West: -");
        westLabel.setForeground(Color.WHITE);
        westPanel.add(westLabel);

        tablePanel.add(northPanel, BorderLayout.NORTH);
        tablePanel.add(eastPanel, BorderLayout.EAST);
        tablePanel.add(southPanel, BorderLayout.SOUTH);
        tablePanel.add(westPanel, BorderLayout.WEST);

        trickPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        trickPanel.setOpaque(false);
        tablePanel.add(trickPanel, BorderLayout.CENTER);

        mainFrame.add(tablePanel, BorderLayout.CENTER);

        trickLabel = new JLabel("Trick: 0");
        trickLabel.setForeground(GOLD_ACCENT);
        mainFrame.add(trickLabel, BorderLayout.PAGE_END);

        mainFrame.setVisible(true);
    }

    private JLabel createCardLabel(Card card) {
        JLabel label = new JLabel(card.toString());
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        label.setFont(new Font("Arial", Font.BOLD, 14));

        Suit suit = card.getSuit();
        if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) {
            label.setBackground(Color.RED);
        } else {
            label.setBackground(Color.BLACK);
        }
        return label;

    }

    public void updateHand(Position position, Hand hand) {
        JPanel target;
        if (position == Position.NORTH) {
            target = northPanel;
        } else if (position == Position.EAST) {
            target = eastPanel;
        } else if (position == Position.SOUTH) {
            target = southPanel;
        } else {
            target = westPanel;
        }
        target.removeAll();
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
        infoLabel.setText("Dealer: " + dealer + "   Vulnerability: " + vulnerability);
    }

    public void updateDeclarer(Seat declarer) {
        declarerLabel.setText("Declarer: " + declarer);
    }

    public void updateContract(Contract contract) {
        if (contract == null) {
            contractLabel.setText("Contract: -");
            return;
        }

        String text = "Contract: " + contract.getLevel() + " " + contract.getDenomination();

        if (contract.isDoubled()) {
            text += " Doubled";
        } else if (contract.isRedoubled()) {
            text += " Redoubled";
        }
        contractLabel.setText(text);
    }

    public void updateTrick(List<Card> trick) {
        trickPanel.removeAll();
        for (Card card : trick) {
            JLabel cardLabel = createCardLabel(card);
            trickPanel.add(cardLabel);
        }
        trickPanel.revalidate();
        trickPanel.repaint();
    }

    public void updateScore(int scoreNS, int scoreEW) {
        scoreLabel.setText("NS: " + scoreNS + "   EW: " + scoreEW);
    }

    public void updateNotesPlaceHolder(String text) {
        notesPlaceholderLabel.setText("Notes: " + text);
    }
}