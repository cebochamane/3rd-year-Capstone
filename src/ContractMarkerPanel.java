import java.awt.BasicStroke; // control the lines drawn using Graphics2D
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;// Used to smooth the rounded edges of the marker.

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Displays the current contract in the centre of the table.
 */
public class ContractMarkerPanel extends JPanel {

    private static final Color DARK_GREEN = new Color(12, 46, 38);
    private static final Color GOLD = new Color(201, 162, 39);
    private static final Color LIGHT_GOLD = new Color(230, 199, 103);

    private JLabel contractLabel;

    public ContractMarkerPanel() {
        this.setLayout(new GridBagLayout());
        this.setOpaque(false);
        this.setPreferredSize(new Dimension(90, 64));

        contractLabel = new JLabel("", SwingConstants.CENTER);
        contractLabel.setFont(new Font("Serif", Font.BOLD, 27));

        this.add(contractLabel);

        /*
         * It remains hidden until a contract is created.
         */
        this.setVisible(false);
    }

    /**
     * Updates the contract shown by the marker.
     *
     * @param contract current contract
     */
    public void updateContract(Contract contract) {
        if (contract == null) {
            contractLabel.setText("");
            this.setVisible(false);
            return;
        }

        String denomination;
        String suitColour;

        if (contract.isNoTrump()) {
            denomination = "NT";
            suitColour = "#FAF8F0";
        } else {
            denomination = PlayerHandPanel.getSuitSymbol(contract.getDenomination());

            if (contract.getDenomination() == Suit.HEARTS|| contract.getDenomination() == Suit.DIAMONDS) {
                suitColour = "#BE1E2D";
            } else {
                suitColour = "#FAF8F0";
            }
        }

        String doublingText = "";

        if (contract.isRedoubled()) {
            doublingText = " XX";
        } else if (contract.isDoubled()) {
            doublingText = " X";
        }

        String displayText =
                "<html>"
                + "<span style='color:#FAF8F0'>"
                + contract.getLevel()
                + "</span>"
                + "<span style='color:"
                + suitColour
                + "'>"
                + denomination
                + "</span>"
                + "<span style='color:#FAF8F0'>"
                + doublingText
                + "</span>"
                + "</html>";

        contractLabel.setText(displayText);
        this.setVisible(true);
        this.repaint();
    }

    /**
     * Paints the dark contract plaque and its double gold border.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create(); // create a copy of the Graphics object to avoid modifying the original

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); // enable anti-aliasing for smoother edges

        int width = getWidth();
        int height = getHeight();

        g2d.setColor(DARK_GREEN);
        g2d.fillRoundRect(2, 2, width - 5, height - 5, 20, 20);

        g2d.setColor(GOLD);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(2, 2, width - 5, height - 5, 20, 20);

        g2d.setColor(LIGHT_GOLD);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(6, 6, width - 13, height - 13, 15, 15);

        g2d.dispose();
    }
}